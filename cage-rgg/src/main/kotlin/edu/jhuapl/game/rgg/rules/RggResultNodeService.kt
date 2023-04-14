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

import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.game.rgg.RggState

/** An external service that updates values for a given node, with a query providing a way to update a single node's attributes. */
abstract class RggResultNodeService(val observedNode: String, val updatedNode: String): RggResultService<Datum> {

    override fun getResult(state: RggState, board: RggBoard, parameters: RggActionParameters): Datum {
        val target = board.node(observedNode)!!
        val resources = state.nodeResources[target]!!.valuesAsMap()
        return getResult(resources)
    }

    /** Gets a result based on a given node's state only. */
    abstract fun getResult(state: Datum): Datum

    override fun updateGameState(state: RggState, board: RggBoard, result: Datum) {
        val target = board.node(updatedNode)!!
        result.forEach { (key, value) ->
            if (value != null)
                state.nodeResources[target]?.set(key, value)
            else
                state.nodeResources[target]?.remove(key)
        }
    }

}
