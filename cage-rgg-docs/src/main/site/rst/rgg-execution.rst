.. _rgg-execution:

###################################################
RGG Simulation Engine
###################################################

The RGG simulation toolkit provides an executable for running RGG game simulations,
with an API to enable pluggable actions, consequences, and observations.
Game rules are typically defined primarily within a YAML configuration file (:ref:`rgg-dsl`),
but may also be customized using custom Java/Kotlin code and external simulation components.
This section covers the general process of running a simulation and viewing/capturing results,
and subsequent sections go into more detail on defining and customizing games.

The simulation toolkit is written in Kotlin, as are the code examples below.
Given that Kotlin is a JVM language, any of the examples below could also be written with equivalent Java code.
Additional execution examples can be found in ``RggUnitTest.kt`` and ``SampleRggRulesProviderTest.kt``.

Executing Simulations Locally
++++++++++++++++++++++++++++++++++++++++

Running a simulation requires an RGG rule set and an associated assignment of team policies, and can be done using
``RggLocalExecutor``, as shown in the following Kotlin code:

.. code-block:: kotlin

  val rules: RggRules = ... // load your rules
  val policy: (RggRules, RggTeam) -> RggPolicy = { ... } // define your policy
  RggLocalExecutor.doGame(rules, policy) // execute your game

Rules can be defined directly in code or in YAML, or for testing purposes there are a number of sample rule sets defined in ``SampleRggRulesProvider``.
The ``policy`` parameter here is a Kotlin lambda function, allowing users to define arbitrary policies based on the rules and team.
Upon execution, ``RggLocalExecutor`` logs game status to a selected ``PrintStream``.
It uses ``System.out`` as a default, but this can be customized as needed using the global configuration setting ``ResourceGraphGame.logStream``.

A few basic policies are provided with the module.
Here is an example showing execution of a sample predefined game with a "random action" policy for each team:

.. code-block:: kotlin

  val rules: RggRules = SampleRggRulesProvider().createRules(SampleRggRulesProvider.MULTIPLAYER)
  RggLocalExecutor.doGame(rules, rules.randomActionPolicies())

In *cage-rgg* test code, this is wrapped up into a convenience method in ``SampleRggRulesProviderTest.kt``:

.. code-block:: kotlin

  private fun runGameWithRandomPolicy(id: String) {
    val rules = SampleRggRulesProvider().createRules(id)
    RggLocalExecutor.doGame(rules, rules.randomActionPolicies())
  }

.. code-block:: kotlin

  runGameWithRandomPolicy(SampleRggRulesProvider.MULTIPLAYER)

For single player games, there is a policy ``RggAlwaysPolicy`` for running games with a fixed action:

.. code-block:: kotlin

  val rules: RggRules = SampleRggRulesProvider().createRules(SampleRggRulesProvider.APT)
  RggLocalExecutor.doGame(rules) { _, _ -> RggAlwaysPolicy("med") }

Additionally, there is a convenience method ``alwaysOrRandom()`` for running games with a fixed action for the specified teams,
and randomly for other teams:

.. code-block:: kotlin

  val rules: RggRules = SampleRggRulesProvider().createRules(SampleRggRulesProvider.APT)
  RggLocalExecutor.doGame(rules) { rules, team -> rules.alwaysForOneTeam("med", team) }

Capturing Metrics
++++++++++++++++++++++++++++++++++++++++

The ``RggStatistics`` class provides executors to run multiple simulation iterations, while capturing statistics for
the resources identified as `metrics` in the rule definition. Here is an example:

.. code-block:: kotlin

  val rules = SampleRggRulesProvider().createRules(SampleRggRulesProvider.APT)
  val runCount = 10
  rules.runCapturingStatistics(runCount) { _, _ -> RggRandomPolicy() }

Monte Carlo Simulations
++++++++++++++++++++++++++++++++++++++++

Building on the statistics capture, ``RggMonteCarlo`` provides the ability to execute many times while also randomizing initial conditions.
This class modifies the YAML structure of an RGG rule file directly, so it is less flexible than the above approaches and
requires direct access to the rule definition file.

.. code-block:: kotlin

  fun testPatchingWithMonteCarlo() {
    var dimensions = arrayOf(
      Dimension("/actions/externalAttack/consequences/0/odds", FiniteDoubleRangeConstraint(0.6, 1.0, defaultValue = 1.0)),
      Dimension("/actions/internalAttack/consequences/0/odds", FiniteDoubleRangeConstraint(0.5, 1.0, defaultValue = 1.0)),
    )
    runMonteCarloWithStatistics("examples/resources/SoftwarePatching.yaml", dimensions, PATCH)
  }

  fun runMonteCarloWithStatistics(gameResource: String, dimensions: Array<Dimension<Double>>, id: String, runCount: Int = 1000) {
    ResourceGraphGame.logStream = null
    val monteCarlo = loadMonteCarlo(gameResource, dimensions)
    monteCarlo.runCapturingStatistics(runCount) { _, _ -> RggRandomPolicy() }
  }

  private fun loadMonteCarlo(gameResource: String, dimensions: Array<Dimension<Double>>): RggMonteCarlo {
    val resource = ResourceGraphGame::class.java.getResource(gameResource)
    val rules = RggMapper.readValue<MutableMap<String, Any?>>(resource)
    val randomizer = MonteCarlo(*dimensions)

    return RggMonteCarlo(randomizer, rules)
  }

.. _rgg-execution-console:

Running Samples from the Command Line
++++++++++++++++++++++++++++++++++++++++

Some predefined games can be executed using the ``main()`` function in ``RggLocalExecutor``, which supports the
command-line options below for customizing rules, policies, and execution options.
Customizing policies by team is not currently supported.

.. code-block:: text

    RULE/POLICY OPTION         ALIAS        DESCRIPTION

    -rules                     -r           predefined ruleset id
    -policy                    -p           "none" (default), "always", "random", or "console"
    -always                                 specific action id to use for "always" policy

    GENERAL OPTION             ALIAS        DESCRIPTION

    -execute                   -x           "single" (default), "stats", or "montecarlo" (TBD)
    -run-count                 -rc          number of runs for stats execution (default: 10)
    -random-seed                            (future feature)
    -timeout-seconds                        (future feature)

Rules may refer to one of the default simulations (``apt, covid, mitm, hidden_node, multiplayer, patch``),
or custom rules may be registered as described in :ref:`rgg-library`.

Use a command like the following to execute a simulation this way:

.. code-block:: text

  java -jar xxx.jar edu.jhuapl.game.rgg.RggLocalExecutor -r apt -p random

.. note::

  The `console` policy option provides an interactive mode, asking the user to specify the team action at each game step.
