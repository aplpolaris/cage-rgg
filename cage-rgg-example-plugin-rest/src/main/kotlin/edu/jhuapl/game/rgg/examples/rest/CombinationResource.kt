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

import javax.ws.rs.*
import javax.ws.rs.core.MediaType.APPLICATION_JSON

/** Sample REST resource with reset and check combination functions. */
@Path("combination")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
class CombinationResource {

    @GET
    @Path("info")
    fun info(): String {
        log("info()")
        return this::class.java.simpleName
    }

    @POST @Path("{id}/reset")
    fun reset(@PathParam("id") id: String): Boolean {
        log("reset() $id")
        return true
    }

    @POST @Path("{id}/check")
    fun checkCombination(@PathParam("id") id: String, combo: Map<String, Number>): Map<String, Boolean> {
        log("checkCombination() $id $combo")
        val toCheck = listOf(combo["a"], combo["b"], combo["c"], combo["d"])
        val unlocked = toCheck == listOf(5, 1, 2, 3) || toCheck == listOf(5.0, 1.0, 2.0, 3.0)
        return mutableMapOf("unlock" to unlocked)
    }

    fun log(text: String) = println("[${ANSI_PURPLE}NOTE$ANSI_RESET] $text")

    private val ANSI_RESET = "\u001B[0m"
    private val ANSI_PURPLE = "\u001B[35m"

}
