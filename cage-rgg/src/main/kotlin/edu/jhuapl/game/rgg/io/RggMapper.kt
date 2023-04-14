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
package edu.jhuapl.game.rgg.io

import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import edu.jhuapl.data.parsnip.gen.DimensionConstraint
import edu.jhuapl.data.parsnip.io.NameObjectDeserializer
import edu.jhuapl.data.parsnip.io.SimpleValueSerializer
import edu.jhuapl.data.parsnip.io.commonTypeModule
import edu.jhuapl.data.parsnip.io.parsnipModule
import edu.jhuapl.game.rgg.policy.RggPolicy
import edu.jhuapl.game.rgg.provider.RggExtensions
import edu.jhuapl.game.rgg.rules.RggResultDelegate

/**
 * Singleton mapper instance with objects required for RGG object serialization already registered.
 */
object RggMapper : YAMLMapper() {
    init {
        registerModule(KotlinModule())
        registerModule(rggModule())
        registerModule(parsnipModule())
        registerModule(commonTypeModule())
    }

    /** Module for RGG-custom serialization. */
    fun rggModule(): SimpleModule {
        val sm = SimpleModule()
        sm.serialize<DimensionConstraint<*>>(SimpleValueSerializer)
        sm.deserialize(ResultDelegateDeserializer)
        sm.deserialize(DimensionConstraintDeserializer)
        sm.deserialize(PolicyDeserializer)
        return sm
    }

    //region CUSTOM TYPE HANDLING

    /** Allows parsing dimension constraints based on the short names in the Parsnip library. */
    object DimensionConstraintDeserializer : NameObjectDeserializer<DimensionConstraint<*>>({
        RggExtensions.dimensionConstraintLookup(
            it
        )
    })

    /** Allows parsing result delegates based on the short names in the RGG rules package. */
    object ResultDelegateDeserializer : NameObjectDeserializer<RggResultDelegate>({
        RggExtensions.resultDelegateLookup(
            it
        )
    })

    /** Allows parsing policies based on the short names in the RGG rules package. */
    object PolicyDeserializer : NameObjectDeserializer<RggPolicy>({ RggExtensions.policyLookup(it) })

    //endregion

    /** Extension function to simplify adding serializers to module. */
    private inline fun <reified T> SimpleModule.serialize(serializer: JsonSerializer<in T>) { addSerializer(T::class.java, serializer) }

    /** Extension function to simplify adding deserializers to module. */
    private inline fun <reified T> SimpleModule.deserialize(deserializer: JsonDeserializer<out T>) { addDeserializer(T::class.java, deserializer) }

}
