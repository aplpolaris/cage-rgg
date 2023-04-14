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

import edu.jhuapl.cage.*
import edu.jhuapl.cage.exec.CageExecutionOptions
import edu.jhuapl.cage.rgg.CageRggExperiment
import edu.jhuapl.game.rgg.provider.RggRulesId
import java.util.*

/**
 * Provides a simulation service that can be used for learning with selected games implemented in Java/Kotlin.
 * The service's API allows external control of the game lifecycle, retrieval of observations/actions/rewards
 * as required for training, and more.
 */
class CageServiceImpl: CageService {

    /** Experiment identifiers. */
    private val experimentIds = mutableMapOf<UUID, CageRggExperiment>()
    /** Tracks environments with runtimes where games are being managed/executed in memory. */
    private val environments = mutableMapOf<CageRggExperiment, CageEnvironmentRuntime>()

    //region ENVIRONMENT LIFECYCLE

    /** Make a new environment for the given set of rules. */
    override fun makeEnvironment(rules: RggRulesId): UUID {
        val uuid = UUID.randomUUID()
        val expt = CageRggExperiment(rules, mapOf(), CageExecutionOptions()) // TODO
        experimentIds[uuid] = expt
        environments[expt] = CageEnvironmentRuntime(expt.createGame())
        return uuid
    }

    /** Shutdown environment and release resources. */
    override fun shutdownEnvironment(id: UUID) {
        require(id in experimentIds.keys)
        val envt = experimentIds[id]!!
        environments[envt]!!.shutdown()
        experimentIds.remove(id)
        environments.remove(envt)
    }

    //endregion

    //region GAME EXECUTION

    private fun game(env: UUID): CageEnvironmentRuntime {
        require(env in experimentIds.keys)
        return environments[experimentIds[env]]!!
    }

    override fun resetGame(env: UUID) {
        require(env in experimentIds.keys)
        val expt = experimentIds[env]!!
        val runtime = environments[expt]!!
        runtime.game.reset(expt.options.randomSeed)
    }

    override fun actionSpace(env: UUID, playerId: String): CageActionSpace {
        return game(env).game.actionSpaceFor(CagePlayer(playerId))
    }

//    override fun observationSpace(env: UUID, playerId: String): CageObservationSpace {
//        return game(env).game.(CagePlayer(playerId))
//    }

    override fun observation(env: UUID, playerId: String): CageObservation {
        return game(env).game.getObservationFor(CagePlayer(playerId))
    }

    override fun stepGame(env: UUID, action: CageAction): StepGameResult {
        with (game(env).game) {
            val current = CagePlayer(action.playerId)
            resolve(action)
            val observation = getObservationFor(current)
            val reward = getRewardFor(current)
            if (!isDone) updateCurrentPlayer()
            logIntermediateStatus()
            return StepGameResult(observation, reward, isDone, "")
        }
    }

    //endregion
}

/** Encapsulates execution of an episodic game. */
class CageEnvironmentRuntime(val game: CageGame) {

    /** Shutdown environment and release resources. */
    fun shutdown() {
        // TODO
    }

}

