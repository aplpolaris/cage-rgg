/*-
 * #%L
 * cage-rgg-example-plugin-rest-0.1.2-SNAPSHOT
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
package edu.jhuapl.game.rgg.examples.rest

import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.Entity
import javax.ws.rs.core.MediaType

/** Client for a remote service that checks a combination. */
object CombinationRestClient {
    private const val REST_URI = "http://localhost:8081/combination/"

    var client = ClientBuilder.newClient().target(REST_URI)

    /** Get info from server. */
    fun info() = client.path("info")
        .request(MediaType.APPLICATION_JSON)
        .get(String::class.java)

    /** Send reset command to server. */
    fun reset(id: String): Boolean = client.path("${id}/reset")
        .request(MediaType.APPLICATION_JSON)
        .post(Entity.entity("", MediaType.APPLICATION_JSON), Boolean::class.java)

    /** Send check combo command to server. */
    fun postCheckCombination(id: String, combo: Map<String, Number>): Map<String, Any> = client.path("${id}/check")
        .request(MediaType.APPLICATION_JSON)
        .post(Entity.entity(combo, MediaType.APPLICATION_JSON), mutableMapOf<String, Any>().javaClass)
}
