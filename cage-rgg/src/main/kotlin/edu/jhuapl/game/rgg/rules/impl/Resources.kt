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
package edu.jhuapl.game.rgg.rules.impl

import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.data.parsnip.datum.transform.Augment
import edu.jhuapl.data.parsnip.datum.transform.Create
import edu.jhuapl.game.rgg.ResourceGraphGame
import edu.jhuapl.game.rgg.RggLogger
import edu.jhuapl.game.rgg.RggState
import edu.jhuapl.game.rgg.rules.RggActionParameters
import edu.jhuapl.game.rgg.rules.RggBoard
import edu.jhuapl.game.rgg.rules.RggResultDelegate

/**
 * Set or add to resource values.
 * Use "add" to add to the current resources, or "put" to replace them.
 */
class Resources : RggResultDelegate {
    var compute: Augment? = null
    var add: Create? = null
    var put: Create? = null

    override fun resolve(state: RggState, board: RggBoard, parameters: RggActionParameters) {
        val augmentedDatum = computeResources(state)
        add?.invoke(augmentedDatum)?.let { datum ->
            RggLogger.logStatus<Resources>(" - adding resources: $datum")
            state.addResources(datum, board)
            RggLogger.logStatus<Resources>(" - new values: " + datum.mapValues { state.valueOf(it.key) })
        }
        put?.invoke(augmentedDatum)?.let {
            RggLogger.logStatus<Resources>(" - setting resources: $it")
            state.replaceResources(it, board)
        }
    }

    private fun computeResources(state: RggState): Datum {
        val input = state.groundTruthAsDatum()
        return compute?.invoke(input) ?: input
    }
}
