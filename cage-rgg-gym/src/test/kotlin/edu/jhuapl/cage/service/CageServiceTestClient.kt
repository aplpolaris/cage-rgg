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
import edu.jhuapl.game.rgg.RggActionDoNothing
import edu.jhuapl.game.rgg.provider.RggRulesId
import java.util.*

/** Test client for interacting with [CageServiceImpl] to select a game and run episodic training. */
class CageServiceTestClient {

    private val server = CageServiceImpl()

    private var env: UUID? = null
    private var playerId: String? = null

    //region GAME ENVIRONMENT

    /** Instantiate game experiment on server, return unique id. */
    fun makeEnvironment(rules: RggRulesId, playerId: String): UUID {
        require(env == null) { "Must shutdown old environment before setting up a new one." }
        env = server.makeEnvironment(rules)
        this.playerId = playerId
        return env!!
    }

    /** Shut down game environment, releasing resources */
    fun shutdownEnvironment() {
        require(env != null) { "No environment to shutdown." }
        server.shutdownEnvironment(env!!)
        env = null
        playerId = null
    }

    //endregion

    //region GAME EXECUTION

    /** Reset the game and all components. */
    fun resetGame() {
        return server.resetGame(env!!)
    }

    /** Get set of possible actions. */
    fun actionSpace(): CageActionSpace {
        return server.actionSpace(env!!, playerId!!)
    }

    /** Get observation associated with current player. */
    fun observation(): CageObservation {
        return server.observation(env!!, playerId!!)
    }

    /** Steps game, with given action. */
    fun stepGame(action: String?): StepGameResult {
        return server.stepGame(env!!, CageAction(playerId!!, action ?: RggActionDoNothing.info.id))
    }

    //endregion

}
