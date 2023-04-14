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
package edu.jhuapl.cage

import edu.jhuapl.data.parsnip.gen.DimensionList

/** Toy implementation of CAGE game for testing. */
abstract class CageGameToy(_players: List<CagePlayer>): CageGame {

    val players = _players
    val scores = mutableListOf(0, 0)
    var playerIndex = 0
    var gameStep = 0

    val statusMessage
        get() = "Step: $gameStep, Player 1: ${scores[0]}, Player 2: ${scores[1]}"

    override fun actionSpaceFor(current: CagePlayer): CageActionSpace {
        return mapOf("High" to listOf(), "Low" to listOf())
    }

    override val isDone: Boolean
        get() = gameStep >= 10

    override fun currentPlayer() = players[playerIndex]
    override fun updateCurrentPlayer() { playerIndex = 1 - playerIndex }
    override fun getObservationFor(current: CagePlayer) = CageObservation(current.playerId)
    override fun resolve(action: CageAction) {
        val playerIndex = if (action.playerId == "A") 0 else 1
        val options = if (action.actionId == "High") listOf(0, 1, 5) else listOf(0, 2, 3)
        scores[playerIndex] += options.random()
        gameStep++
    }

    override fun getRewardFor(current: CagePlayer): Double {
        val playerIndex = if (current.playerId == "A") 0 else 1
        return scores[playerIndex]!!.toDouble()
    }
    override fun logIntermediateStatus() { println(statusMessage) }
    override fun finalResult() = CageGameResult(statusMessage)

    override fun reset(randomSeed: Long?) = true
    override fun finish() = true

}
