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
package edu.jhuapl.cage.service

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import edu.jhuapl.cage.CageAction
import edu.jhuapl.cage.CageActionSpace
import edu.jhuapl.data.parsnip.gen.BooleanConstraint
import edu.jhuapl.data.parsnip.gen.Dimension
import edu.jhuapl.data.parsnip.gen.DimensionList
import edu.jhuapl.game.common.OBSERVABLE
import edu.jhuapl.game.rgg.RggLogger
import edu.jhuapl.game.rgg.provider.RggRulesId
import edu.jhuapl.game.rgg.provider.RggRulesProvider
import edu.jhuapl.game.rgg.rules.RggRules
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import java.util.*
import java.util.logging.Level
import kotlin.system.exitProcess

class GameInfo(
    var uuid: String,
    var name: String,
    var doesServerPlay: String,
    var serverName: String,
    var thirdPartyPlayerId: String,
    var doesServerGoFirst: String,
    var isAdversarial: String,
    var sequenceNumber: Int)

class TurnDetails(
    var name: String,
    var sequenceNumber: Int
)

class ActionDetails(
    var action: String,
    var name: String,
    var sequenceNumber: String
)

class CageGameServer(address: InetSocketAddress) : WebSocketServer(address) {
    // Manages game state and executes moves
    private val server = CageServiceImpl()
    // Used to select the correct game state
    private var env: UUID? = null
    // Team name for the server (if they have a role)
    private var serverPlayerId: String? = null
    // Additional team for record keeping
    private var thirdPartyPlayerId: String? = null
    // Value from client that determines whether the server plays a role
    private var doesServerPlay: String? = null
    // Value from client that determines whether the server should go first, assuming it has a role
    private var doesServerGoFirst: Boolean? = null
    // Assuming it has a role, the server's action space
    private var serverActionSpace: CageActionSpace? = null
    // Determines whether the game is adversarial or not
    private var isAdversarial: Boolean = false
    // Observation space for training
    private var observationSpace: DimensionList? = null
    // Json parser
    private var objectMapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
        .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
    // Function used to pick an action for the server, if the server plays a role
    private fun serverActionFunction(teamId: String) : String {
        val actionSpace = server.actionSpace(env!!, teamId)
        return actionSpace.keys.random()
    }

    /** Given a message, return the sequence number and name of the team performing the action. */
    private fun getSequenceNumberAndTeam(message: String): List<Any> {
        // Parsing the message from the client
        val tmp = message.replace("\"", "").replace("\\", "\"")
        val parsedMessage = objectMapper.readValue(tmp, TurnDetails::class.java)
        return listOf(
            parsedMessage.sequenceNumber,
            parsedMessage.name
        )
    }

    /** Function that generates the server's move if they act as a player */
    private fun serverMove(teamId: String): StepGameResult {
        val action = serverActionFunction(teamId)
        return server.stepGame(env!!, CageAction(teamId, action))
    }

    /** Setting up the game based on the client's first message */
    private fun setupGame(message: String): CageActionSpace {
        // Parsing the message from the client
        val tmp = message.replace("\"", "").replace("\\", "\"")
        val parsedMessage = objectMapper.readValue(tmp, GameInfo::class.java)
        val rulesId = RggRulesId(name=parsedMessage.uuid)
        val clientPlayerId = parsedMessage.name
        doesServerPlay = parsedMessage.doesServerPlay
        serverPlayerId = parsedMessage.serverName
        thirdPartyPlayerId = parsedMessage.thirdPartyPlayerId
        isAdversarial = parsedMessage.isAdversarial.toBoolean()

        if (env == null) {
            // Set up values used for gameplay
            env = server.makeEnvironment(rulesId)
            server.resetGame(env!!)

            // Determine whether the server should go first
            doesServerGoFirst = when (serverPlayerId) {
                null -> null
                else -> {
                    serverActionSpace = server.actionSpace(env!!, serverPlayerId!!)
                    parsedMessage.doesServerGoFirst.toBoolean()
                }
            }
            this.observationSpace = observationSpace(RggRulesProvider.runtime.createRules(rulesId))
        }

        // Give the action space for the client's team
        return server.actionSpace(env!!, clientPlayerId)
    }

