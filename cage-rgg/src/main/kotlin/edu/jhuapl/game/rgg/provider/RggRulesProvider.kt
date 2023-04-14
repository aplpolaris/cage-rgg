/*-
 * #%L
 * cage-rgg-0.1.2-SNAPSHOT
 * %%
 * Copyright (C) 2020 - 2023 Johns Hopkins University Applied Physics Laboratory
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package edu.jhuapl.game.rgg.provider

import edu.jhuapl.game.rgg.rules.RggRules
import edu.jhuapl.util.services.services

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

    companion object {
        private val providers by lazy { services<RggRulesProvider>() }

        /** Create rules, assuming source and version are null. */
        fun createRules(name: String) = runtime.createRules(RggRulesId(name = name))

        /** Includes samples and other registered rule providers. */
        val runtime = object : RggRulesProvider {

            override val ruleIds: List<RggRulesId>
                get() = providers.flatMap { it.ruleIds }

            override fun createRules(id: RggRulesId) =
                providers.firstOrNull { id in it.ruleIds }?.createRules(id)
                    ?: providers.firstRuleSetIgnoringSourceAndVersion(id)
                    ?: throw IllegalArgumentException("Rule id not supported by $this: $id")

            private fun List<RggRulesProvider>.firstRuleSetIgnoringSourceAndVersion(id: RggRulesId): RggRules? {
                forEach { provider ->
                    provider.ruleIds.firstOrNull { it.name == id.name }?.let {
                        return provider.createRules(it)
                    }
                }
                return null
            }

        }

    }

}
