/*-
 * #%L
 * cage-rgg-example-plugin-0.1.2-SNAPSHOT
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
package edu.jhuapl.game.rgg.examples.test

import com.fasterxml.jackson.module.kotlin.readValue
import edu.jhuapl.game.rgg.io.RggMapper
import edu.jhuapl.game.rgg.provider.RggRulesId
import edu.jhuapl.game.rgg.provider.RggRulesProvider
import edu.jhuapl.game.rgg.rules.RggRules
import java.net.URL

/** Example provider of a collection of rules. */
class MyCustomRulesProvider: RggRulesProvider {

    override val ruleIds = listOf(RggRulesId(MY_CUSTOM_PROVIDER_ID, MY_CUSTOM_GAME_ID))

    override fun createRules(id: RggRulesId): RggRules {
        require(id.source == MY_CUSTOM_PROVIDER_ID && id.version == null)

        customGames[id.name]?.let {
            return RggMapper.readValue(it)
        } ?: throw IllegalArgumentException("Rule id not supported by $this: $id")
    }

    // store list of URLs and associated id's for games -- games are generated from yaml on request
    private val customGames: Map<String, URL> = listOf(MY_CUSTOM_GAME_ID).associateWith {
        MyCustomRulesProvider::class.java.getResource("resources/$it.yaml")!!
    }

    companion object {
        const val MY_CUSTOM_PROVIDER_ID = "MyGameProvider"
        const val MY_CUSTOM_GAME_ID = "MyCustomGame"
    }

}
