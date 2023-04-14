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
 * Support class for results that depend on a randomly selected vector in the graph meeting certain criteria.
 */
abstract class RggVectorResultDelegate: RggResultDelegate {
    var sourceFilter: DatumFilter? = null
    var targetFilter: DatumFilter? = null

    /** Select a random matching vector given the source and target filters. */
    fun randomVector(state: RggState, board: RggBoard, parameters: RggActionParameters = mapOf()): RggVector? =
        matchingVectors(state, board, parameters).let {
            when {
                it.isEmpty() -> null
                else -> it.random()
            }
        }

    /** Get matching vectors given the source and target filters. */
    fun matchingVectors(state: RggState, board: RggBoard, parameters: RggActionParameters = mapOf()): List<RggVector> {
        var vectors = state.matchingVectors(board, sourceFilter, targetFilter)
        vectors = when(val source = parameters.entries.singleOrNull{ it.key == ACTION_PARAMETER_SOURCE_ID }){
            null -> vectors
            else -> vectors.filter { it.from.id == source.value }
        }
        vectors = when(val target = parameters.entries.singleOrNull{ it.key == ACTION_PARAMETER_TARGET_ID }){
            null -> vectors
            else -> vectors.filter { it.to.id == target.value }
        }
        return vectors
    }

}
