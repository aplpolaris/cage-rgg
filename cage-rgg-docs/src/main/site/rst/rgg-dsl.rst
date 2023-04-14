.. _rgg-dsl:

#######################################################
RGG Rule Specification (YAML)
#######################################################

YAML Overview
++++++++++++++++++++++++++++++++

The *cage-rgg* module provides a *domain specific language* (DSL) for defining RGG rule sets in `yaml` configuration files.
The files contain all of the components in :ref:`rgg-terms` other than policy definitions, with general structure as follows.
Details about each field are below.

.. code-block:: yaml

    # metadata for the game
    metadata
      id: "about the game"

    # teams in the game
    teams: [s, t]

    # board on which the game is played
    board:
      nodes: [a, b, c]
      vectors:
        - {from: a, to: b}
        - ...

    # game resources associated with each node
    resourceSpace:
      a:
        - # resource object
        - ...
      b:
        - ...

    # possible actions that can be taken by each team
    actionSpace:
      s:
        action1: ...
        action2: ...
      t:
        ...

    # action definitions and constraints
    actions:
      a1: # scan from node a
        cost: ...
        requires: ...
        consequences:
          - # consequence object
          - ...

    # reward space computes a numeric value from the state space for each team
    rewards:
      s: # compute object
      t: ...

    # completion step computes a boolean value from the state space
    completed: # boolean compute object

    # values to be logged to the console upon game completion (optional)
    metrics:
      - # resource

YAML Object Details
++++++++++++++++++++++++++++++++

A detailed specification of this structure follows. In some cases, the specification references *Parsnip*,
which is used for value filters and computations. See [1]_ for details.

Metadata
--------------------------------

The `metadata` object contains:

- `id` (string): game id

Teams
--------------------------------

The `teams` object is an array of strings representing the team ids.

Board
--------------------------------

The `board` object contains:

- `nodes` (array of strings): node ids
- `vectors` (array of objects): directed links/vectors between two node ids, of the form ``{ from: a, to: b }``

The nodes may represent positions in the game "terrain", or may be placeholders to store, e.g. state associated with a single team.
Vectors are intended to support defining the space of legal "moves" in the game.
See `MakeNodeVisible` and `WithRandomVector` below to see how this works in practice.

Resources
--------------------------------

The `resourceSpace` object defines the set of resources associated with each node.
Resources are defined as values within a constrained or unconstrained space of possible values.
Here is an example that defines a set of resources for the node ``target_node``:

.. code-block:: yaml

    - target_node:
        observable: set(red, blue)
        vulnerable: true
        compromised: int.range(0; 0,1)
        parameter: float.range(3.0; 0.0,10.0) # 3.0 is the default value, acceptable values between 0.0 and 10.0
        reward: int(1)

Resources are defined as key-value pairs, where the value is a string describing both the initial value and the associated space.
The general syntax for this is ``type(initial; param1, param2, ...)`` or ``type(param1, param2, ...)``,
but additional variations are supported:

- ``string(value)`` for a string
- ``boolean(value)`` for a boolean, where ``value`` is ``true`` or ``false``
- ``enum(value; value1, value2, ...)`` for a string value in a fixed set of permissible values
- ``set(v1, v2, ...; value1, value2, ...)`` for a set of strings, initialized to ``[v1, v2, ...]`` with allowed items in ``[value1, value2, ...]``
- ``int(value)`` or ``int.range(value; min, max)`` for an unconstrained or constrained integer (integer must be in the *closed* range `[min, max]`)
- ``int.enum(value; value1, value2, ...)`` for an integer value in a fixed set of permissible values
- ``float(value)`` or ``float.range(value; min, max)`` for an unconstrained or constrained float (float must be in the *closed* range `[min, max]`)
- ``float.enum(value; value1, value2, ...)`` for a float value in a fixed set of permissible values
- ``float.normal(value; mean, dev)`` for a float value in a Gaussian distribution of real values

If omitted, a default initial condition is used (`0` and `0.0` for integer and float, `""` for strings, or an empty set).
In limited cases, the value can be provided by itself:

- `true` or `false` can be used directly

Action Space
--------------------------------

