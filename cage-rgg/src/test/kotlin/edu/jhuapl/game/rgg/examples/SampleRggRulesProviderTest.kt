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

import edu.jhuapl.game.rgg.RggLocalExecutor
import edu.jhuapl.game.rgg.RggLogger
import edu.jhuapl.game.rgg.examples.SampleRggRulesProvider.Companion.APT
import edu.jhuapl.game.rgg.examples.SampleRggRulesProvider.Companion.COVID
import edu.jhuapl.game.rgg.examples.SampleRggRulesProvider.Companion.HIDDEN_NODE
import edu.jhuapl.game.rgg.examples.SampleRggRulesProvider.Companion.MITM
import edu.jhuapl.game.rgg.examples.SampleRggRulesProvider.Companion.MULTIPLAYER
import edu.jhuapl.game.rgg.examples.SampleRggRulesProvider.Companion.PATCH
import edu.jhuapl.game.rgg.policy.impl.randomActionPolicies
import org.junit.Test
import java.util.logging.Level

internal class SampleRggRulesProviderTest {

    private fun runGameWithRandomPolicy(id: String) {
        val rules = SampleRggRulesProvider().createRules(id)
        RggLocalExecutor.doGame(rules, rules.randomActionPolicies())
    }

    @Test
    fun `test Terrain Discovery game`() {
        RggLogger.logLevel = Level.INFO
        runGameWithRandomPolicy(HIDDEN_NODE)
    }

    @Test
    fun `test Multiplayer game`() {
        runGameWithRandomPolicy(MULTIPLAYER)
    }

    @Test
    fun `test APT game`() {
        runGameWithRandomPolicy(APT)
    }

    @Test
    fun `test COVID19 game`() {
        runGameWithRandomPolicy(COVID)
    }

    @Test
    fun `test MITM game`() {
        runGameWithRandomPolicy(MITM)
    }

    @Test
    fun `test Software Patching game`() {
        runGameWithRandomPolicy(PATCH)
    }

    @Test
    fun `test Invalid ID passed`() {
        try {
            runGameWithRandomPolicy("I_DO_NOT_EXIST")
        } catch (ex: IllegalArgumentException) {
            // this is the correct case
            assert(true)
        } catch (ex: Exception) {
            // an unexpected exception occurred
            assert(false)
        }
    }

}
