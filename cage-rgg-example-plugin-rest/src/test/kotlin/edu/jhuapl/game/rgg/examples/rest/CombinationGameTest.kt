/*-
 * #%L
 * cage-rgg-example-plugin-rest-0.1.2-SNAPSHOT
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
package edu.jhuapl.game.rgg.examples.rest

import edu.jhuapl.game.rgg.RggLocalExecutor
import edu.jhuapl.game.rgg.examples.rest.CheckCombinationGameRulesProvider.Companion.MY_CUSTOM_GAME_ID_LOCAL
import edu.jhuapl.game.rgg.examples.rest.CheckCombinationGameRulesProvider.Companion.MY_CUSTOM_GAME_ID_REST
import edu.jhuapl.game.rgg.examples.rest.CheckCombinationGameRulesProvider.Companion.MY_CUSTOM_PROVIDER_ID
import edu.jhuapl.game.rgg.provider.RggExtensions
import edu.jhuapl.game.rgg.provider.RggRulesId
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime

class CombinationGameTest {

    @Test
    fun testLookupResultDelegate() {
        assertEquals(CheckCombination::class.java, RggExtensions.resultDelegateLookup("CheckCombination"))
    }

    @Test
    fun `test simulation game from RggLocalExecutor`() {
        // simple game to illustrate passing values to an external service, and using the result to update game state
        val rules = CheckCombinationGameRulesProvider().createRules(RggRulesId(MY_CUSTOM_PROVIDER_ID, MY_CUSTOM_GAME_ID_LOCAL))
    }

    @ExperimentalTime
    @Test
    fun `test run LOCAL game from RggLocalExecutor main`() {
        // demonstrates that if we register the game, we can execute from command-line
        val args = arrayOf("-r", MY_CUSTOM_GAME_ID_LOCAL, "-p", "random")
        RggLocalExecutor.main(args)
    }

    @ExperimentalTime
    fun `test run REST game from RggLocalExecutor main`() {
        // demonstrates that if we register the game, we can execute from command-line
        val args = arrayOf("-r", MY_CUSTOM_GAME_ID_REST, "-p", "random")
        RggLocalExecutor.main(args)
    }

    companion object {
        @ExperimentalTime
        @JvmStatic
        fun main(args: Array<String>) {
            CombinationGameTest().`test run REST game from RggLocalExecutor main`()
        }
    }

}
