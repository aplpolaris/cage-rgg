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

import com.fasterxml.jackson.module.kotlin.readValue
import edu.jhuapl.cage.exec.CageExecutionOptions
import edu.jhuapl.game.rgg.examples.SampleRggRulesProvider.Companion.APT
import edu.jhuapl.game.rgg.io.RggMapper
import edu.jhuapl.game.rgg.provider.RggRulesId
import edu.jhuapl.util.internal.prettyPrintJsonTest
import edu.jhuapl.util.internal.recycleJsonTest
import org.junit.Test

class CageRggExperimentTest {

    @Test
    fun testSerialize() {
        val experiment = CageRggExperiment(
            RggRulesId(name = APT),
            mapOf("red" to ParameterizedPolicy("RggRandomPolicy", mapOf())),
            CageExecutionOptions()
        )

        experiment.prettyPrintJsonTest()
        experiment.recycleJsonTest()

        // test that still functions after serialization round trip
        val expt2 = RggMapper.readValue<CageRggExperiment>(RggMapper.writeValueAsString(experiment))
        CageRggExecutor.invoke(expt2)
    }

}
