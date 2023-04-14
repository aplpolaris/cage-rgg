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

import com.fasterxml.jackson.module.kotlin.readValue
import edu.jhuapl.data.parsnip.gen.*
import edu.jhuapl.game.rgg.RggStatisticsTest.Companion.expertAptPolicy
import edu.jhuapl.game.rgg.RggStatisticsTest.Companion.expertPatchingPolicy
import edu.jhuapl.game.rgg.examples.SampleRggRulesProvider.Companion.APT
import edu.jhuapl.game.rgg.examples.SampleRggRulesProvider.Companion.PATCH
import edu.jhuapl.game.rgg.io.RggMapper
import edu.jhuapl.game.rgg.policy.impl.*
import org.junit.Test
import java.util.logging.Level
import kotlin.time.ExperimentalTime

class RggMonteCarloTest {

    /** Functions that set the dimensions for types of games */
    @ExperimentalTime
    @Test
    fun testRunAPT() {
        /** Able to set specific constraints on different aspects of the game, set in the .yaml files */
        val dimensions = arrayOf(
            Dimension("/actions/low/consequences/0/odds", FiniteDoubleRangeConstraint(0.05, 0.35, defaultValue = 0.15)),
            Dimension("/actions/med/consequences/0/odds", FiniteDoubleRangeConstraint(0.15, 0.85, defaultValue = 0.40)),
            Dimension("/actions/high/consequences/0/odds", FiniteDoubleRangeConstraint(0.8, 1.0, defaultValue = 1.0))
        )
        testRunCapturingStatistics("examples/resources/AdvancedPersistentThreat.yaml", dimensions, APT)
    }

    @ExperimentalTime
    @Test
    fun testRunPatching() {
        val dimensions = arrayOf(
            Dimension("/actions/externalAttack/consequences/0/odds", FiniteDoubleRangeConstraint(0.6, 1.0, defaultValue = 1.0)),
            Dimension("/actions/internalAttack/consequences/0/odds", FiniteDoubleRangeConstraint(0.5, 1.0, defaultValue = 1.0)),
        )
        testRunCapturingStatistics("examples/resources/SoftwarePatching.yaml", dimensions, PATCH)
    }

    /** This function runs the actual simulations of some of the game types */
    @ExperimentalTime
    fun testRunCapturingStatistics(gameResource: String, dimensions: Array<Dimension<Double>>, id: String, runCount: Int = 50) {
        RggLogger.logLevel = Level.OFF
        val monteCarlo = testMonteCarloGame(gameResource, dimensions)

        when (id) {
            APT -> {
                println("---- RANDOM")
                monteCarlo.runCapturingStatistics(runCount) { _, _ -> RggRandomPolicy() }
                println("---- LOW")
                monteCarlo.runCapturingStatistics(runCount) { _, _ -> RggAlwaysPolicy("low") }
                println("---- MED")
                monteCarlo.runCapturingStatistics(runCount) { _, _ -> RggAlwaysPolicy("med") }
                println("---- HIGH")
                monteCarlo.runCapturingStatistics(runCount) { _, _ -> RggAlwaysPolicy("high") }
                println("---- EXPERT")
                monteCarlo.runCapturingStatistics(runCount) { _, _ -> expertAptPolicy() }
            }

            PATCH -> {
                println("---- RANDOM")
                monteCarlo.runCapturingStatistics(runCount) { _, _ -> RggRandomPolicy() }
                println("---- EXTERNAL")
                monteCarlo.runCapturingStatistics(runCount) { rules, team -> rules.alwaysOrRandom("externalAttack", team) }
                println("---- INTERNAL")
                monteCarlo.runCapturingStatistics(runCount) { rules, team -> rules.alwaysOrRandom("internalAttack", team) }
                println("---- EXPERT")
                monteCarlo.runCapturingStatistics(runCount) { _, team -> expertPatchingPolicy(team) }
            }

            else -> println("Invalid option. Please try again.")
        }
    }

    /** Returns a Monte Carlo game object */
    private fun testMonteCarloGame(gameResource: String, dimensions: Array<Dimension<Double>>): RggMonteCarlo {
        val resource = ResourceGraphGame::class.java.getResource(gameResource)
        val rules = RggMapper.readValue<MutableMap<String, Any?>>(resource)

        val randomizer = MonteCarlo(*dimensions)

        return RggMonteCarlo(randomizer, rules)
    }
}
