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
package edu.jhuapl.game.rgg.rules

import edu.jhuapl.game.rgg.RggState

/** Uses an external service to prepare result and update the game. */
interface RggResultService<V> {
    /** Create query based on current game state. */
    fun getResult(state: RggState, board: RggBoard, parameters: RggActionParameters): V

    /** Updates game state based on result. */
    fun updateGameState(state: RggState, board: RggBoard, result: V)
}