The `actionSpace` object encodes the space of permissible actions for each team. Actions may or may not be parameterized.
If not parametrized, the action id maps to an empty value.
If parameterized, the action id maps to a list of dimensions,
defined as key-value pairs as in `resourceSpace` but without initial conditions.
Values here should have the general form ``type(param1, param2, ...)``.
Parameters may be used to customize consequence results, as described in :ref:`consequence-plugin`.

.. code-block:: yaml

    actionSpace:
      red: # team
        nothing: # an action without parameters
        scan: # an action with a single parameter
          source: string.enum( a, b, c, d, e, f, g, h )

.. _value-filters:

Actions
--------------------------------

The `actions` object specifies the preconditions and consequences of each action.
It is a key-value structure where the keys are action ids (as listed in `actionSpace`),
and the values are "action delegate" objects with `cost`, `requires`, and `consequences` fields.
An example follows:

.. code-block:: yaml

    actions:
      scan:
        cost: # key-value object
          a.capital: -1
        requires: # filter object
          a.capital: { Gte: 1 }
        consequences:
          - odds: 0.9 # float
            independent: true # optional indicating the consequence is independent from prior consequences
            result: # result object
          - # more consequence objects

The `cost` field encodes a resource-value object.
Keys are references to resources in the game state, in the form ``node.resource``, and values should be numeric.
This cost is added to the current game state whenever a team selects that action.

The `requires` field encodes a conditional statement about the current game state to define when the action is "valid".
If the action is invalid, the cost will still be applied but there will be no consequence.
Keys are references to resources in the game state, in the form ``node.resource``,
and the value is either a fixed value (e.g. ``1`` or ``true``) or a filter object of the form ``{ FilterType: parameters }``.

The full list of supported value filters is defined in [1]_. In summary:

- General filters for arbitrary value types include ``Equal: v, NotEqual: v, OneOf: [v1, v2, ...]``.
- Filters for numeric fields include ``Gt: v, Gte: v, Lt: v, Lte: v, Range: [min, max]``.
- Filters for strings include ``Contains, StartsWith, EndsWith, Matches, ContainsMatch``.
- Filters for sets are not directly supported, but string filters can be used to test against the set's string representation.
- Filters can be combined using ``And, Or, Not``.

The `consequences` field is an array of possible outcomes, encoding both the likelihood of occurrence and the impact on the game state.

Two parameters determine when the consequence occurs: `odds` is a float between 0 and 1 indicating the likelihood of the outcome,
and `independent` is a true/false value indicating whether the outcome depends on prior outcomes.
These are interpreted as follows:

- When selecting an outcome, a random number between 0 and 1 is chosen.
- If `independent` is missing or always false, the first outcome with cumulative odds greater than that random number is selected to occur.
  In this case, consequences should be listed in increasing order, so if mutually exclusive outcomes have likelihoods of 50%, 30%, and 20%,
  the odds should be captured as 0.5, 0.8, and 1.0.
- If `independent` is present, a new random number is chosen for this and successive consequences.
  In this case, consequences are not mutually exclusive, and likelihoods are set directly.
- An additional parameter `odds.discount` can be specified, which will adjust the odds each time the probability of the
  outcome is checked. A value of `0.95`, for instance, would reduce the likelihood by `5%` each time.

For each consequence, the `result` field encodes what happens when the action is selected and valid.
The following result objects are currently supported, described in more depth below.

- ``Resources: { compute: { }, add: { }, put: { } }``, used to calculate and modify node resources in the game state.
- ``MakeNodeVisible: { team: x, source: {}, target: {} }``, used to make a randomly selected a node to make visible.
- ``WithRandomVector: { team: x, source: {}, target: {}, result: {} }``, used to apply an outcome based on a randomly selected vector.

Additional results can be defined as plugins, as described in :ref:`consequence-plugin`.

.. _value-calculations:

`Resources` Result
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

The `Resources` result has syntax ``Resources: { compute: { }, add: { }, put: { } }`` and
modifies the game state by either adding values (`add`), or replacing values (`put`).

- The `compute` parameter can be used to calculate intermediate values.
  It consists of key-value pairs where keys are intermediate variable ids, and values are calculations.
- The `add` parameter can be used to add values to the game state.
  It consists of key-value pairs where keys have the form ``node.resource``, and values are calculations.
- The `put` parameter can be used to replace values in the game state.
  It consists of key-value pairs where keys have the form ``node.resource``, and values are calculations.

