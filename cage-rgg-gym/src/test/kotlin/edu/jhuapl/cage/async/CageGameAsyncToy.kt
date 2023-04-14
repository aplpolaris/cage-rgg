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

import edu.jhuapl.cage.CageGameToy
import edu.jhuapl.cage.CagePlayer

/**
 * Toy implementation of CAGE game for testing, using asynchronous players. All parts of the core game definition are
 * the same. The asynchronous portion of the logic happens with the player components, which use asynchronous messaging
 * to receive and respond to messages from the core game.
 */
class CageGameAsyncToy(messageService: CageMessageService): CageGameToy(
    listOf(CagePlayerRemoteAsyncMessaging("A", messageService), CagePlayerRemoteAsyncMessaging("B", messageService))) {

    override fun getPolicyFor(current: CagePlayer) = (current as CagePlayerRemoteAsync).getPolicy()

}
