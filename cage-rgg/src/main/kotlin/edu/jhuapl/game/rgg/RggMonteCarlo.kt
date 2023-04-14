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

import com.fasterxml.jackson.module.kotlin.convertValue
import edu.jhuapl.data.parsnip.gen.MonteCarlo
import edu.jhuapl.data.parsnip.gen.patch
import edu.jhuapl.game.rgg.RggLocalExecutor.doGame
import edu.jhuapl.game.rgg.RggLogger.logStatus
import edu.jhuapl.game.rgg.rules.RggRules
import edu.jhuapl.game.rgg.rules.RggTeam
import edu.jhuapl.game.rgg.io.RggMapper
import edu.jhuapl.game.rgg.policy.RggPolicy
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

/** Execution guidelines for performing many randomized simulations of a game. */
class RggMonteCarlo(
        /** Stores the randomized variables. */
        var randomParameters: MonteCarlo = MonteCarlo(),
        /** Stores the rules template as a nested map object. */
        var rulesTemplate: MutableMap<String, Any?> = mutableMapOf()
) {

    /** Get the (fixed) template rules object. */
    internal val rulesTemplateObject
        get() = RggMapper.convertValue<RggRules>(rulesTemplate)

    /** Get a randomized instance of rules, using the template with adjustments from a random object. */
    internal val randomRulesInstance
        get() = rulesTemplate.patch(randomParameters()).let { RggMapper.convertValue<RggRules>(it) }
}

/** Execute a given RGG game a fixed number of times, capturing statistics. */
@ExperimentalTime
fun RggMonteCarlo.runCapturingStatistics(runs: Int, policies: (RggRules, RggTeam) -> RggPolicy): RggStatistics {
    var stats: RggStatistics? = null
    measureTime {
        stats = RggStatistics(rulesTemplateObject)
        with(ResourceGraphGame(randomRulesInstance, policies)) {
            require(this.policies.keys.containsAll(teams)) { "Must specify a policy for each team!" }
            for (i in 1..runs) {
                doGame(this)
                stats!!.log(state, rules)
                if (i % 100 == 0)
                    logStatus<RggMonteCarlo>("Run $i Complete")
            }
        }
        stats!!.printSummaryInfo()
    }.also {
        logStatus<RggMonteCarlo>("Total runtime: ${it.inWholeSeconds} seconds")
    }
    return stats!!
}
