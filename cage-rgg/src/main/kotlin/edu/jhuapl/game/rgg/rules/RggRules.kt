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

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import edu.jhuapl.data.parsnip.datum.DatumCompute
import edu.jhuapl.data.parsnip.datum.DatumFilter
import edu.jhuapl.data.parsnip.datum.filter.None
import edu.jhuapl.data.parsnip.gen.Dimension
import edu.jhuapl.data.parsnip.gen.DimensionConstraint
import edu.jhuapl.data.parsnip.gen.DimensionList
import edu.jhuapl.game.common.Info
import edu.jhuapl.game.rgg.ResourceGraphGame
import edu.jhuapl.game.rgg.RggState

typealias RewardFunction = (RggState) -> Float
typealias CompletionFunction = (RggState) -> Boolean

typealias ActionPlusParameters = Map<String, ConstrainedDimensionListDelegate?>
typealias ConstrainedDimensionListDelegate = Map<String, String>

/**
 * Defines the elements that comprise a [ResourceGraphGame], including the [RggBoard], the set of [RggTeam]s, and the actions/rewards
 * associated with each team.
 */
class RggRules(val metadata: Info = Info("rules")) {

    constructor(name: String) : this(Info(name))

    /** The set of teams in the game (ordered). */
    var teams = mutableListOf<RggTeam>()
    /** The nodes and vectors for the game. */
    var board = RggBoard()

    /** The resource space associated with nodes; also includes observability information. */
    @get:JsonIgnore // see below
    var resourceSpace = mutableMapOf<RggNode, DimensionList>()
    /** The set of viable actions in the game per team */
    @get:JsonIgnore // see below
    var actionSpace = mutableMapOf<RggTeam, RggActionSpace>()
    /** Table of actions, referenced by id. */
    @get:JsonProperty("actions")
    var actions = mutableMapOf<String, RggActionDelegate>()
    /** The reward functions associated with each of the teams. */
    @get:JsonIgnore // see below
    var rewards = mutableMapOf<RggTeam, RewardFunction>()
    /** When the game is over. */
    @get:JsonIgnore // see below
    var completed: CompletionFunction = DatumFilterCompletionFunction(None())

    /** List of fields that should be logged, in "node.resource" notation. */
    var metrics = mutableListOf<String>()

    //region JSON HELPERS

    /** Used to save/restore resource space in JSON/YAML, keeping the node id only. */
    @get:JsonProperty("resourceSpace")
    var resourceSpaceAsString: Map<String, Map<String, String>>
        get() = resourceSpace.map { (node, dimensions) ->
            node.id to dimensions.associate { it.name to it.constraint.toString() }
        }.toMap()
        set(value) {
            resourceSpace = value.map {
                board.nodeNonNull(it.key) to it.value.map { Dimension(it.key, DimensionConstraint.valueOf(it.value)) }
            }.toMap().toMutableMap()
        }


    /** Used to save/restore action space in JSON/YAML. */
    @get:JsonProperty("actionSpace")
    var actionSpaceAsString: Map<String, ActionPlusParameters>
        get() = actionSpace.entries.associate { (team, space) ->
            team.id to space.mapValues { (_, dimensions) ->
                dimensions.associate { it.name to it.constraint.toString() }
            }.toMap()
        }
        set(value) {
            actionSpace = value.map { (teamId, actionParams) ->
                team(teamId)!! to actionParams.mapValues { (_, dimensions) ->
                    dimensions?.map { (name, constraint) -> Dimension(name, DimensionConstraint.valueOf(constraint)) }
                        ?: emptyList()
                }
            }.toMap().toMutableMap()
        }

    /** Used to save/restore rewards in JSON/YAML, keeping the node id only. */
    @get:JsonProperty("rewards")
    var rewardsWithTeamIds: Map<String, DatumCompute<*>>
        get() = rewards.map { it.key.id to datumCompute(it.value) }.toMap()
        set(value) {
            rewards = value.map { team(it.key)!! to DatumComputeRewardFunction(it.key, it.value) as RewardFunction }.toMap().toMutableMap()
        }

    /** Gets reward function as a datum compute object. */
    private fun datumCompute(reward: RewardFunction) = when(reward) {
        is DatumComputeRewardFunction -> reward.compute
        else -> throw UnsupportedOperationException("We only support DatumComputeRewardFunction for now.")
    }

    /** Wraps a compute object for a team as a [RewardFunction]. */
    class DatumComputeRewardFunction(var teamId: String, var compute: DatumCompute<*>) : RewardFunction {
        override fun invoke(s: RggState) =
            (compute(s.observationAsDatum(teamId)) as? Number)?.toFloat()
                ?: throw IllegalStateException("Invalid reward function $compute")
    }

    /** Used to save/restore completion status in JSON/YAML, keeping the node id only. */
    @get:JsonProperty("completed")
    var completedWithTeamIds: DatumFilter
        get() = datumFilter(completed)
        set(value) {
            completed = DatumFilterCompletionFunction(value)
        }

    /** Gets completed function as a datum filter object. */
    private fun datumFilter(complete: CompletionFunction) = when(complete) {
        is DatumFilterCompletionFunction -> complete.filter
        else -> throw UnsupportedOperationException("We only support DatumFilterCompletionFunction for now.")
    }

    /** Wraps a compute object for a team as a completion filter. */
    class DatumFilterCompletionFunction(var filter: DatumFilter) : CompletionFunction {
        override fun invoke(s: RggState) = filter(s.groundTruthAsDatum())
    }

    //endregion

    //region LOOKUPS

    fun team(id: String) = teams.firstOrNull { it.id == id }
    fun node(id: String) = board.node(id)
    fun vector(id: String) = board.vector(id)

    //endregion

}

