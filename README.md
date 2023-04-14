# RGG Simulation Toolkit

## Overview

Resource Graph Games (RGG) are a general class of multiplayer games that play out over graphs, with moves determined largely based on “resources” associated with nodes in the graphs. The RGG Simulation Toolkit supports the design, construction, testing, and execution of these games using a configuration language and associated execution engine.

The toolkit is implemented in Kotlin, with a combination of modules to support building and executing simulations. The core game simulation modules are:

* `cage-rgg` contains the core RGG rule specification and simulation engine, enabling users to define and execute
games
* `cage-rgg-gym` wraps RGG games with player, policy, and game execution management, with service provider
interfaces (SPIs) allowing for integration of remote resources
* `cage-rgg-docs` provides documentation

Two examples are provided as plugins to illustrate game customization:

* `cage-rgg-example-plugin` provides an example game with YAML definition and some custom components
* `cage-rgg-example-plugin-rest` provides an example game utilizing a remote REST service as part of the game

## Getting Started

To get started with this project, you will need to have the following installed on your computer:

* JDK 11 or higher
* Kotlin SDK 1.6 or higher
* Maven
* IntelliJ or a similar IDE

### Building Required Libraries

The project uses the `Parsnip` library, which you will need to clone and build separately. Clone the repository at https://github.com/aplpolaris/parsnip to your local machine and run the following command in the project root directory: `mvn clean install`. Alternately, you can open the project in IntelliJ.

### Building the CAGE-RGG Project

Once you have these dependencies installed, you can clone the repository to your local machine and open the project in IntelliJ. To build the project, run the following command in the project root directory:

* `mvn clean install`

Alternately, you can open the context menu fro the project in IntelliJ, and select `Rebuild Module cage-rgg`. Either option will build the project, run all test cases, and execute sample simulations.

### Running the Sample Simulation

A sample simulation can be found in the `cage-rgg-example-plugin` module. To run this sample (defined in the `MyCustomGame.yaml` file), execute the `MyCustomGameTest.kt` test file within IntelliJ. When executed, this will print interim results to the console window.

### Developing Custom Simulations

To customize simulations, it is recommended to create a new Java/Kotlin module, following the pattern of this plugin module, and either implement an executable `main()` function to execute the simulation with appropriate configuration. See documentation for more details.

## Advanced Usage

See documentation for advanced usage, including the simulation specification language, customizing simulation code, and more.
