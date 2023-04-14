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

import edu.jhuapl.cage.*
import edu.jhuapl.game.rgg.*
import edu.jhuapl.game.rgg.policy.RggObservation
import edu.jhuapl.game.rgg.policy.RggPolicy
import edu.jhuapl.game.rgg.rules.RggRules

/** Wraps an RGG as a [CageGame]. */
class CageGameRgg(var rules: RggRules, var policies: Map<String, RggPolicy>): CageGame {

    /** The ground-truth state of the game. */
    var state = RggState()
    /** Current team, by index. */
    private var currentTeamIndex = -1

    override fun reset(randomSeed: Long?): Boolean {
        require(rules.teams.size > 0)
//        require(policies.keys.containsAll(rules.teams.map { it.id }))

        state = RggState()
        currentTeamIndex = 0
        rules.resourceSpace.forEach { (node, dimensions) ->
            dimensions.forEach { dim ->
                dim.constraint.defaultValue?.let { state(node).setInitValue(dim.name, it) }
            }
        }
        rules.actions.values.forEach { it.reset() }

        RggLogger.logStatus<CageGameRgg>("Completed game setup")
        return true
    }

    override fun actionSpaceFor(current: CagePlayer): CageActionSpace {
        return rules.actionSpace[rules.team(current.playerId)]!!
    }

    override val isDone: Boolean
        get() = rules.completed(state)

    override fun currentPlayer() = CagePlayer(rules.teams[currentTeamIndex].id)

    override fun updateCurrentPlayer() {
        currentTeamIndex = (currentTeamIndex + 1) % rules.teams.size
    }

    override fun getObservationFor(current: CagePlayer): CageObservation =
        state.observation(current.playerId).let {
            CageObservation(current.playerId, it.observations)
        }

    override fun getRewardFor(current: CagePlayer): Double {
        return rules.rewards[rules.team(current.playerId)]!!.invoke(state).toDouble()
    }


    override fun getPolicyFor(current: CagePlayer): (CageObservation) -> CageAction = {
        val curTeam = rules.teams[currentTeamIndex]
        val policy = policies[curTeam.id]!!
        val rggObservation = RggObservation(it.observations)
        val rggActionSpace = rules.actionSpace[curTeam]!!
        val rggAction = policy.invoke(rggObservation, rggActionSpace)
        CageAction(current.playerId, rggAction.id)
    }

    override fun resolve(action: CageAction) {
        val rggAction = rules.actions[action.actionId]!!.toAction(action.actionId, rules.board)
        if (rggAction.valid(state)) {
            rggAction.resolve(state)
        } else {
            RggLogger.logStatus<CageGameRgg>("Team selected action $action, but this is not valid for the current game state, and will be ignored.")
        }
    }

    override fun logIntermediateStatus() {
        RggLogger.logStatus<CageGameRgg>(state.nodeResources)
    }

    override fun finalResult(): CageGameResult {
        RggLogger.logStatus<CageGameRgg>("Game complete. Final rewards/metrics:")
        val rewards = rules.teams.map { it to rules.rewards[it]!!.invoke(state) }
        RggLogger.logStatus<CageGameRgg>(
            rewards.joinToString("\n   - ", prefix = "   - ") { "${it.first.id} = ${it.second}" }
        )
        val metrics = rules.metrics.map { it to state.valueOf(it) }
        RggLogger.logStatus<CageGameRgg>(
            metrics.joinToString("\n   - ", prefix = "   - ") { "${it.first} = ${it.second}" }
        )
        return CageGameResult("")
    }

    override fun finish() = true

}
