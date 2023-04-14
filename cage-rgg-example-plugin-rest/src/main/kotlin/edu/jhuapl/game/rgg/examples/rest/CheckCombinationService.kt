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

/** Basic example of a function that checks specific inputs at one node and generates an output boolean if they match. */
class CheckCombinationService(input: String, output: String): RggResultNodeService(input, output) {
    override fun getResult(state: Datum): Datum {
        return CombinationResource().checkCombination("x", state.filterValues { it is Number } as Map<String, Number>)
    }
}
