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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import kotlinx.coroutines.channels.Channel

/** Stub representing a messaging service, where messages are serialized/deserialized. */
class ToyMessagingServiceSerialized : CageMessageService() {
    private val controlChannel = Channel<String>()
    private val controlResponseChannel = Channel<String>()
    private val observeChannel = Channel<String>()
    private val actionChannel = Channel<String>()

    override suspend fun sendControl(message: CageControlMessage) = controlChannel.send(MAPPER.writeValueAsString(message))
    override suspend fun receiveControl() = MAPPER.readValue(controlChannel.receive(), CageControlMessage::class.java)
    override suspend fun sendControlResponse(message: CageControlResponseMessage) = controlResponseChannel.send(MAPPER.writeValueAsString(message))
    override suspend fun receiveControlResponse() = MAPPER.readValue(controlResponseChannel.receive(), CageControlResponseMessage::class.java)
    override suspend fun sendObserve(message: CageObservationMessage) = observeChannel.send(MAPPER.writeValueAsString(message))
    override suspend fun receiveObserve() = MAPPER.readValue(observeChannel.receive(), CageObservationMessage::class.java)
    override suspend fun sendAction(message: CageActionMessage) = actionChannel.send(MAPPER.writeValueAsString(message))
    override suspend fun receiveAction() = MAPPER.readValue(actionChannel.receive(), CageActionMessage::class.java)

    companion object {
        val MAPPER = ObjectMapper()
            .registerModule(KotlinModule())
    }
}
