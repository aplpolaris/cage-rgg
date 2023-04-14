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
package edu.jhuapl.game.common

import edu.jhuapl.utilkt.core.severe
import java.util.*

/** Property name used for observability. */
const val OBSERVABLE = "observable"

typealias DimensionValue = Pair<String, Any>

/** A collection of resources of various quantities. */
class ResourceBag {

    /** Stores visibility state of the resource bag. */
    private val visibility = mutableSetOf<String>()
    /** Stores values by dimension id. */
    val values = mutableMapOf<String, Any>()
    /** Stores default null values (returned when the bag is not visible) by dimension id. */
    private val nullValues = mutableMapOf<String, Any>()

    //region LOOKUPS

    /** Get value at given dimension, null if not present. */
    fun valueOf(dimId: String): Any? = values[dimId]
    /** Get numeric value of resource, [ifInvalid] returned if not a number or null. */
    fun amountOf(dimId: String, ifInvalid: Number = 0): Number = valueOf(dimId) as? Number ?: ifInvalid

    /** Get all resource values. */
    fun values() = values.map { (k, v) -> k to v }
    /** Get null resource values. */
    fun nullValues() = nullValues.map { (k, v) -> DimensionValue(k, v) }
    /** Get all resource values, as a map. */
    fun valuesAsMap() = values.toMap()
    /** Get visibility state of the bag. */
    fun visibility() = visibility.toSet()

    /** Test if given resource bag is visible for any of the provided labels. Return true if provided labels is empty. */
    fun visible(labels: Set<String>) = labels.isEmpty() || visibility.intersect(labels).isNotEmpty()
    /** Test if any resource has any of the provided visibility labels. Always return true if no labels given. */
    fun visible(vararg labels: String) = visible(setOf(*labels))

    /** Test if bag has any (a positive amount) of given resource. Any value other than 0, 0.0, and false will return true. */
    infix fun hasAny(name: String) = when(val value = valueOf(name)) {
        null -> false
        is Number -> value.toDouble() > 0
        is Boolean -> value
        else -> true
    }

    /** Test if bag has at least the amount of given resource. */
    infix fun hasAtLeast(resource: DimensionValue): Boolean =
            when (val amount = resource.second) {
                is Number -> amountOf(resource.first).toDouble() >= amount.toDouble()
                else -> hasAny(resource.first)
            }

    /** Test if bag has any (a positive amount) of given resource. */
    infix fun hasNoneOf(name: String) = !hasAny(name)

    //endregion

    //region VALUE MUTATORS

    /** Set resource value for given dimension. */
    operator fun set(dimId: String, value: Any) = values.set(dimId, value)

    /** Set initial value for given dimension. */
    fun setInitValue(dimId: String, value: Any) {
        if (dimId == OBSERVABLE) {
            when {
                value is Collection<*> && value.all { it is String } -> visibility.addAll(value.map { it as String })
                else -> severe<ResourceBag>("Invalid observability object $value")
            }
        } else {
            values[dimId] = value
            nullValues[dimId] = value
        }
    }

    /** Remove resource value for given dimension. */
    fun remove(dimId: String) = values.remove(dimId)

    /** Adds given amount of resource to the bag. */
    operator fun plusAssign(resource: DimensionValue) {
        when (resource.second) {
            is Number -> values.merge(resource.first, resource.second as Number, ::mergeAdd)
            else -> values[resource.first] = resource.second
        }
    }

    /** Subtracts given amount of resource to the bag. */
    operator fun minusAssign(resource: DimensionValue) {
        values.merge(resource.first, resource.second as Number, ::mergeSubtract)
    }

    //endregion

    //region VISIBILITY MUTATORS

    /** Add to the visibility state. */
    fun makeVisibleTo(name: String) = visibility.add(name)

    /** Sets the visibility state. */
    fun makeVisibleOnlyTo(names: Collection<String>) {
        visibility.clear()
        visibility.addAll(names)
    }

    //endregion

    override fun toString(): String {
        val desc = StringJoiner(", ")
        desc.add("$OBSERVABLE: $visibility")
        values.forEach { (n, v) -> desc.add("$n: $v") }
        return "{$desc}"
    }
}

//region UTILS

/** Add values with type checking. */
private fun mergeAdd(first: Any, other: Any): Number {
    require(other is Number)
    return when {
        first is Int && other is Int -> first + other
        first is Number -> first.toDouble() + other.toDouble()
        else -> other.toDouble()
    }
}

/** Subtract values with type checking. */
private fun mergeSubtract(first: Any, other: Any): Number {
    require(other is Number)
    return when {
        first is Int && other is Int -> first - other
        first is Number -> first.toDouble() - other.toDouble()
        else -> other.toDouble()
    }
}

//endregion
