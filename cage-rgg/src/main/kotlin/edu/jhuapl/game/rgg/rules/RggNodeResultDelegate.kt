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

import edu.jhuapl.data.parsnip.datum.DatumFilter
import edu.jhuapl.game.rgg.RggState

/**
 * Support class for results that depend on a randomly selected node in the graph meeting certain criteria.
 */
abstract class RggNodeResultDelegate: RggResultDelegate {

    open lateinit var team: String
    open var targetFilter: DatumFilter? = null

    /** Get matching nodes given the source filter. */
    fun matchingNodes(state: RggState, board: RggBoard, parameters: RggActionParameters = mapOf()): List<RggNode> {
        var nodes = state.matchingNodes(board, targetFilter)
        nodes = when (val target = parameters.entries.singleOrNull { it.key == ACTION_PARAMETER_TARGET_ID }) {
            null -> nodes
            else -> nodes.filter { it.id == target.value }
        }
        return nodes
    }

    /** Select a random matching node given the source filter. */
    fun randomNode(state: RggState, board: RggBoard): RggNode? = matchingNodes(state, board).let {
        when {
            it.isEmpty() -> null
            else -> it.random()
        }
    }

}
