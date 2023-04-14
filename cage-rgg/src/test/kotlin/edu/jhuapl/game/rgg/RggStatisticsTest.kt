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

import edu.jhuapl.game.rgg.examples.SampleRggRulesProvider.Companion.APT
import edu.jhuapl.game.rgg.examples.SampleRggRulesProvider.Companion.PATCH
import edu.jhuapl.game.rgg.policy.RggActionId
import edu.jhuapl.game.rgg.policy.RggObservation
import edu.jhuapl.game.rgg.policy.RggPolicy
import edu.jhuapl.game.rgg.policy.impl.*
import edu.jhuapl.game.rgg.provider.RggRulesProvider
import edu.jhuapl.game.rgg.rules.RggActionSpace
import edu.jhuapl.game.rgg.rules.RggTeam
import org.junit.Test
import java.util.logging.Level
import kotlin.time.ExperimentalTime

/** Class for running a game under certain circumstances a 'runCount' number of times */
class RggStatisticsTest {

    /** Functions that run the tests */
    @ExperimentalTime
    @Test
    fun testAptStats() {
        RggLogger.logLevel = Level.OFF

        val rules = RggRulesProvider.createRules(APT)
        val runCount = 10

        println("---- RANDOM")
        rules.runCapturingStatistics(runCount) { _, _ -> RggRandomPolicy() }

        println("---- LOW")
        rules.runCapturingStatistics(runCount) { _, _ -> RggAlwaysPolicy("low") }
        println("---- MED")
        rules.runCapturingStatistics(runCount) { _, _ -> RggAlwaysPolicy("med") }
        println("---- HIGH")
        rules.runCapturingStatistics(runCount) { _, _ -> RggAlwaysPolicy("high") }

        println("---- EXPERT")
        rules.runCapturingStatistics(runCount) { _, _ -> expertAptPolicy() }
    }

    @ExperimentalTime
    @Test
    fun testPatchingStats() {
        RggLogger.logLevel = Level.OFF

        /** Sets the type of game and the number of runs per game type */
        val rules = RggRulesProvider.createRules(PATCH)
        val runCount = 10

        /** Randomly chooses actions for each team to take */
        println("---- RANDOM")
        rules.runCapturingStatistics(runCount) { _, _ -> RggRandomPolicy() }

        /** Red team only performs external attacks */
        println("---- EXTERNAL")
        rules.runCapturingStatistics(runCount) { rules, team -> rules.alwaysOrRandom("externalAttack", team) }
        /** Red team only performs internal attacks */
        println("---- INTERNAL")
        rules.runCapturingStatistics(runCount) { rules, team -> rules.alwaysOrRandom("internalAttack", team) }
        /** Red team operates based on the expertPatchingPolicy, defined below */
        println("---- EXPERT")
        rules.runCapturingStatistics(runCount) { _, team -> expertPatchingPolicy(team) }
    }

    companion object {

        internal fun expertAptPolicy() = object : RggPolicy {
            override fun reset() {}

            override fun invoke(o: RggObservation, p2: RggActionSpace): RggActionId {
                return when {
                    o.observations["p8"]!!["own"]!! == true -> RggActionId("high")
                    else -> RggActionId("low")
                }
            }
        }

        /** Custom policies to influence the actions taken by a specific team in certain situations */
        internal fun expertPatchingPolicy(team: RggTeam) = object : RggPolicy {
            override fun reset() {}

            override fun invoke(o: RggObservation, p2: RggActionSpace): RggActionId {
                return when (team.id) {
                    // If it's red's turn, perform internal attack unless blue has been alerted.
                    "red" -> when {
                        (o.observations["game"]!!["blueAlerted"]!! == 1) -> RggActionId("internalAttack")
                        else -> RggActionId("externalAttack")
                    }
                    // If not alerted, Blue team does nothing. Otherwise, it will start patching.
                    "blue" -> when {
                        o.observations["game"]!!["blueAlerted"]!! == 0 -> RggActionId(RggActionDoNothing.info.id)
                        (o.observations["game"]!!["numCompromised"]!! as Int) > 0 -> RggActionId("patch")
                        else -> RggActionId(RggActionDoNothing.info.id)
                    }
                    // Catch all policy
                    else -> RggRandomPolicy().invoke(o, p2)
                }
            }
        }

    }

}
