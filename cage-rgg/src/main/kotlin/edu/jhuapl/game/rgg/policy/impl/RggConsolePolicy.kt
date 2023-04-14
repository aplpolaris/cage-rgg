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
package edu.jhuapl.game.rgg.policy.impl

import edu.jhuapl.data.parsnip.datum.MutableDatum
import edu.jhuapl.data.parsnip.gen.Dimension
import edu.jhuapl.game.common.DimensionValue
import edu.jhuapl.game.rgg.RggLogger.printConsole
import edu.jhuapl.game.rgg.RggLogger.printlnConsole
import edu.jhuapl.game.rgg.policy.RggActionId
import edu.jhuapl.game.rgg.policy.RggObservation
import edu.jhuapl.game.rgg.policy.RggPolicy
import edu.jhuapl.game.rgg.rules.RggActionSpace
import java.util.*

/** Present observation vector to the console and ask for action inputs. */
class RggConsolePolicy(val teamId: String): RggPolicy {

    override fun reset() { }

    override fun invoke(observation: RggObservation, actionSpace: RggActionSpace): RggActionId {
        printlnConsole(observation)
        printConsole("What action should $teamId take? ")
        val possibleActions = actionSpace.keys.toList()
        var selectedActionIndex: Int? = null
        while (selectedActionIndex !in possibleActions.indices) {
            printlnConsole(possibleActions.mapIndexed { i, s -> "($i) $s" }.joinToString("   "))
            selectedActionIndex = Scanner(System.`in`).next().toIntOrNull()
        }
        val chosen = possibleActions[selectedActionIndex!!]
        val parameters: MutableDatum = mutableMapOf()
        if (!actionSpace[chosen].isNullOrEmpty()) {
            for (dim in actionSpace[chosen]!!) {
                var selectedParamValue: DimensionValue? = null
                while (selectedParamValue == null) {
                    printlnConsole("What should \"${dim.name}\" be for action $chosen - constraint: ${dim.constraint}")
                    val input = Scanner(System.`in`).next()
                    selectedParamValue = parseInputDimension(input, dim as Dimension<Any>)
                }
                parameters[dim.name] = selectedParamValue
            }
        }
        return RggActionId(chosen, parameters)
    }

    private fun parseInputDimension(input: String, dim: Dimension<Any>): DimensionValue? {
        val value: Any = when(dim.constraint.typeAsString){
            "BOOLEAN" -> input.toBoolean()
            "STRING" -> input
            "INTEGER" -> input.toInt()
            "SET" -> input.split(",").toMutableSet()
            else -> input.toFloat()
        }

        return when(dim.constraint.contains(value)){
            true -> DimensionValue(dim.name, value)
            false -> null
        }
    }

}
