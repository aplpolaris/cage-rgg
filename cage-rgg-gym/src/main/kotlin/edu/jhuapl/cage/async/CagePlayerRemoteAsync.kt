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

import edu.jhuapl.cage.CageAction
import edu.jhuapl.cage.CageObservation
import edu.jhuapl.cage.CagePlayerRemote
import edu.jhuapl.game.rgg.RggLogger

/**
 * A remote player component of a [CageGame] that is handled via an asynchronous messaging system. Assumes two outgoing
 * channels: (1) control (2) observation, and two incoming channels: (3) control confirmation (4) action.
 *
 * This class frames out the types of messages to be sent and received. Implementations handle serialization and
 * send/receive queues.
 */
abstract class CagePlayerRemoteAsync(playerId: String) : CagePlayerRemote(playerId) {

    override fun getPolicy(): (CageObservation) -> CageAction {
        return { observation ->
            log("Sending observation")
            val messageId = postObservationMessage(observation)
            getLatestActionMessage(messageId)
        }
    }

    override fun reset(randomSeed: Long?): Boolean {
        log("Sending reset")
        val messageId = postControlResetMessage()
        return getControlSuccessMessage(messageId)
    }

    override fun finish(): Boolean {
        log("Sending finish")
        val messageId = postControlFinishMessage()
        return getControlSuccessMessage(messageId)
    }

    /** Posts a reset control message to service. Returns immediately with message id. */
    abstract fun postControlResetMessage(): String

    /** Posts a reset control message to service. Returns immediately with message id. */
    abstract fun postControlFinishMessage(): String

    /** Posts an observation message to service. Returns immediately with message id. */
    abstract fun postObservationMessage(observation: CageObservation): String

    /** Receives a confirmation message from service, matching given message id. Blocks until received, returning true if successful. */
    abstract fun getControlSuccessMessage(messageId: String): Boolean

    /** Returns the most recently received action message. Blocks until received. */
    abstract fun getLatestActionMessage(messageId: String): CageAction

    private fun log(message: Any?) {
        RggLogger.logStatus<CagePlayerRemoteAsync>("[${Thread.currentThread().name}] $message")
    }
}
