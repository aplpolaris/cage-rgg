.. _rgg-library:

###################################################
RGG Rule Library
###################################################

In addition to customizing simulations, users can configure and register rules using ``RggRulesProvider``.
Registering additional rules in this way makes them available to the console game executor (:ref:`rgg-execution-console`)
and to the experiment environments in the `cage-rgg-gym` module.
To create a custom provider, use the following API:

.. code-block:: kotlin

    /** Provides a collection of [RggRules] indexed by id. */
    interface RggRulesProvider {

        /** Get the list of rule ids. */
        val ruleIds: List<RggRulesId>

        /**
         * Create a rules set for given id.
         * @throws IllegalArgumentException if there is no rule set with given id
         */
        @Throws(IllegalArgumentException::class)
        fun createRules(id: RggRulesId): RggRules

    }

This custom rule provider must be registered using Java's service loader API [2]_: create a file in the source directory
``src/main/resources/META-INF/services/edu.jhuapl.game.rgg.provider.RggRulesProvider``
and provide the full class name on a single line in that file.
For an example see ``edu.jhuapl.game.rgg.examples.SampleRggRulesProvider`` in the `cage-rgg` module.

Once registered, the full collection of rule sets can be accessed via ``RggRulesProvider.runtime.ruleIds``
and ``RggRulesProvider.runtime.createRules(id)``.

.. [2] https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/ServiceLoader.html