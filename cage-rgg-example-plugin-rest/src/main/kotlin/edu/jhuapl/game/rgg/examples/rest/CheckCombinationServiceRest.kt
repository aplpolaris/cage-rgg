/*-
 * #%L
 * cage-rgg-example-plugin-rest-0.1.2-SNAPSHOT
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
package edu.jhuapl.game.rgg.examples.rest

import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.game.rgg.rules.RggResultNodeService

/**
 * Basic example of a function that checks specific inputs at one node and generates an output boolean if they match.
 * The parent class takes care of extracting resources from [input] to send to the service, and then mapping the
 * result to [output].
 */
class CheckCombinationServiceRest(val restClient: CombinationRestClient, input: String, output: String)
    : RggResultNodeService(input, output) {

    init {
        // when the class is initialized, check service connection
        println(CombinationRestClient.info())
    }

    override fun getResult(state: Datum): Datum {
        // any transformation to input/output to match what's required of the rest API is done here
        val serviceInput = state.filterValues { it is Number } as Map<String, Number>
        val serviceOutput = CombinationRestClient.postCheckCombination("x", serviceInput)
        return serviceOutput
    }
}
