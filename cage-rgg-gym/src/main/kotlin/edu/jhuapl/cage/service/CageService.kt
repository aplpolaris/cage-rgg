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

import edu.jhuapl.cage.CageAction
import edu.jhuapl.cage.CageActionSpace
import edu.jhuapl.cage.CageObservation
import edu.jhuapl.game.rgg.provider.RggRulesId
import java.util.*

/**
 * Provides a simulation service that can be used for learning with selected games implemented in Java/Kotlin.
 * The service's API allows external control of the game lifecycle, retrieval of observations/actions/rewards
 * as required for training, and more.
 */
interface CageService {

    /** Make a new environment for the given set of rules. */
    fun makeEnvironment(rules: RggRulesId): UUID
    /** Shutdown environment and release resources. */
    fun shutdownEnvironment(id: UUID)

    /** Reset the given game. */
    fun resetGame(env: UUID)
    /** Get action space for given game and player. */
    fun actionSpace(env: UUID, playerId: String): CageActionSpace
    /** Get observation for given game and player. */
    fun observation(env: UUID, playerId: String): CageObservation
    /** Iterate the game loop, after taking the given player action. */
    fun stepGame(env: UUID, action: CageAction): StepGameResult

}
