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

import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.data.parsnip.datum.DatumFilter
import edu.jhuapl.game.common.Info
import edu.jhuapl.game.rgg.RggAction
import edu.jhuapl.game.rgg.RggState
import kotlin.random.Random

/**
 * Representation of actions that can be serialized. Each action has a [requires] field indicating a certain precondition
 * on the game that must be met for the action to be valid. There is also a [cost] field that is subtracted from the game
 * state whenever the validity check is made, to act as a penalty or tracker of moves. If the action is valid, a result
 * is selected from the ordered [consequences], with multiple different outcomes possible according to a probability
 * distribution. Each consequence may have additional logic for being resolved.
 */
class RggActionDelegate {

    /** Provides resource amounts to be subtracted when making the move. */
    lateinit var cost: Datum
    /** Maps resource string to predicate. */
    lateinit var requires: DatumFilter
    /** Requires a condition that depends on the game state */
    var requiresParamDependent: ((RggActionParameters) -> DatumFilter)? = null
    /** List of consequences and associated probabilities. */
    var consequences = mutableListOf<RggConsequenceDelegate>()

    fun toAction(id: String, board: RggBoard, parameters: RggActionParameters = mapOf()) = object : RggAction(Info(id)) {
        override fun valid(state: RggState): Boolean {
            state.addResources(cost)
            with(requiresParamDependent) {
                return if (this != null) state.testGroundTruth(requiresParamDependent!!(parameters))
                else state.testGroundTruth(requires)
            }
        }

        override fun resolve(state: RggState) {
            var random = Random.nextDouble()
            var sum = 0.0
            var foundOutcome = false
            for (consequence in consequences) {
                if (consequence.independent) {
                    sum = 0.0
                    random = Random.nextDouble()
                    foundOutcome = false
                }
                sum += consequence.discountedOdds!!
                if (!foundOutcome && sum > random) {
                    consequence.result.resolve(state, board, parameters)
                    foundOutcome = true
                }
            }
        }
    }

    fun reset() {
        consequences.forEach { it.discountedOdds = null }
    }
}

/** List of values that are passed as parameters when actions are called. */
typealias RggActionParameters = Datum
