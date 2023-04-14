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

import com.fasterxml.jackson.annotation.*
import edu.jhuapl.game.common.Info

/** The board defines the set of nodes and vectors that is the "backdrop" for the game. */
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
class RggBoard(val info: Info = Info("board")) {
    /** Set of nodes in the world. */
    var nodes = mutableSetOf<RggNode>()
    /** Set of ways to access other nodes. */
    @get:JsonIgnore
    var vectors = mutableSetOf<RggVector>()

    //region JSON HELPERS

    @JsonCreator
    constructor(nodes: Set<RggNode>, vectors: List<RggVectorById> = listOf()): this() {
        this.nodes = nodes.toMutableSet()
        this.vectorsWithStringIds = vectors
    }

    /** Used to save/restore resource space in JSON/YAML, keeping the node id only. */
    @get:JsonProperty("vectors")
    var vectorsWithStringIds: List<RggVectorById>
        get() = vectors.map { RggVectorById(it.from.id, it.to.id, it.id) }
        set(value) {
            vectors = value.map { RggVector(node(it.from)!!, node(it.to)!!, it.id) }.toMutableSet()
        }

    //endregion

    //region LOOKUPS

    fun node(id: String) = nodes.find { it.info.id == id }
    fun nodeNonNull(id: String) = node(id) ?: throw IllegalArgumentException("Unknown node $id")
    fun vector(id: String) = vectors.find { it.info.id == id }
    fun vectorNonNull(id: String) = vector(id) ?: throw IllegalArgumentException("Unknown vector $id")

    fun vectorsBasedAt(node: String) = vectors.filter { it.from.info.id == node }
    fun nodesReachableFrom(node: String) = vectorsBasedAt(node).map { it.to.id }

    //endregion
}

/** A node in the world. */
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
data class RggNode(val info: Info) {

    @JsonCreator
    constructor(name: String) : this(Info(name))

    @get:JsonValue
    val id: String
        get() = info.id

    override fun toString() = id
}

/**
 * Defines a way that one node can be accessed from another.
 * Not designed for JSON serialization.
 */
data class RggVector(val from: RggNode, val to: RggNode, val info: Info = Info()) {

    constructor(from: RggNode, to: RggNode, id: String) : this(from, to, Info(id))

    val id: String
        get() = info.id

    override fun toString() = "$from > $to"

}

/** Vector for serialization, uses string values. */
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
data class RggVectorById(val from: String, val to: String, val id: String = "")
