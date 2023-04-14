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
package edu.jhuapl.game.rgg.policy

import edu.jhuapl.game.common.DimensionValue

/** Tracks a set of observations, representing the content observable by a given team in the game. */
class RggObservation(var observations: MutableMap<String, MutableMap<String, Any>> = mutableMapOf()) {

    override fun toString() = StringBuilder().apply {
        observations.keys.forEach {
            this.append("$it  ")
            this.append(observations[it]!!.map { (k, v) -> "$k = $v" }.joinToString(", "))
            this.append("\n") // add newline
        }
    }.toString()

    /** Add observation with given node id, observation name, and value. */
    fun put(nodeId: String, name: String, value: Any) {
        observations.computeIfAbsent(nodeId) { mutableMapOf() }[name] = value
    }

    /** Add observation with given node id and value */
    fun put(nodeId: String, value: DimensionValue) {
        observations.computeIfAbsent(nodeId) { mutableMapOf() }[value.first] = value.second
    }

}
