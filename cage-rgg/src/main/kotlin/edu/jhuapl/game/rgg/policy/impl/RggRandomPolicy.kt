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

import edu.jhuapl.game.common.DimensionValue
import edu.jhuapl.game.rgg.*
import edu.jhuapl.game.rgg.policy.RggActionId
import edu.jhuapl.game.rgg.policy.RggObservation
import edu.jhuapl.game.rgg.policy.RggPolicy
import edu.jhuapl.game.rgg.rules.RggActionParameters
import edu.jhuapl.game.rgg.rules.RggActionSpace
import edu.jhuapl.game.rgg.rules.RggRules
import edu.jhuapl.game.rgg.rules.RggTeam

/** Always select the same action. */
class RggRandomPolicy: RggPolicy {

    override fun reset() { }

    override fun invoke(p1: RggObservation, actionSpace: RggActionSpace): RggActionId {
        val actionId = actionSpace.keys.random()
        val parameters: RggActionParameters = when (val paramList = actionSpace[actionId]) {
            null -> mapOf()
            else -> paramList.associate { DimensionValue(it.name, it.constraint.random()!!) }
        }

        val logMessageBuilder = StringBuilder().append(" - selected action \"$actionId\"")
        if (parameters.isNotEmpty()) {
            logMessageBuilder.append(" with parameters: $parameters")
        }
        RggLogger.logStatus<RggRandomPolicy>(logMessageBuilder.toString())

        return RggActionId(actionId, parameters)
    }

}

/** Team-policy mapping that picks a random action for all teams. */
fun RggRules.randomActionPolicies() =
    teams.associateWith { RggRandomPolicy() }
