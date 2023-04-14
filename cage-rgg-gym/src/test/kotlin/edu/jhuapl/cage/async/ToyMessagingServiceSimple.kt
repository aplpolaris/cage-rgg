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

import kotlinx.coroutines.channels.Channel

/** Stub representing a messaging service, where messages are stored directly. */
class ToyMessagingServiceSimple : CageMessageService() {
    private val controlChannel = Channel<CageControlMessage>()
    private val controlResponseChannel = Channel<CageControlResponseMessage>()
    private val observeChannel = Channel<CageObservationMessage>()
    private val actionChannel = Channel<CageActionMessage>()

    override suspend fun sendControl(message: CageControlMessage) = controlChannel.send(message)
    override suspend fun receiveControl() = controlChannel.receive()
    override suspend fun sendControlResponse(message: CageControlResponseMessage) = controlResponseChannel.send(message)
    override suspend fun receiveControlResponse() = controlResponseChannel.receive()
    override suspend fun sendObserve(message: CageObservationMessage) = observeChannel.send(message)
    override suspend fun receiveObserve() = observeChannel.receive()
    override suspend fun sendAction(message: CageActionMessage) = actionChannel.send(message)
    override suspend fun receiveAction() = actionChannel.receive()
}
