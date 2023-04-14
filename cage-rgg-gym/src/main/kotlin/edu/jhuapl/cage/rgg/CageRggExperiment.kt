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

import edu.jhuapl.cage.CageGameComponent
import edu.jhuapl.cage.exec.CageExecutionOptions
import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.game.rgg.provider.RggExtensions
import edu.jhuapl.game.rgg.provider.RggRulesId
import edu.jhuapl.game.rgg.provider.RggRulesProvider

/** Encapsulates an RGG experiment for CAGE, including references to rules, policies, components, and execution options. */
data class CageRggExperiment(
    /** Unique identifier for rules. */
    var id: RggRulesId,
    /** Teams and associated policies. */
    val policies: Map<String, ParameterizedPolicy>,
    /** Execution options. */
    var options: CageExecutionOptions
) {

    /** Creates game from experiment parameters. */
    fun createGame(): CageGameRgg {
        val rules = RggRulesProvider.runtime.createRules(id)
        val lookupPolicies = policies.mapValues { (_, p) -> RggExtensions.createPolicy(p.id, p.parameters) }
        return CageGameRgg(rules, lookupPolicies)
    }

    fun components(game: CageGameRgg): List<CageGameComponent> {
        // TODO - pluggable support for more games??
        return listOf(game)
    }

}

/** Associate teams to policy instances with parameters. */
class ParameterizedPolicy(
    var id: String,
    var parameters: Datum
)
