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
package edu.jhuapl.cage.async

import edu.jhuapl.cage.CageGameComponent
import edu.jhuapl.cage.exec.CageGameExecutor
import kotlinx.coroutines.runBlocking
import org.junit.Test

class CageGameExecutorAsyncIT {

    @Test
    fun testExecuteAsync() {
        testMessageService(ToyMessagingServiceSimple())
    }

    @Test
    fun testExecuteAsyncSerialized() {
        testMessageService(ToyMessagingServiceSerialized())
    }

    private fun testMessageService(service: CageMessageService) {
        runBlocking {
            ToyRemoteAsyncPlayer("A", service).initListening(this)
            ToyRemoteAsyncPlayer("B", service).initListening(this)

            val toy = CageGameAsyncToy(service)
            val result = CageGameExecutor.executeOnce(toy, listOf(toy) + toy.players.map { it as CageGameComponent })
            println(result)
        }
    }

}

