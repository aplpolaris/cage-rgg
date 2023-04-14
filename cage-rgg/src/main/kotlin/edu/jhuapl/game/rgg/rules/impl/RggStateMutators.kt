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
import edu.jhuapl.game.common.OBSERVABLE
import edu.jhuapl.game.rgg.RggState
import edu.jhuapl.game.rgg.rules.RggBoard
import edu.jhuapl.game.rgg.rules.RggNode

//region EXTENSION FUNCTIONS FOR ADJUSTING STATE

/**
 * Add resources in [datum] to current state.
 * Allows aliasing resources by "_source" and "_target" when dynamically selecting these nodes.
 */
fun RggState.addResources(datum: Datum, board: RggBoard, sourceNode: RggNode? = null, targetNode: RggNode? = null) {
    datum.forEach { (k, v) ->
        if (v == null) {
            throw IllegalStateException("Attempt to add null resources $k = $v")
        }
        val split = k.split('.')
        when {
            split.size == 1 && targetNode != null -> add(targetNode, k, v)
            split[0] == SOURCE_NODE_ID && sourceNode != null -> add(sourceNode, split[1], v)
            split[0] == TARGET_NODE_ID && targetNode != null -> add(targetNode, split[1], v)
            else -> add(board.nodeNonNull(split[0]), split[1], v)
        }
    }
}

/**
 * Replaces resources in [datum] with given values.
 * Allows aliasing resources by "_source" and "_target" when dynamically selecting these nodes.
 */
fun RggState.replaceResources(datum: Datum, board: RggBoard, sourceNode: RggNode? = null, targetNode: RggNode? = null) {
    datum.forEach { (k, v) ->
        val split = k.split('.')
        when {
            split.size == 1 && targetNode != null -> put(targetNode, k, v)
            split[0] == SOURCE_NODE_ID && sourceNode != null -> put(sourceNode, split[1], v)
            split[0] == TARGET_NODE_ID && targetNode != null -> put(targetNode, split[1], v)
            else -> put(board.nodeNonNull(split[0]), split[1], v)
        }
    }
}

/** Add resource value to current state. */
fun RggState.add(node: RggNode, dimId: String, value: Any) {
    when (dimId) {
        OBSERVABLE -> invoke(node).makeVisibleTo(value.toString())
        else -> invoke(node) += dimId to value
    }
}

/** Replace resource with given value. */
fun RggState.put(node: RggNode, dimId: String, value: Any?) {
    val bag = invoke(node)
    when {
        dimId == OBSERVABLE -> {
            val newNames = when (value) {
                null -> listOf()
                is Iterable<*> -> value.map { it.toString() }
                else -> listOf(value.toString())
            }
            bag.makeVisibleOnlyTo(newNames)
        }
        value == null -> bag.remove(dimId)
        else -> bag[dimId] = value
    }
}

//endregion
