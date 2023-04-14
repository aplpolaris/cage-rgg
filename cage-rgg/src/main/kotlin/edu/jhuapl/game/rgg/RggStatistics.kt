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

import com.google.common.collect.LinkedHashMultiset
import edu.jhuapl.game.rgg.RggLocalExecutor.doGame
import edu.jhuapl.game.rgg.RggLogger.logStatus
import edu.jhuapl.game.rgg.policy.RggPolicy
import edu.jhuapl.game.rgg.rules.RggRules
import edu.jhuapl.game.rgg.rules.RggTeam
import java.util.*
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

/** Captures statistics from multiple runs of an RGG. */
class RggStatistics(val rules: RggRules) {
    val rewardSummaries = rules.teams.associate { it.id to DoubleSummaryStatistics() }
    val metricSummaries = rules.metrics.associateWith { LinkedHashMultiset.create<Any?>() }

    fun log(state: RggState, rules: RggRules) {
        rewardSummaries.forEach { (teamId, stats) ->
            val reward = rules.rewards[rules.team(teamId)]!!.invoke(state).toDouble()
            stats.accept(reward)
        }
        metricSummaries.forEach { (comboId, values) ->
            values += state.valueOf(comboId)
        }
    }

    fun printSummaryInfo() {
        rewardSummaries.forEach { (t, stats) ->
            logStatus<RggStatistics>("Team $t rewards: $stats")
        }
        metricSummaries.forEach { (m, values) ->
            logStatus<RggStatistics>("Metric $m values: $values")
            if (values.elementSet().all { it is Int }) {
                val stats = IntSummaryStatistics().apply { values.forEach { this.accept(it as Int) } }
                logStatus<RggStatistics>("    statistics: $stats")
            } else if (values.elementSet().all { it is Number }) {
                val stats = DoubleSummaryStatistics().apply { values.forEach { this.accept((it as Number).toDouble()) } }
                logStatus<RggStatistics>("    statistics: $stats")
            }
        }
    }
}

/** Execute a given RGG game a fixed number of times, capturing statistics. */
@ExperimentalTime
fun RggRules.runCapturingStatistics(runs: Int, policies: Map<RggTeam, RggPolicy>) =
    runCapturingStatistics(runs) { _, team -> policies[team]!! }

/** Execute a given RGG game a fixed number of times, capturing statistics. */
@ExperimentalTime
fun RggRules.runCapturingStatistics(runs: Int, policies: (RggRules, RggTeam) -> RggPolicy) {
    measureTime {
        with (ResourceGraphGame(this, policies)) {
            val stats = RggStatistics(rules)
            require(this.policies.keys.containsAll(teams)) { "Must specify a policy for each team!" }
            for (i in 1..runs) {
                doGame(this)
                stats.log(state, rules)
                if (i % 100 == 0)
                    logStatus<RggStatistics>("Run $i Complete")
            }
            stats.printSummaryInfo()
        }
    }.also {
        logStatus<RggStatistics>("Total runtime: ${it.inWholeSeconds} seconds")
    }
}
