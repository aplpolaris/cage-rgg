.. _rgg-terms:

###################################################
RGG Terminology
###################################################

A *resource graph game* is a multiplayer game that plays out over a graph with nodes and links between nodes. The game
rules are determined primarily by properties of the nodes called *resources*, which may change over the course of the
game. These resources determine both the set of moves available to the teams and when the game is complete.

Resource Graph Game Rules
++++++++++++++++++++++++++++++++

In the RGG Simulation Toolkit, an *RGG rule set* consists of the following elements:

- A sequence of **teams**

  - Games may involve a single team or multiple teams.
  - Teams take turns according to the sequence.

- A game **board**, with **nodes** and directed links between nodes called **vectors**

  - There may be any number of vectors between the same two nodes.

- A **resource space** associating **resources** to each node

  - This describes the *ground truth* of the game.
  - Each node has a fixed number of *resources*, each of which has a fixed type (bounded/unbounded numbers, strings, enums, booleans, etc.).
  - The game rules specify an initial allocation of resources.

- An **observability rule** defining what resources are visible to each team

  - Observability is defined at the node level, so the same for all resources belonging to the same node.
  - Observability may change over the course of the game.
  - At any given time, the *resource space* and the *observability rule* determine the *observation* for a team.

- A set of allowable moves for each team called **actions**

  - Actions have a *resource requirement* (what resources are required at what nodes as a precondition),
    and a **consequence** (how resource allocations change as a consequence of the action, possibly as a probability distribution).
  - Actions are often based at a given node/vector and require a minimum amount of resources to be available at that node/vector to proceed.
  - The **action space** is the set of all actions that are available at a given time to a team.

- A numeric **reward function** for each team

  - May be a node resource, or may be calculated from node resources.

- A **termination criterion** indicating when the game is over

  - Termination is calculated as true/false based on the current resource space.

In summary, the **game rules** consists of (i) the teams, (ii) the board, (iii) an initial allocation of resources/observability to nodes,
(iv) a set of allowed moves per team, (v) a reward function per team, and (vi) a termination criterion.
The **game state** is the ground truth allocation of node resources and observability to teams/nodes.

Resource Graph Game Policies
++++++++++++++++++++++++++++++++

Resource graph games are designed for the exploration and optimization of decision-making rules.
A team **policy** is an algorithm or method for selecting an action based on the current observation vector.

- Some policies may depend on the team's current belief about resource allocation in addition to observations.
- Some policies may select actions outside of the available action space due to false beliefs;
  these are invalid and should result in "losing a turn" and possibly a penalty to the reward function.

This observation/policy structure allows for *reinforcement learning* and other automated learning strategies to be
applied to the game rules, with the goal of creating a policy that optimizes a team's reward function.