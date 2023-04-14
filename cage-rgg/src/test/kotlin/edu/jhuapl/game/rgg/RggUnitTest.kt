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

import edu.jhuapl.game.rgg.RggLocalExecutor.logGameMetrics
import edu.jhuapl.game.rgg.examples.SampleRggRulesProvider
import edu.jhuapl.game.rgg.policy.RggActionId
import edu.jhuapl.game.rgg.policy.RggObservation
import edu.jhuapl.game.rgg.policy.RggPolicy
import edu.jhuapl.game.rgg.policy.impl.RggRandomPolicy
import edu.jhuapl.game.rgg.provider.RggRulesProvider
import edu.jhuapl.game.rgg.rules.RggActionSpace
import edu.jhuapl.game.rgg.rules.RggNode
import edu.jhuapl.game.rgg.rules.RggRules
import edu.jhuapl.game.rgg.rules.RggTeam
import org.junit.Test
import kotlin.time.ExperimentalTime

/** This class allows you to pick a list of actions for each team to test and validate output. */
class RggUnitTest {

    @ExperimentalTime
    @Test
    fun testRedReward() {
        /** Determine the actions to take place */
        redMoves = listOf("internalAttack", "internalAttack", "internalAttack")
        blueMoves = listOf("blueNothing", "blueNothing", "blueNothing")

        val rules = RggRulesProvider.createRules(SampleRggRulesProvider.PATCH)

        /** Creates a game with the predetermined set of moves and rules */
        val unitTestGame = rules.runUnitTest()
        assert((unitTestGame.state.nodeResources[RggNode("game")]!!.values["blueReward"] as Double) <
                (unitTestGame.state.nodeResources[RggNode("game")]!!.values["redReward"] as Double))
    }

    @ExperimentalTime
    @Test
    fun testRemediation() {
        redMoves = listOf("externalAttack")
        blueMoves = listOf("remediate")

        val rules = RggRulesProvider.createRules(SampleRggRulesProvider.PATCH)

        /** Set the rules to ensure the proper action takes place */
        rules.actions["externalAttack"]?.consequences!![1].odds = 1.0
        rules.actions["remediate"]?.consequences!![0].odds = 1.0

        val unitTestGame = rules.runUnitTest()
        assert(unitTestGame.state.nodeResources[RggNode("game")]!!.values["blueReward"] == 0.0)
        assert(unitTestGame.state.nodeResources[RggNode("game")]!!.values["redReward"] == 0.0)
        assert(unitTestGame.state.nodeResources[RggNode("game")]!!.values["numCompromised"] == 0)
    }

    @ExperimentalTime
    @Test
    fun testNumCompromised() {
        /** Determine the actions to take place */
        redMoves = listOf("externalAttack", "externalAttack", "externalAttack", "externalAttack", "externalAttack",
            "externalAttack", "externalAttack", "externalAttack")
        blueMoves = listOf("blueNothing", "blueNothing", "blueNothing", "blueNothing", "blueNothing", "blueNothing", "blueNothing",
            "patch")

        val rules = RggRulesProvider.createRules(SampleRggRulesProvider.PATCH)
        val unitTestGame = rules.runUnitTest()
        assert(unitTestGame.state.nodeResources[RggNode("game")]!!.values["numCompromised"] == 8)
        assert(unitTestGame.state.nodeResources[RggNode("game")]!!.values["redReward"] == 76.0)
        assert(unitTestGame.state.nodeResources[RggNode("game")]!!.values["blueReward"] == 0.0)
    }

    @ExperimentalTime
    @Test
    fun testNumPatched() {
        /** Determine the actions to take place */
        redMoves = listOf("externalAttack", "redNothing", "redNothing", "redNothing", "redNothing", "redNothing", "redNothing",
            "redNothing", "redNothing", "internalAttack")
        blueMoves = listOf("remediate", "patch", "patch", "patch", "patch", "patch", "patch", "patch", "patch")

        val rules = RggRulesProvider.createRules(SampleRggRulesProvider.PATCH)
        rules.actions["externalAttack"]?.consequences!![1].odds = 1.0
        rules.actions["remediate"]?.consequences!![0].odds = 1.0

        val unitTestGame = rules.runUnitTest()
        assert(unitTestGame.state.nodeResources[RggNode("game")]!!.values["numPatched"] == 8)
        assert(unitTestGame.state.nodeResources[RggNode("game")]!!.values["redReward"] == 0.0)
        assert(unitTestGame.state.nodeResources[RggNode("game")]!!.values["blueReward"] == 76.0)
    }

