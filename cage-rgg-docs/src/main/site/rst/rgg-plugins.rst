.. _rgg-plugins:

###################################################
RGG Customization and Plugins
###################################################

Plugins can be used to customize simulations, using a service provider interface (SPI).
Currently, the following customizations are supported

- Create a custom consequence outcome by implementing and registering `RggResultDelegate`.
- Create a custom dimension constraint by implementing and registering `DimensionConstraint`.
- Create a custom policy by implementing and registering `RggPolicy`.

Registration of these three is handled through a custom implementation of `RggExtensionLookup`, with the following API:

.. code-block:: kotlin

    /** Provides methods for looking up custom logic for use within [RggRules]. */
    interface RggExtensionLookup {
        fun lookupResultDelegate(shortClassName: String) : Class<out RggResultDelegate>?
        fun lookupDimensionConstraint(shortClassName: String) : Class<out DimensionConstraint<*>>?
        fun lookupPolicy(shortClassName: String) : Class<out RggPolicy>?
    }

The `shortClassName` parameter here is how the custom functionality is referenced in the simulation YAML file.
This custom lookup must be registered using Java's service loader API [3]_: create a file in the source directory
``src/main/resources/META-INF/services/edu.jhuapl.game.rgg.provider.RggExtensionLookup``
and provide the full class name on a single line in that file.
For an example see ``edu.jhuapl.game.rgg.examples.rest.CombinationGameExtensions`` in the `cage-rgg-plugin-rest` module.

.. _consequence-plugin:

Customizing Consequence Results
+++++++++++++++++++++++++++++++++++

As described in :ref:`value-filters`, `actions` uses `cost` as a baseline outcome for selecting an action,
`requires` is used to determine when an action is valid, and `consequences` is used to to determine what happens for
valid actions. Consequences have `odds` to determine likelihood of occurrence, and `result` to determine the impact on
the game. The core RGG library includes `Resources`, `MakeNodeVisible`, and `WithRandomVector` result functions.
Additional outcomes can be provided by implementing the following interface with custom Java or Kotlin code:

.. code-block:: kotlin

  /** Adjust the state of the game, when a valid action is taken and consequence is selected. */
  interface RggResultDelegate {
      fun resolve(state: RggState, board: RggBoard, parameters: ActionParameters = emptyList())
  }

The result is dependent on the state of the game (`RggState`), the game topology (`RggBoard`),
and the action's parameters (`ActionParameters`).
There are no restrictions on what part of the game state can be modified by this result.

Some partial implementations are provided with the core RGG library:

- `RggNodeResultDelegate` provides a unique result based on a random node in the graph (possibly within a subset of all nodes)
- `RggVectorResultDelegate` provides a unique result based on a random vector in the graph (possibly within a subset of all vectors)
- `RggResultServiceDelegate` delegates the outcome to a `RggResultService`, which has one function to compute a "result" object and one function to update the game state

Custom result functions may extend one of these objects, or may be written from scratch.
To make them available for simulations, register them with a custom `RggExtensionLookup` as shown above.
Here is an example implementation that registers all result implementations in a given directory
(from the `cage-rgg-plugin-rest` module):

.. code-block:: kotlin

    /** Provides custom class lookups for the sample combination game. */
    class CombinationGameExtensions: RggExtensionLookup {

        override fun lookupResultDelegate(shortClassName: String): Class<out RggResultDelegate>? {
            // example showing how the RGG yaml file can reference classes in custom locations
            return RggExtensions.packageLookup(shortClassName, "edu.jhuapl.game.rgg.examples.rest")
        }

        override fun lookupDimensionConstraint(shortClassName: String): Class<out DimensionConstraint<*>>? = null
        override fun lookupPolicy(shortClassName: String): Class<out RggPolicy>? = null

    }

The `lookupResultDelegate` function could also provide an explicit name to class mapping.

Customizing Dimension Constraints
+++++++++++++++++++++++++++++++++++
This is a beta feature and will be documented in a future release.

Customizing Policies
+++++++++++++++++++++++++++++++++++
This is a beta feature and will be documented in a future release.

.. [3] https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/ServiceLoader.html