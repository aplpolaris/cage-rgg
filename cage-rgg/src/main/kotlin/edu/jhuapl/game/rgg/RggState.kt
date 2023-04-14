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
package edu.jhuapl.game.rgg

import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.data.parsnip.datum.DatumFilter
import edu.jhuapl.game.common.OBSERVABLE
import edu.jhuapl.game.common.ResourceBag
import edu.jhuapl.game.rgg.policy.RggObservation
import edu.jhuapl.game.rgg.rules.RggBoard
import edu.jhuapl.game.rgg.rules.RggNode
import edu.jhuapl.game.rgg.rules.RggVector

/** Manages the state objects associated with the board. State is dynamically instantiated. */
class RggState {

    /** Current state for each node. */
    val nodeResources = mutableMapOf<RggNode, ResourceBag>()

    //region QUERIES

    /** Get resources. */
    operator fun invoke(nodeId: String) = invoke(node(nodeId)!!)

    /** Get resources. */
    operator fun invoke(node: RggNode): ResourceBag = nodeResources.getOrPut(node) { ResourceBag() }

    /** Get node by name. */
    private fun node(id: String) = nodeResources.keys.firstOrNull { it.id == id }

    /** Get value at given node by node-resource combo id, returning null if not available. */
    fun valueOf(nodeDotResourceId: String): Any? {
        val (node, resource) = splitComboId(nodeDotResourceId)
        return valueOf(node, resource)
    }

    /** Get value of resource at given node, returning null if not available. */
    fun valueOf(nodeId: String, resourceId: String) =
        node(nodeId)?.let { invoke(it) }?.valueOf(resourceId)

    /** Get amount of resource at given node, returning null if not available or a default value if not a number. */
    fun amountOf(nodeId: String, resourceId: String, ifInvalid: Number = 0) =
        node(nodeId)?.let { invoke(it) }?.amountOf(resourceId, ifInvalid)

    //endregion

    /** Get observation associated with the given team, for the given resource configuration. */
    fun observation(teamId: String) = RggObservation().apply {
        nodeResources.forEach { (node, bag) ->
            // Observation specific to given team
            if (bag.visible(teamId)) {
                put(node.id, OBSERVABLE, true)
                bag.values().forEach { (dim, value) -> put(node.id, dim, value) }
            } else {
                put(node.id, OBSERVABLE, false)
                bag.nullValues().forEach { (dim, value) -> put(node.id, dim, value) }
            }
        }
    }

    /** Get ground-truth observation, for the given resource configuration. */
    fun groundTruthObservation() = RggObservation().apply {
        nodeResources.forEach { (node, bag) ->
            // Ground truth observation (visibility is represented as a set of labels)
            put(node.id, OBSERVABLE, bag.visibility())
            bag.values().forEach { (dim, value) -> put(node.id, dim, value) }
        }
    }

    //region DATUM FUNCTIONS

    /** Add to current state. Keys in [datum] should be of the form "node.resource". */
    fun addResources(datum: Datum) {
        datum.forEach { (k, v) ->
            val (node, resource) = splitComboId(k)
            this(node) += resource to v!!
        }
    }

    /** Tests if current team observation matches given filter. */
    fun testObservation(teamId: String, filter: DatumFilter): Boolean = filter(observationAsDatum(teamId))
    /** Gets the team observations as a datum. */
    fun observationAsDatum(teamId: String): Datum = observation(teamId).observations.asDatum()

    /** Tests if current ground truth state matches given filter. */
    fun testGroundTruth(filter: DatumFilter): Boolean = filter(groundTruthAsDatum())
    /** Gets the ground truth state as a datum. */
    fun groundTruthAsDatum(): Datum = groundTruthObservation().observations.asDatum()

    /** Filter list of nodes in the game state based on provided filter. */
    fun matchingNodes(board: RggBoard, source: DatumFilter?): List<RggNode> = board.nodes.filter {
        matchingNode(it, source)
    }

    /** Filter list of vectors in the game state based on provided filters for source and target. */
    fun matchingVectors(board: RggBoard, sourceFilter: DatumFilter?, targetFilter: DatumFilter?): List<RggVector> = board.vectors.filter {
        matchingNode(it.from, sourceFilter) && matchingNode(it.to, targetFilter)
    }

    /** Tests match for given node. Returns true if filter is null. */
    private fun matchingNode(node: RggNode, filter: DatumFilter?) =
        filter == null || filter(nodeResources[node]!!.bagAsDatum(node.id))

    //endregion

}

/** Represent observations as a datum with node and resource name concatenated. */
private fun Map<String, MutableMap<String, Any>>.asDatum(): Datum = flatMap { en -> en.value.entries.map { comboId(en.key, it.key) to it.value } }.toMap()
/** Represent resource bag as a datum; if nodeId is provided, this is added as an additional field with key "_node". */
private fun ResourceBag.bagAsDatum(nodeId: String?): Datum {
    val res = valuesAsMap() + (OBSERVABLE to visibility())
    return when (nodeId) {
        null -> res
        else -> res + ("_node" to nodeId)
    }
}
