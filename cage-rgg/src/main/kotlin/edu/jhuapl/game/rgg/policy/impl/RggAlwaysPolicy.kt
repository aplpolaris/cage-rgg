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

import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.game.rgg.policy.RggActionId
import edu.jhuapl.game.rgg.policy.RggObservation
import edu.jhuapl.game.rgg.policy.RggPolicy
import edu.jhuapl.game.rgg.rules.RggActionSpace
import edu.jhuapl.game.rgg.rules.RggRules
import edu.jhuapl.game.rgg.rules.RggTeam

/** Always select the same action. */
class RggAlwaysPolicy(var actionId: String, var actionParams: Datum = mapOf()) : RggPolicy {

    override fun reset() { }

    override fun invoke(p1: RggObservation, p2: RggActionSpace): RggActionId = RggActionId(actionId, actionParams)

}

/** Always select the same action. */
fun RggRules.always(actionId: String) =
    RggAlwaysPolicy(actionId)

/** Always perform the given action, assuming the team is able to perform said action. Otherwise pick at random. */
fun RggRules.alwaysOrRandom(actionId: String, team: RggTeam) : RggPolicy {
    val teamActionSpace = actionSpace[team]!!
    return when (actionId) {
        in teamActionSpace.keys -> RggAlwaysPolicy(actionId)
        else -> RggRandomPolicy()
    }
}
