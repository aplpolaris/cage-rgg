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
package edu.jhuapl.cage.rgg

import edu.jhuapl.cage.CageGameResult
import edu.jhuapl.cage.exec.CageExecutionException
import edu.jhuapl.cage.exec.CageGameExecutor

/** Runs [CageRggExperiment]s. */
object CageRggExecutor: (CageRggExperiment) -> CageGameResult {

    /**
     * Executes the game exactly once, finishing when the game is done.
     * @throws CageExecutionException if consensus not achieved in given time
     */
    override fun invoke(experiment: CageRggExperiment): CageGameResult {
        val game = experiment.createGame()
        return CageGameExecutor.executeOnce(game, experiment.components(game), experiment.options)
    }

}
