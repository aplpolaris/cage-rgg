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

/** Stub representing a messaging service. */
abstract class CageMessageService {
    /** Send a control message. */
    abstract suspend fun sendControl(message: CageControlMessage)
    /** Retrieve a control message. */
    abstract suspend fun receiveControl(): CageControlMessage

    /** Send a control response message. */
    abstract suspend fun sendControlResponse(message: CageControlResponseMessage)
    /** Retrieve a control response message. */
    abstract suspend fun receiveControlResponse(): CageControlResponseMessage

    /** Send an observe message. */
    abstract suspend fun sendObserve(message: CageObservationMessage)
    /** Receive an observe message. */
    abstract suspend fun receiveObserve(): CageObservationMessage

    /** Send an action message. */
    abstract suspend fun sendAction(message: CageActionMessage)
    /** Receive an action message. */
    abstract suspend fun receiveAction(): CageActionMessage
}
