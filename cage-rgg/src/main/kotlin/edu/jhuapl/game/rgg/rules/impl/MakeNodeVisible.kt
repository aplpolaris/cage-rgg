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
package edu.jhuapl.game.rgg.rules.impl

import edu.jhuapl.game.rgg.ResourceGraphGame
import edu.jhuapl.game.rgg.RggLogger
import edu.jhuapl.game.rgg.RggState
import edu.jhuapl.game.rgg.rules.RggActionParameters
import edu.jhuapl.game.rgg.rules.RggBoard
import edu.jhuapl.game.rgg.rules.RggVectorResultDelegate

/**
 * Make target node visible to the team, with source and target nodes fixed or randomized, specified in parameters.
 * Parameter keys are [ACTION_PARAMETER_SOURCE_ID] and [ACTION_PARAMETER_TARGET_ID].
 */
class MakeNodeVisible: RggVectorResultDelegate() {
    lateinit var team: String

    override fun resolve(state: RggState, board: RggBoard, parameters: RggActionParameters) {
        when (val chosen = randomVector(state, board, parameters)) {
            null -> RggLogger.logStatus<MakeNodeVisible>(" - no nodes found")
            else -> {
                state.nodeResources[chosen.to]!!.makeVisibleTo(team)
                RggLogger.logStatus<MakeNodeVisible>(" - found node $chosen")
            }
        }
    }
}
