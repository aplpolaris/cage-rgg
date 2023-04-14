/*-
 * #%L
 * cage-rgg-gym-0.1.2-SNAPSHOT
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
package edu.jhuapl.cage.service

import edu.jhuapl.cage.CageObservation
import edu.jhuapl.game.rgg.provider.RggRulesId

/** Implements episodic game running. */
class CageClientTestExecutor(val client: CageServiceTestClient) {

    fun runLearningEpisodes(rules: RggRulesId, player: String, episodes: Int, maxStepsPerEpisode: Int = 100): List<Double> {
        client.makeEnvironment(rules, player)

        val rewards = mutableListOf<Double>()

        // for now, use a random action
        val actionSpace = client.actionSpace()
        val actionFunction: (CageObservation) -> String = { actionSpace.keys.random() }

        repeat(episodes) {
            client.resetGame()
            var iter = 0
            var stepResult = client.stepGame(null)
            while (iter < maxStepsPerEpisode && !stepResult.done) {
                val action: String = actionFunction(stepResult.observation)
                stepResult = client.stepGame(action)
                iter++
            }
            println("Finished episode after $iter timesteps ${if (stepResult.done) "(completed)" else "(terminated at max iterations)"}")
            rewards += stepResult.reward
        }

        client.shutdownEnvironment()
        return rewards
    }

}
