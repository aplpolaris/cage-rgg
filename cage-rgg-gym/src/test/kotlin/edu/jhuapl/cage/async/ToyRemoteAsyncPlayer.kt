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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Class designed to asynchronously handle player messages. Serves as a proxy for a remote component that interfaces with
 * the core CAGE engine through a messaging service.
 */
class ToyRemoteAsyncPlayer(val playerId: String, val service: CageMessageService) {
    var ready = AtomicBoolean(false)
    var requestCount = AtomicInteger(0)

    fun initListening(scope: CoroutineScope) {
        // handle observation messages
        val observationJob = scope.launch {
            log("Listening for observation message")
            while (true) {
                val message = service.receiveObserve()
                log("Received observation message $message")
                if (ready.get()) {
                    // here the policy converts observation to action
                    val observation = message.observation
                    val count = requestCount.incrementAndGet()
                    val actionId = if (count > 2) "High" else "Low"
                    Thread.sleep((50L..200L).random()) // simulate blocking
                    service.sendAction(CageActionMessage(message.messageId, playerId, actionId))
                }
            }
        }

        // handle control messages
        scope.launch {
            while (true) {
                log("Listening for control message")
                val message = service.receiveControl()
                log("Received control message $message")
                when (message.messageSubType) {
                    CageControlMessage.RESET -> {
                        when {
                            !ready.get() -> {
                                requestCount.set(0)
                                ready.set(true)
                                Thread.sleep((50L..200L).random()) // simulate blocking
                                sendControlSuccess(message.messageId)
                            }
                            else -> sendControlFailure(message.messageId)
                        }
                    }
                    CageControlMessage.FINISH -> {
                        when {
                            ready.get() -> {
                                ready.set(false)
                                Thread.sleep((50L..200L).random()) // simulate blocking
                                sendControlSuccess(message.messageId)
                                observationJob.cancelAndJoin()
                                break
                            }
                            else -> sendControlFailure(message.messageId)
                        }
                    }
                    else -> TODO()
                }
            }
        }
    }

    private suspend fun sendControlSuccess(messageId: String) {
        service.sendControlResponse(CageControlResponseMessage(messageId, CageControlResponseMessage.SUCCESS))
    }

    private suspend fun sendControlFailure(messageId: String) {
        service.sendControlResponse(CageControlResponseMessage(messageId, CageControlResponseMessage.FAILURE))
    }

    private fun log(message: Any?) {
        println("[${Thread.currentThread().name}] $message")
    }
}
