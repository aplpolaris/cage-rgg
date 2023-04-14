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

/** A game with distributed, asynchronous components. */
interface CageGame : CageGameComponent {

    /** Check if game is over. */
    val isDone: Boolean
    /** Gets the action space for the current player. */
    fun actionSpaceFor(currentPlayer: CagePlayer): CageActionSpace
    /** Gets the current player. */
    fun currentPlayer(): CagePlayer
    /** Updates the current player. */
    fun updateCurrentPlayer()
    /** Gets the observed state for a given player. */
    fun getObservationFor(current: CagePlayer): CageObservation
    /** Gets policy for a given player. This may involve an asynchronous call. */
    fun getPolicyFor(current: CagePlayer): (CageObservation) -> CageAction
    /** Update game state based on given action. */
    fun resolve(action: CageAction)
    /** Gets the reward for a given player. */
    fun getRewardFor(current: CagePlayer): Double
    /** Perform any desired logging of game status. Called at beginning of game and after every change. */
    fun logIntermediateStatus()
    /** Provide the final status of the game. */
    fun finalResult(): CageGameResult

}
