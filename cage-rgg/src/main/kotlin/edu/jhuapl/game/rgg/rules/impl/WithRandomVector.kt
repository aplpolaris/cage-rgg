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
import edu.jhuapl.game.rgg.RggLogger
import edu.jhuapl.game.rgg.RggState
import edu.jhuapl.game.rgg.rules.RggActionParameters
import edu.jhuapl.game.rgg.rules.RggBoard
import edu.jhuapl.game.rgg.rules.RggVector
import edu.jhuapl.game.rgg.rules.RggVectorResultDelegate

const val SOURCE_NODE_ID = "_source"
const val TARGET_NODE_ID = "_target"

/**
 * Result that changes the resources of a target node via a selectable vector. Filter conditions are used to select a
 * vector in game whose source and target nodes match the given filter. If successful, the result datum is applied to
 * nodes in the game. The result may include special keywords "_source" and "_target" to indicate the source or target
 * of the action.
 */
open class WithRandomVector: RggVectorResultDelegate() {
    var compute: Augment? = null
    lateinit var result: Create

    override fun resolve(state: RggState, board: RggBoard, parameters: RggActionParameters) {
        when (val chosen = randomVector(state, board, parameters)) {
            null -> RggLogger.logStatus<WithRandomVector>(" - no targets found")
            else -> {
                // compute result by evaluating against the game state and adjust game state
                val input = computeResources(state, chosen)
                RggLogger.logStatus<WithRandomVector>(" - found target $chosen and adding $input")
                state.addResources(input, board, chosen.from, chosen.to)
            }
        }
    }

    protected fun computeResources(state: RggState, chosen: RggVector): Datum {
        val input = state.groundTruthAsDatum().toMutableMap()
        input += aliasSourceTargetNodes(input, chosen.from.id, chosen.to.id)
        return result.invoke(compute?.invoke(input) ?: input)
    }

    /** Add aliases for source/target nodes. */
    private fun aliasSourceTargetNodes(input: Datum, sourceId: String, targetId: String): Datum {
        val res = mutableMapOf<String, Any?>()
        input.forEach { (key, value) ->
            when {
                key.startsWith("$sourceId.") -> res[key.replace("$sourceId.", "$SOURCE_NODE_ID.")] = value
                key.startsWith("$targetId.") -> res[key.replace("$targetId.", "$TARGET_NODE_ID.")] = value
            }
        }
        return res
    }
}