    @ExperimentalTime
    @Test
    fun testCantPatchYet() {
        /** Determine the actions to take place */
        redMoves = listOf("redNothing")
        blueMoves = listOf("patch")

        val rules = RggRulesProvider.createRules(SampleRggRulesProvider.PATCH)
        val unitTestGame = rules.runUnitTest()
        assert(unitTestGame.state.nodeResources[RggNode("game")]!!.values["numPatched"] == 0)
        assert(unitTestGame.state.nodeResources[RggNode("game")]!!.values["redReward"] == 0.0)
        assert(unitTestGame.state.nodeResources[RggNode("game")]!!.values["blueReward"] == 0.0)
    }

    @ExperimentalTime
    @Test
    fun testAttackThenRemediate() {
        /** Determine the actions to take place */
        redMoves = listOf("externalAttack", "internalAttack", "externalAttack", "internalAttack", "externalAttack",
            "internalAttack", "externalAttack", "internalAttack", "externalAttack", "internalAttack")
        blueMoves = listOf("remediate", "remediate", "remediate", "remediate", "remediate", "remediate", "remediate",
            "remediate", "remediate", "remediate")

        val rules = RggRulesProvider.createRules(SampleRggRulesProvider.PATCH)
        rules.actions["externalAttack"]?.consequences!![1].odds = 1.0
        rules.actions["remediate"]?.consequences!![0].odds = 1.0

        val unitTestGame = rules.runUnitTest()
        assert(unitTestGame.state.nodeResources[RggNode("game")]!!.values["numPatched"] == 0)
        assert(unitTestGame.state.nodeResources[RggNode("game")]!!.values["numCompromised"] == 0)
        assert(unitTestGame.state.nodeResources[RggNode("game")]!!.values["redReward"] == 0.0)
        assert(unitTestGame.state.nodeResources[RggNode("game")]!!.values["blueReward"] == 0.0)
    }

    companion object {
        private var redMoves: List<String> = listOf()
        private var blueMoves: List<String> = listOf()
        private var redIndex = 0
        private var blueIndex = 0

        /** Iterate the indexing value for the correct moveset list*/
        private fun iterate(currentTeam: RggTeam) {
            if (currentTeam.id == "red") redIndex++ else blueIndex++
        }

        /** Modified game execution function the terminates the run after a set number of actions */
        private fun ResourceGraphGame.execute() {
            reset()
            RggLogger.logStatus<RggUnitTest>("Completed game setup")
            while (!completed) {
                if (redIndex == redMoves.size && blueIndex == blueMoves.size) break
                RggLogger.logStatus<RggUnitTest>("$currentTeam")
                val move = selectAction()
                if (move.valid(state)) {
                    move.resolve(state)
                } else {
                    RggLogger.logStatus<RggUnitTest>("  ... invalid action")
                }
                RggLogger.logStatus<RggUnitTest>("  ... current reward: ${rules.rewards[currentTeam]?.invoke(state)}")
                iterate(currentTeam)
                nextTeam()
            }
            finish()
            redIndex = 0
            blueIndex = 0
            logGameMetrics()
        }

        /** Execute a given RGG game a fixed number of times, capturing statistics. */
        internal fun RggRules.runUnitTest() = runUnitTest { _, team -> unitTestPolicy(team) }

        /** Execute a given RGG game a fixed number of times, capturing statistics. */
        private fun RggRules.runUnitTest(policies: (RggRules, RggTeam) -> RggPolicy): ResourceGraphGame {
            val unitTestGame = ResourceGraphGame(this, policies)
            unitTestGame.reset()
            unitTestGame.execute()
            return unitTestGame
        }

        /** Given a team id, select the correct action */
        private fun unitTestPolicy(team: RggTeam) = object : RggPolicy {
            override fun reset() {}

            override fun invoke(o: RggObservation, p2: RggActionSpace): RggActionId {
                return when (team.id) {
                    "red" -> when {
                        redIndex < redMoves.size -> RggActionId(redMoves[redIndex])
                        else -> RggActionId(RggActionDoNothing.info.id)
                    }
                    "blue" -> when {
                        blueIndex < blueMoves.size -> RggActionId(blueMoves[blueIndex])
                        else -> RggActionId(RggActionDoNothing.info.id)
                    }
                    else -> RggRandomPolicy().invoke(o, p2)
                }
            }
        }
    }
}
