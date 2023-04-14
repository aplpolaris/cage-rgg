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
package edu.jhuapl.cage.exec

import edu.jhuapl.cage.*
import org.junit.Test
import kotlin.random.Random

class CageGameExecutorTest {
    @Test
    fun testExecuteLocal() {
        val toy = CageGameLocalToy()
        val result = CageGameExecutor.executeOnce(toy, listOf(toy))
        println(result)
    }

    @Test
    fun testExecuteRemote() {
        val toy = CageGameRemoteToy()
        val result = CageGameExecutor.executeOnce(toy, listOf(toy) + toy.players.map { it as CageGameComponent })
        println(result)
    }
}

/** Toy implementation of CAGE game for testing. */
class CageGameLocalToy: CageGameToy(listOf(CagePlayer("A"), CagePlayer("B"))) {
    override fun getPolicyFor(current: CagePlayer): (CageObservation) -> CageAction = { _ ->
        // for testing, just pick a random action
        CageAction(current.playerId, if (Random.nextBoolean()) "High" else "Low")
    }
}

/** Toy implementation of CAGE game for testing. */
class CageGameRemoteToy: CageGameToy(listOf(CagePlayerRemoteToy("A"), CagePlayerRemoteToy("B"))) {
    override fun getPolicyFor(current: CagePlayer) = (current as CagePlayerRemote).getPolicy()
}

/** A CAGE component that implements a player policy, selecting an action based on an observation. */
class CagePlayerRemoteToy(id: String): CagePlayerRemote(id) {
    var requestCount = 0
    override fun reset(randomSeed: Long?): Boolean {
        requestCount = 0
        return true
    }
    override fun finish() = true
    override fun getPolicy(): (CageObservation) -> CageAction = { _ ->
        CageAction(playerId, if (requestCount > 2) "High" else "Low")
    }
}
