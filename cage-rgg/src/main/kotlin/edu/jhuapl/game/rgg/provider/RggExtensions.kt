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
package edu.jhuapl.game.rgg.provider

import edu.jhuapl.data.parsnip.gen.DimensionConstraint
import edu.jhuapl.game.rgg.policy.RggPolicy
import edu.jhuapl.game.rgg.rules.RggResultDelegate
import edu.jhuapl.game.rgg.io.RggMapper
import edu.jhuapl.util.services.services
import edu.jhuapl.util.types.TypeUtils
import edu.jhuapl.utilkt.core.fine
import edu.jhuapl.utilkt.core.severe

/** Provides pluggable components for RGG games through Java's service API. */
object RggExtensions {

    //region DEFAULT LOOKUPS

    private const val DIMENSION_CONSTRAINT_DEFAULT_PACKAGE = "edu.jhuapl.data.parsnip.gen"
    private const val RESULT_DELEGATE_DEFAULT_PACKAGE = "edu.jhuapl.game.rgg.rules.impl"
    private const val POLICY_DEFAULT_PACKAGE = "edu.jhuapl.game.rgg.policy.impl"

    private val STANDARD_DIMENSION_LOOKUPS: List<(String) -> Class<out DimensionConstraint<*>>?> = listOf(
        { packageLookup(it, DIMENSION_CONSTRAINT_DEFAULT_PACKAGE) },
        { serviceLoaderLookup(it) }
    )

    private val STANDARD_RESULT_LOOKUPS: List<(String) -> Class<out RggResultDelegate>?> = listOf(
        { packageLookup(it, RESULT_DELEGATE_DEFAULT_PACKAGE) },
        { serviceLoaderLookup(it) }
    )

    private val STANDARD_POLICY_LOOKUPS: List<(String) -> Class<out RggPolicy>?> = listOf(
        { packageLookup(it, POLICY_DEFAULT_PACKAGE) },
        { serviceLoaderLookup(it) }
    )

    //endregion

    fun dimensionConstraintLookup(shortClassName: String): Class<out DimensionConstraint<*>> {
        return typeLookup(shortClassName,
            STANDARD_DIMENSION_LOOKUPS + services<RggExtensionLookup>().map { it.dimensionConstraintLookup }
        )
    }

    fun resultDelegateLookup(shortClassName: String): Class<out RggResultDelegate> {
        return typeLookup(shortClassName,
            STANDARD_RESULT_LOOKUPS + services<RggExtensionLookup>().map { it.resultDelegateLookup }
        )
    }

    fun policyLookup(shortClassName: String): Class<out RggPolicy> {
        return typeLookup(shortClassName,
            STANDARD_POLICY_LOOKUPS + services<RggExtensionLookup>().map { it.policyLookup }
        )
    }

    /** Create a policy from given id and name. */
    fun createPolicy(id: String, parameters: Map<String, Any?>): RggPolicy {
        val type: Class<out RggPolicy> = policyLookup(id)
        return RggMapper.convertValue(parameters, type)
    }

    //region UTILITIES FOR CLASS LOOKUP

    /**
     * Returns the class corresponding to the given shorthand name.
     * @param <T> generic type
     * @param shortClassName short type name
     * @param lookups methods for looking up classes by name
     * @return instance if found
     * @throws ClassNotFoundException if the class can't be found
     */
    inline fun <reified T : Any> typeLookup(shortClassName: String, lookups: List<(String) -> Class<out T>?>): Class<out T> {
        lookups.forEach { lookup ->
            lookup(shortClassName)?.let { return it }
        }
        throw ClassNotFoundException("Unable to find object $shortClassName of type ${T::class.java}")
    }

    /** Looks for a class in one of a given number of packages. Returns [null] if not found. */
    inline fun <reified T : Any> packageLookup(shortClassName: String, vararg packages: String): Class<out T>? {
        val type = T::class.java

        // iterate through list of classes with package names added
        // ignore if class not found, but log an error if it's the wrong type
        val qualifiedNamesToTry = listOf(shortClassName) + packages.map { "$it.$shortClassName" }
        for (name in qualifiedNamesToTry) {
            try {
                val c = Thread.currentThread().contextClassLoader.loadClass(name)
                if (c != null && type.isAssignableFrom(c)) {
                    return c as Class<T>
                } else if (c != null) {
                    severe<TypeUtils>("Expected type $type but was $c")
                }
            } catch (x: ClassNotFoundException) {
                fine<TypeUtils>("Expected in most cases")
            }
        }
        return null
    }

    /** Looks for a class using types registered using [ServiceLoader]. */
    inline fun <reified T : Any> serviceLoaderLookup(shortClassName: String): Class<out T>? {
        val type = T::class.java
        val c = services(type).firstOrNull { it::class.java.simpleName == shortClassName }?.javaClass
        if (c != null && type.isAssignableFrom(c)) {
            return c
        } else if (c != null) {
            severe<TypeUtils>("Expected type $type but was $c")
        }
        return null
    }

    //endregion

}
