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

import edu.jhuapl.data.parsnip.datum.filter.DatumFieldFilter
import edu.jhuapl.game.rgg.examples.SampleRggRulesProvider
import edu.jhuapl.game.rgg.examples.SampleRggRulesProvider.Companion.HIDDEN_NODE
import edu.jhuapl.game.rgg.provider.RggRulesProvider
import edu.jhuapl.game.rgg.rules.impl.MakeNodeVisible
import edu.jhuapl.game.rgg.io.RggMapper
import edu.jhuapl.util.internal.prettyPrintJsonTest
import org.junit.Test

internal class RggRulesTest {

    @Test
    fun `test serialize result delegates`() {
        MakeNodeVisible().apply {
            team = "red"
            sourceFilter = DatumFieldFilter("_node", "a")
            targetFilter = null
        }.prettyPrintJsonTest()
    }

    @Test
    fun testSerialize() {
        RggRules().prettyPrintJsonTest()
        terrainDiscovery().prettyPrintJsonTest()
    }

    @Test
    fun testLoadFromYaml() {
        terrainDiscovery().prettyPrintJsonTest()
    }

    @Test
    fun testTheyreTheSameRules() {
        val exportFromCodeRules = RggMapper.writeValueAsString(terrainDiscovery())

        val yamlRules = terrainDiscovery()
        val exportFromYamlRules = RggMapper.writeValueAsString(yamlRules)

        // we expect both exports to have the same content, but won't write an assertion yet since the order may differ
    }

    /** Get rules from terrain discovery game. */
    private fun terrainDiscovery() = RggRulesProvider.createRules(HIDDEN_NODE)

}
