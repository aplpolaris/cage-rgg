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
import edu.jhuapl.game.rgg.policy.impl.RggConsolePolicy
import edu.jhuapl.game.rgg.policy.impl.RggAlwaysPolicy
import edu.jhuapl.game.rgg.policy.impl.RggNothingPolicy
import edu.jhuapl.game.rgg.policy.impl.RggRandomPolicy
import edu.jhuapl.game.rgg.provider.RggRulesId
import edu.jhuapl.game.rgg.provider.RggRulesProvider
import edu.jhuapl.game.rgg.rules.RggRules
import java.time.Duration

/** Handles command-line configuration of RGG execution. */
internal class RggLocalExecutorConfig(args: List<String>) {

    /** CL options for execution type. */
    internal enum class ExecOptions(val value: String) {
        SINGLE("single"),
        STATS("stats"),
        MONTECARLO("montecarlo")
    }

    /** CL options for policy. */
    internal enum class PolicyOptions(val value: String) {
        NONE("none"),
        RANDOM("random"),
        ALWAYS("always"),
        CONSOLE("console")
    }

    companion object {
        private val ARG_EXECUTE = listOf("-execute", "-x")

        private val ARG_RULES = listOf("-rules", "-r")
        private val ARG_RUN_COUNT = listOf("-run-count", "-rc")

        private val ARG_POLICY = listOf("-policy", "-p")
        private val ARG_POLICY_ALWAYS_ACTION = listOf("-always")

        private val ARG_RANDOM_SEED = listOf("-random-seed")
        private val ARG_TIMEOUT_DURATION_SECONDS = listOf("-timeout-seconds")
    }

    var executeOption = ExecOptions.SINGLE
    var rulesId: String? = null
    var runCount: Int = 10
    var policy = PolicyOptions.NONE
    var policyAlwaysAction: String = ""

    var randomSeed: Long? = null
    var timeout: Duration = Duration.ofSeconds(2L)

    init {
        val argIndices = args.indices.filter { args[it].startsWith("-") }
        val useArgs = argIndices.mapIndexed { i, argIndex ->
            args[argIndex] to args.subList(argIndex, argIndices.getOrElse(i + 1) { args.size })
        }.toMap()

        useArgs.whenFoundHandleValue(ARG_EXECUTE) { executeOption = ExecOptions.valueOf(it.uppercase()) }
        useArgs.whenFoundHandleValue(ARG_RULES) { rulesId = it }
        useArgs.whenFoundHandleValue(ARG_RUN_COUNT) { runCount = it.toInt() }
        useArgs.whenFoundHandleValue(ARG_POLICY) { policy = PolicyOptions.valueOf(it.uppercase()) }
        useArgs.whenFoundHandleValue(ARG_POLICY_ALWAYS_ACTION) { policyAlwaysAction = it }
        useArgs.whenFoundHandleValue(ARG_RANDOM_SEED) { randomSeed = it.toLong() }
        useArgs.whenFoundHandleValue(ARG_TIMEOUT_DURATION_SECONDS) { timeout = Duration.ofSeconds(it.toLong()) }
    }

    val rules: RggRules? by lazy { rulesId?.let { loadRules(it) } }
    val policies: Map<String, RggPolicy>?
        get() = rules?.let { r ->
            r.teams.associate {
                it.id to when (policy) {
                    PolicyOptions.NONE -> RggNothingPolicy()
                    PolicyOptions.RANDOM -> RggRandomPolicy()
                    PolicyOptions.ALWAYS -> RggAlwaysPolicy(policyAlwaysAction, mapOf())
                    PolicyOptions.CONSOLE -> RggConsolePolicy(it.id)
                }
            }
        }

    /** Loads rules by id. */
    private fun loadRules(id: String): RggRules {
        return RggRulesProvider.runtime.createRules(RggRulesId(name = id))
    }

    /** If any of the arguments are present, perform the given operation. */
    private fun Map<String, List<String>>.whenFound(match: List<String>, op: () -> Unit): Boolean {
        val res = match.any { it in this }
        if (res) op()
        return res
    }

    /** If any of the arguments are present, perform the given operation with the argument after it. */
    private fun Map<String, List<String>>.whenFoundHandleValue(match: List<String>, op: (String) -> Unit): Boolean {
        val found = match.any { it in this }
        if (found) {
            var res = get(match.first { it in this })!!.drop(1).joinToString(" ")
            if (res.startsWith('\'') && res.endsWith('\'')) res = res.trim { it == '\'' }
            if (res.startsWith('"') && res.endsWith('"')) res = res.trim { it == '"' }
            op(res)
            return true
        }
        return false
    }
}
