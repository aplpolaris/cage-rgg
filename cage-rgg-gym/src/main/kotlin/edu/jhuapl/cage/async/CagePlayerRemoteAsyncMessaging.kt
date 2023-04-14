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
import edu.jhuapl.cage.exec.CageExecutionException
import edu.jhuapl.cage.CageObservation
import edu.jhuapl.cage.async.CageControlMessage.Companion.FINISH
import edu.jhuapl.cage.async.CageControlMessage.Companion.RESET
import edu.jhuapl.cage.async.CageControlResponseMessage.Companion.SUCCESS
import edu.jhuapl.data.parsnip.io.ParsnipMapper
import kotlinx.coroutines.runBlocking
import java.util.*

/**
 * A CAGE component that interfaces with a remote player using asynchronous messaging communications.
 * This class prepares and sends/receives messages, whereas [ToyRemoteAsyncPlayer] has the business logic.
 */
class CagePlayerRemoteAsyncMessaging(playerId: String, val service: CageMessageService): CagePlayerRemoteAsync(playerId) {

    override fun postControlResetMessage(): String {
        return runBlocking {
            UUID.randomUUID().toString().also {
                service.sendControl(CageControlMessage(it, RESET, ""))
            }
        }
    }

    override fun postControlFinishMessage(): String {
        return runBlocking {
            UUID.randomUUID().toString().also {
                service.sendControl(CageControlMessage(it, FINISH, ""))
            }
        }
    }

    override fun getControlSuccessMessage(messageId: String): Boolean {
        return runBlocking {
            val message = service.receiveControlResponse()
            if (message.messageId != messageId) throw CageExecutionException("Wrong messageId returned.")
            message.response == SUCCESS
        }
    }

    override fun postObservationMessage(observation: CageObservation): String {
        return runBlocking {
            UUID.randomUUID().toString().also {
                service.sendObserve(CageObservationMessage(it, playerId, encodeObservation(observation)))
            }
        }
    }

    override fun getLatestActionMessage(messageId: String): CageAction {
        return runBlocking {
            val message = service.receiveAction()
            if (message.messageId != messageId) throw CageExecutionException("Wrong messageId returned.")
            CageAction(playerId, message.actionId)
        }
    }

    /** Generates observation content string for the message. */
    private fun encodeObservation(observation: CageObservation): String {
        return ParsnipMapper.writeValueAsString(observation.observations)
    }

}