Value calculations may be either fixed values, references to other fields, or calculations.
In the example below, `Calculate` is used to calculate a value based on a selection of node resources (or intermediate values),
while `Field` is used to reference an intermediate value.
The full list of supported value calculations is defined in [1]_.

Here is an example:

.. code-block:: yaml

  # virus behavior
  spread:
    cost: {}
    requires: {}
    consequences:
      - odds: 1.0
        result:
          Resources:
            compute:
              newInfected: { Calculate: "5 * {disease.r0} * {disease.recoveryRate} * {population.susceptible} * {population.infected} / {population.count}" }
              newRecovered: { Calculate: "{disease.recoveryRate} * {population.infected}" }
              newDeath: { Calculate: "{disease.deathRate} * {population.infected}" }
            add:
              population.susceptible: { Calculate: "-{newInfected}" }
              population.infected: { Calculate: "{newInfected} - {newRecovered} - {newDeath}" }
              population.recovered: { Field: "newRecovered" }
              population.dead: { Field: "newDeath" }
            put:
              simulation.running: true

`MakeNodeVisible` Result
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

The `MakeNodeVisible` result has syntax ``MakeNodeVisible: { team: x, source: {}, target: {} }`` and
makes a randomly selected node in the game state "visible".

- This selects a random vector by applying `source` and `target` criteria to the resources on the `from` and `to` nodes in all game vectors,
  and makes the target node visible for the given team.
  The filters are expressed as key-value pairs where keys have the form ``resource`` and values are value filters, as defined in :ref:`value-filters`.
- Nodes can also be chosen explicitly using the syntax ``source: { _node: x }`` (and similar for `target`).

Here is an example:

.. code-block:: yaml

  scan: # scan from node a
    cost:
      a.capital: 1
      a.moves: -1
    requires:
      a.capital: { Gte: 1 }
    consequences:
      - odds: 0.9
        result:
          MakeNodeVisible:
            team: red
            source: { _node: a }
            target: { observable: { Not: { Contains: red } } }

`WithRandomVector` Result
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

The `WithRandomVector` result has syntax ``WithRandomVector: { team: x, source: {}, target: {}, compute: {}, result: {} }`` and
applies a result for a randomly selected vector.

- This selects a random vector by applying `source` and `target` criteria to the resources on the `from` and `to` nodes in all game vectors,
  and adds the result object to the game state.
  The filter syntax is as defined in :ref:`value-filters`.
- Nodes can also be chosen explicitly using the syntax ``source: { _node: x }`` (and similar for `target`).
- An optional `compute` object can be used to compute intermediate values, as defined in :ref:`value-calculations`.
- The result object is a set of key-value pairs, where keys have the form ``node.resource``
  and values are calculations, as defined in :ref:`value-calculations`.
- Both `compute` and `result` can reference the source or target node directly using `_source.resource` or `_target.resource`,
  or can reference other nodes by name. If the node is omitted, it is assumed to be the target node.

Once the vector is selected, the values in `result` are added to the current game state.
Here is an example:

.. code-block:: yaml

  attack: # attack from random owned node to random other node
    cost:
      a.capital: 2
      a.moves: -1
    requires:
      a.capital: { Gte: 2 }
    consequences:
      - odds: 0.5
        result:
          WithRandomVector:
            team: red
            source: { ownership: true } # conditions on source node
            target: { ownership: false, observable: { Contains: red } } # conditions on target node
            result: { _target.ownership: true }

Rewards
--------------------------------

The `rewards` object calculates a reward for each team. Keys are teams and values are computations as defined in :ref:`value-calculations`.

.. code-block:: yaml

    rewards:
      red: { Calculate: "-{a.capital}" }

Completion
--------------------------------

The `completed` object defines when the game terminates, with value being a boolean computation as defined in :ref:`value-filters` above.
Here is an example, with two termination conditions.

.. code-block:: yaml

    completed:
      Or:
      - g.ownership: true
      - a.capital: { Lte: 0.0 }

Metrics
--------------------------------

The `metrics` object is a list of node resource values to log and make available at the end of a successfully completed game.
Resources have the form ``node.resource``.

.. code-block:: yaml

    metrics:
      - game.numTotal
      - game.moves
      - attacker.success

.. [1] Parsnip data mapping documentation