    /** Parsing the move message from the client and returning the action to take. */
    private fun parseMove(message: String): CageAction {
        val tmp = message.replace("\"", "").replace("\\", "\"")
        val parsedMessage = objectMapper.readValue(tmp, ActionDetails::class.java)
        val action = parsedMessage.action
        val team = parsedMessage.name
        return CageAction(team, action)
    }

    /** Create the observation space to give to the client */
    private fun observationSpace(rules: RggRules): DimensionList = rules.resourceSpace.flatMap { (node, list) ->
        list.map {
            val name = "${node.id}.${it.name}"
            when(it.name){
                OBSERVABLE -> Dimension(name, BooleanConstraint(defaultValue = false))
                else -> it.copy(name = name)
            }
        }
    }

    override fun onStart() {
        println("Started Server")
    }

    override fun onOpen(conn: WebSocket, handshakedata: ClientHandshake?) {
        println("Successful client connection")
    }

    /** Handle the messages as they arrive. */
    override fun onMessage(conn: WebSocket, message: String) {
        // Handle initial message to reset the environment
        if (message == "Starting Game") {
            env = null
            return
        }

        if (message == "Adversary Observation") {
            conn.send(server.observation(env!!, serverPlayerId!!).observations.toString())
            return
        }

        // Parse data from the client
        val messageData = getSequenceNumberAndTeam(message)
        val sequenceNumber = messageData[0] as Int
        val clientPlayerId = messageData[1] as String

        // Handling the game setup messages
        if (sequenceNumber == 0) {
            val actionSpace = setupGame(message)
            conn.send(actionSpace.keys.toString())

            // If the server has a role and goes first, then they would impact the initial observation for red
            if (doesServerPlay == "true" && doesServerGoFirst == true) {
                serverMove(serverPlayerId!!)
            }

            // Send the observation to the client and increase sequence number
            val observation = server.observation(env!!, clientPlayerId)
            conn.send(observation.observations.toString())
            conn.send(this.observationSpace.toString())
        }

        // Normal game packet
        else if (sequenceNumber > 0) {
            val stepGameResult = server.stepGame(env!!, parseMove(message))
            var stepDone = stepGameResult.done.toString()
            var serverMoveResult: StepGameResult

            if (thirdPartyPlayerId != null && thirdPartyPlayerId != "" && doesServerGoFirst!!) {
                serverMove(thirdPartyPlayerId!!)
            }

            // Server moves if it's supposed to
            if (doesServerPlay == "true") {
                if (!isAdversarial) {
                    serverMoveResult = serverMove(serverPlayerId!!)
                    stepDone = serverMoveResult.done.toString()
                }
                if (thirdPartyPlayerId != null && thirdPartyPlayerId != "") {
                    serverMoveResult = serverMove(thirdPartyPlayerId!!)
                    stepDone = serverMoveResult.done.toString()
                }
            }

            // Send both the current observations, whether the game has finished, and reward
            conn.send(server.observation(env!!, clientPlayerId).observations.toString())
            conn.send(stepDone)
            conn.send(stepGameResult.reward.toString())
        }
    }

    /** Gracefully shutdown the environment and websocket connection. */
    override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
        print("Connection has been closed")
        if (remote) {
            println(" by client.")
            if (env != null) {
                server.shutdownEnvironment(env!!)
            }
            exitProcess(0)
        } else {
            conn.close()
        }
    }

    /** Print errors if one arises. */
    override fun onError(conn: WebSocket, ex: Exception) {
        ex.printStackTrace()
    }

    /** Start the Game Server. */
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            RggLogger.logLevel = Level.OFF
            val s = CageGameServer(InetSocketAddress("0.0.0.0", 8080))
            s.createBuffer()
            s.start()
        }
    }
}
