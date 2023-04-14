/*-
 * #%L
 * cage-rgg-example-plugin-0.1.2-SNAPSHOT
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
package edu.jhuapl.game.rgg.examples.test

import edu.jhuapl.game.rgg.RggState
import edu.jhuapl.game.rgg.rules.*

/**
 * Example of a custom result function. Any parameters that should be configurable from within the YAML specification file
 * can be provided as constructor parameters here.
 *
 * See also [RggResultNodeService] which provides some standard code for getting data from one node and applying result to another.
 */
class MyCustomResultFunction(val input: String, val output: String) : RggResultDelegate {

    override fun resolve(state: RggState, board: RggBoard, parameters: RggActionParameters) {
        // collect data from the input node (to be specified in YAML file)
        val nodeWithInputs = board.node(input)!!
        val nodeResource = state.nodeResources[nodeWithInputs]!!.valuesAsMap()

        // restrict to just the data with the attempted combination
        val comboEntered = nodeResource.filterKeys { it in "abcd" } as Map<String, Number>
        val result = checkCombination(comboEntered)

        // update the game state with the lock/unlock state
        val target = board.node(output)!!
        result.forEach { (key, value) ->
            state.nodeResources[target]?.set(key, value)
        }
    }

    /** Sample function providing the basic logic to be implemented -- here just checking for a matching combination. */
    private fun checkCombination(combo: Map<String, Number>): Map<String, Boolean> {
        log("checkCombination() $combo")
        val toCheck = listOf(combo["a"], combo["b"], combo["c"], combo["d"])
        val unlocked = toCheck == listOf(5, 1, 2, 3) || toCheck == listOf(5.0, 1.0, 2.0, 3.0)
        return mutableMapOf("unlock" to unlocked)
    }

    //region LOGGING UTILS

    private val ANSI_RESET = "\u001B[0m"
    private val ANSI_PURPLE = "\u001B[35m"

    private fun log(text: String) = println("[${ANSI_PURPLE}NOTE$ANSI_RESET] $text")

    //endregion

}
