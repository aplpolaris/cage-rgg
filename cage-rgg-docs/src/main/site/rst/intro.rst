####################################
Introduction
####################################

*Resource Graph Games* (RGG) are a general class of multiplayer games that play out over graphs, with moves determined
largely based on "resources" associated with nodes in the graphs. The *RGG Simulation Toolkit* supports the design,
construction, testing, and execution of these games using a configuration language and associated execution engine.

The toolkit is implemented in Kotlin, with a combination of modules to support building and executing simulations.
The core game simulation modules are:

- *cage-rgg* contains the core RGG rule specification and simulation engine, enabling users to define and execute games.
- *cage-rgg-gym* wraps RGG games with player, policy, and game execution management, with service provider interfaces (SPIs)
  allowing for integration of remote resources.
- *cage-rgg-docs* provides this documentation.

Two examples are provided as plugins to illustrate game customization:

- *cage-rgg-example-plugin* provides an example game with YAML definition and some custom components.
- *cage-rgg-example-plugin-rest* provides an example game utilizing a remote REST service as part of the game.

This documentation
describes the structure of RGGs in more detail in :ref:`rgg-terms`,
describes how to execute games and view results in :ref:`rgg-execution`,
provides the detailed YAML specification for building simulations in :ref:`rgg-dsl`, and
describes how to customize games further using plugins in :ref:`rgg-plugins`.