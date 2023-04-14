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

import edu.jhuapl.game.rgg.RggLogger.logStatus
import edu.jhuapl.game.rgg.RggLogger.printlnConsole
import edu.jhuapl.game.rgg.RggLogger.printConsoleNote
import edu.jhuapl.game.rgg.policy.RggPolicy
import edu.jhuapl.game.rgg.rules.RggRules
import edu.jhuapl.game.rgg.rules.RggTeam
import kotlin.time.ExperimentalTime

/** Executes the game loop with the given input strategies. */
object RggLocalExecutor {

    //region MAIN

    @ExperimentalTime
    @JvmStatic
    fun main(args: Array<String>) {
        val config = RggLocalExecutorConfig(args.toList())
        val rules = config.rules

        if (rules == null) {
            printConsoleNote("Must specify rules using -r rules. See full list of options below.")
            printScriptOptions()
        } else {
            printConsoleNote("Executing rules ${config.rulesId} with policy ${config.policy} and execution mode ${config.executeOption}...")
            val teamPolicies = config.policies!!.mapKeys { rules.team(it.key)!! }
            when (config.executeOption) {
                RggLocalExecutorConfig.ExecOptions.SINGLE ->
                    doGame(rules, teamPolicies)
                RggLocalExecutorConfig.ExecOptions.STATS ->
                    rules.runCapturingStatistics(config.runCount, teamPolicies)
                RggLocalExecutorConfig.ExecOptions.MONTECARLO ->
                    TODO("Not supported yet")
            }
        }
    }

    private fun printScriptOptions() {
        printlnConsole("")
        printlnConsole("""
------------------------------------------------------------------------------------------
RULE/POLICY OPTION         ALIAS        DESCRIPTION

-rules                     -r           predefined ruleset id (see SampleRggRulesProvider)
-policy                    -p           "none" (default), "always", "random", or "console"
-always                                 specific action id to use for "always" policy

GENERAL OPTION             ALIAS        DESCRIPTION

-execute                   -x           "single" (default), "stats", or "montecarlo" (TBD)
-run-count                 -rc          number of runs for stats execution (default: 10)
-random-seed                            (future feature)
-timeout-seconds                        (future feature)
------------------------------------------------------------------------------------------
        """.trimIndent())
    }

    //endregion

    /** Execute primary game loop, starting with a set of rules and policies. */
    fun doGame(rules: RggRules, policies: (RggRules, RggTeam) -> RggPolicy) =
        ResourceGraphGame(rules, policies).execute()

    /** Execute primary game loop, starting with a set of rules and policies indexed by team. */
    fun doGame(rules: RggRules, policies: Map<RggTeam, RggPolicy>) =
        ResourceGraphGame(rules, policies).execute()

    /** Execute primary game loop, starting with a game. */
    fun doGame(game: ResourceGraphGame) = game.execute()

    /** Perform main game execution loop. */
    private fun ResourceGraphGame.execute() {
        reset()
        logStatus<RggLocalExecutor>("Completed game setup")
        while (!completed) {
            logStatus<RggLocalExecutor>("$currentTeam")
            val move = selectAction()
            if (move.valid(state)) {
                move.resolve(state)
            } else {
                logStatus<RggLocalExecutor>("  ... invalid action")
            }
            logStatus<RggLocalExecutor>("  ... current reward: ${rules.rewards[currentTeam]?.invoke(state)}")
            nextTeam()
        }
        finish()
        logGameMetrics()
    }

    /** Logs rewards and metrics. */
    fun ResourceGraphGame.logGameMetrics() {
        logStatus<RggLocalExecutor>("Game complete. Final rewards/metrics:")
        val rewards = rules.teams.map { it to rules.rewards[it]!!.invoke(state) }
        logStatus<RggLocalExecutor>(rewards.map { "${it.first.id} = ${it.second}" }.joinToString("\n   - ", prefix = "   - "))
        val metrics = rules.metrics.map { it to state.valueOf(it) }
        logStatus<RggLocalExecutor>(metrics.map { "${it.first} = ${it.second}" }.joinToString("\n   - ", prefix = "   - "))
    }

}
