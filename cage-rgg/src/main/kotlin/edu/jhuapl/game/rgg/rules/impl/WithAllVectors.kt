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

import edu.jhuapl.game.rgg.RggLogger
import edu.jhuapl.game.rgg.RggState
import edu.jhuapl.game.rgg.rules.RggActionParameters
import edu.jhuapl.game.rgg.rules.RggBoard
import edu.jhuapl.game.rgg.rules.RggVector

class WithAllVectors: WithRandomVector() {

    override fun resolve(state: RggState, board: RggBoard, parameters: RggActionParameters) {
        val vectors: List<RggVector> = state.matchingVectors(board, sourceFilter, targetFilter)
        if(vectors.isEmpty()){
            RggLogger.logStatus<WithAllVectors>(" - no targets found")
            return
        }

        // for each vector, resolve actions on each vector
        for(vector in vectors){
            // compute result by evaluating against the game state and adjust game state
            val input = computeResources(state, vector)
            RggLogger.logStatus<WithAllVectors>(" - found target $vector and adding $input")
            state.addResources(input, board, vector.from, vector.to)
        }
    }
}
