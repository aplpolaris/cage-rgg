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
package edu.jhuapl.game.rgg

import edu.jhuapl.game.rgg.policy.RggPolicy
import edu.jhuapl.game.rgg.policy.impl.RggNothingPolicy
import edu.jhuapl.game.rgg.rules.RggRules
import edu.jhuapl.game.rgg.rules.RggTeam

/**
 * A "resource graph game", in which teams make moves to try to gain access to nodes in the "world". A "move" is an attempt to gain access
 * to another node. Each move costs resources, has a certain likelihood of success, gives the player additional resources,
 * and has some likelihood of detection. After each move, the team updates its world model based on what it currently knows.
 * If detected by an opposing team, that team updates its world model as well.
 */
class ResourceGraphGame(
    /** Board, teams, and available actions. */
    val rules: RggRules = RggRules(),
    /** Policy table. */
    var policies: Map<RggTeam, RggPolicy> = mapOf()
) {

    /** Construct with dynamic policy chooser. */
    constructor(rules: RggRules, policies: (RggRules, RggTeam) -> RggPolicy): this(rules) {
        this.policies = rules.teams.associateWith { policies(rules, it) }
    }

    /** The ground-truth state of the game. */
    var state = RggState()
    /** Current team, by index. */
    private var currentTeamIndex = -1

    //region DERIVED PROPERTIES

    /** Gets list of teams. */
    val teams
        get() = rules.teams
    /** Current team. */
    val currentTeam: RggTeam
        get() = teams[currentTeamIndex]
    /** Completion status. */
    val completed: Boolean
        get() = rules.completed(state)

    //endregion

    //region LOOKUPS

    fun team(id: String) = rules.team(id)
    fun node(id: String) = rules.node(id)
    fun vector(id: String) = rules.vector(id)

    fun vectorsBasedAt(node: String) = rules.board.vectorsBasedAt(node)
    fun nodesReachableFrom(node: String) = rules.board.nodesReachableFrom(node)

    //endregion

    //region LIFECYCLE

    /** Setup the game. */
    fun reset() {
        state = RggState() // Reset the game state
        require(teams.size > 0)
        require(policies.keys.containsAll(teams))
        policies.values.forEach { it.reset() }
        currentTeamIndex = 0
        rules.resourceSpace.forEach { (node, dimensions) ->
            dimensions.forEach { dim ->
                dim.constraint.defaultValue?.let { state(node).setInitValue(dim.name, it) }
            }
        }
        rules.actions.values.forEach { it.reset() }
        RggLogger.logStatus<ResourceGraphGame>(state.nodeResources)
    }

    /** Get action for the current team. */
    fun selectAction(): RggAction {
        val policy = policies[currentTeam] ?: RggNothingPolicy()
        val observations = state.observation(currentTeam.id)
        val actionSpace = rules.actionSpace[currentTeam]!!

        val selectedAction = policy.invoke(observations, actionSpace)
        if (selectedAction.id !in actionSpace.keys && selectedAction.id != DO_NOTHING_ID)
            throw IllegalStateException("$selectedAction is invalid for current team.")

        return when (val id = selectedAction.id) {
            DO_NOTHING_ID -> RggActionDoNothing
            else -> rules.actions[id]?.toAction(id, rules.board, selectedAction.parameters)
                ?: throw IllegalStateException("Unable to apply $selectedAction for current game rules.")
        }
    }

    /** Change current team to the next team. */
    fun nextTeam() {
        currentTeamIndex = (currentTeamIndex + 1) % teams.size
    }

    /** Finish the game. */
    fun finish() {}

    //endregion

    /** Centralized logging so we can manage logging game status globally. */
    companion object {
        private val DO_NOTHING_ID = RggActionDoNothing.info.id
    }

}
