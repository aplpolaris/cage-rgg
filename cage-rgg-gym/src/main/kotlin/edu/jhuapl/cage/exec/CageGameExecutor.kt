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
package edu.jhuapl.cage.exec

import edu.jhuapl.cage.*
import java.time.Duration

/**
 * Manages distributed game execution. Supports non-local components that may require separate initialization and
 * finalization before and after the primary game loop, and may have varying and/or unpredictable response times.
 */
object CageGameExecutor {

    /**
     * Executes the game exactly once, finishing when the game is done.
     * @throws CageExecutionException if consensus not achieved in given time
     */
    fun executeOnce(game: CageGame, components: List<CageGameComponent>, options: CageExecutionOptions = CageExecutionOptions()): CageGameResult {
        require(game in components) { "Game should be included in list of components." }

        components.getResetConsensus(options.randomSeed, options.timeout)
        game.runMainGameLoop(options.timeout)
        components.getFinishConsensus(options.timeout)

        return game.finalResult()
    }

    /** Runs the primary game loop. Only used once all components are ready. */
    private fun CageGame.runMainGameLoop(timeout: Duration) {
        while (!isDone) {
            logIntermediateStatus()

            val current = currentPlayer()
            val observation = getObservationFor(current)
            val policy = getPolicyFor(current)
            val action = applyPolicy(observation, policy, timeout)
            resolve(action)

            if (!isDone) updateCurrentPlayer()
        }
        logIntermediateStatus()
    }

    //region RUNNERS with TIMEOUTS

    /**
     * Uses given policy to retrieve action for given observation.
     * @throws CageExecutionException if action is not returned in given time
     */
    @Throws(CageExecutionException::class)
    private fun applyPolicy(observation: CageObservation, policy: (CageObservation) -> CageAction, timeout: Duration): CageAction {
        // TODO - run with timeout
        try {
            return policy(observation)
        } catch (x: Exception) {
            throw CageExecutionException("Invocation of policy caused an exception.", x)
        }
    }

    /**
     * Block current thread until receiving reset consensus from all components.
     * @throws CageExecutionException if consensus not achieved in given time
     */
    @Throws(CageExecutionException::class)
    private fun List<CageGameComponent>.getResetConsensus(randomSeed: Long?, timeout: Duration) {
        // TODO - run this in parallel with timeout
        onEach {
            try {
                val reset = it.reset(randomSeed)
                if (!reset) throw CageExecutionException("Component $it reset failed.")
            } catch (x: Exception) {
                throw CageExecutionException("Component $it reset caused an exception.", x)
            }
        }
    }

    /**
     * Block current thread until receiving EOG consensus from all components.
     * @throws CageExecutionException if consensus not achieved in given time
     */
    @Throws(CageExecutionException::class)
    private fun List<CageGameComponent>.getFinishConsensus(timeout: Duration) {
        // TODO - run this in parallel with timeout
        onEach {
            try {
                val finish = it.finish()
                if (!finish) throw CageExecutionException("Component $it finish failed.")
            } catch (x: Exception) {
                throw CageExecutionException("Component $it finish caused an exception.")
            }
        }
    }

    //endregion
}
