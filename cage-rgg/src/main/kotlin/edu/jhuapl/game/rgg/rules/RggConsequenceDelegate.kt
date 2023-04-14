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
package edu.jhuapl.game.rgg.rules

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Delegate for a consequence. Provides odds of occurrence, and an "independent" flag indicating whether this odds is
 * contingent on the last consequence occurring or not.
 *
 * The "odds.discount" parameter can be used to decrease (or increase) the odds exponentially over time.
 */
class RggConsequenceDelegate {
    var odds: Double = 1.0
    @get:JsonProperty("odds.discount")
    var oddsDiscount: Double? = null
    var independent = false
    lateinit var result: RggResultDelegate

    /**
     * Used internally to adjust the actual odds by a constant factor each time this variable is requested.
     * Should only be used by [RggActionDelegate].
     */
    @get:JsonIgnore
    internal var discountedOdds: Double? = null
        get() {
            field = when {
                field == null || oddsDiscount == null -> odds
                else -> field!! * oddsDiscount!!
            }
            return field
        }
}
