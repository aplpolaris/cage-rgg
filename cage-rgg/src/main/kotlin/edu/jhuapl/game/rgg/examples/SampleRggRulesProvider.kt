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
package edu.jhuapl.game.rgg.examples

import com.fasterxml.jackson.module.kotlin.readValue
import edu.jhuapl.game.rgg.provider.RggRulesId
import edu.jhuapl.game.rgg.provider.RggRulesProvider
import edu.jhuapl.game.rgg.rules.RggRules
import edu.jhuapl.game.rgg.io.RggMapper
import java.net.URL

/** Provides some sample games that are included with the core library. */
class SampleRggRulesProvider : RggRulesProvider {

    companion object {
        val PROVIDER_ID = SampleRggRulesProvider::class.simpleName

        const val APT = "apt"
        const val COVID = "covid"
        const val MITM = "mitm"
        const val HIDDEN_NODE = "hidden_node"
        const val MULTIPLAYER = "multiplayer"
        const val PATCH = "patch"
    }

    private val sampleGames: Map<String, URL> = mapOf(
        APT to resource("AdvancedPersistentThreat"),
        COVID to resource("Covid19"),
        MITM to resource("ManInTheMiddle"),
        HIDDEN_NODE to resource("TerrainDiscovery"),
        MULTIPLAYER to resource("MultiplayerTestRgg"),
        PATCH to resource("SoftwarePatching")
    )

    private fun resource(id: String) = SampleRggRulesProvider::class.java.getResource("resources/$id.yaml")

    override val ruleIds: List<RggRulesId> = sampleGames.keys.map { RggRulesId(PROVIDER_ID, it) }

    /** Lookup a sample rules object by name. */
    fun createRules(name: String) = createRules(RggRulesId(PROVIDER_ID, name))

    override fun createRules(id: RggRulesId): RggRules {
        require(id.source == PROVIDER_ID && id.version == null)
        sampleGames[id.name]?.let {
            return RggMapper.readValue(it)
        } ?: throw IllegalArgumentException("Rule id not supported by $this: $id")
    }

}
