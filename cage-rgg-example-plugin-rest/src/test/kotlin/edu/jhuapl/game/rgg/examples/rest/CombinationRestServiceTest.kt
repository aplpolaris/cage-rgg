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

import io.netty.handler.logging.LoggingHandler
import org.glassfish.jersey.netty.httpserver.NettyHttpContainerProvider
import org.glassfish.jersey.server.ResourceConfig
import java.net.URI
import javax.ws.rs.core.Application

/** Application running REST service. */
class CombinationServiceApplication: Application() {
    override fun getClasses() = setOf(CombinationResource::class.java)
}

/** Sets up a web service for testing. */
object CombinationNettyServerForTesting {
    @JvmStatic
    fun main(args: Array<String>) {
        val resourceConfig = ResourceConfig.forApplication(CombinationServiceApplication())
        val server = NettyHttpContainerProvider.createHttp2Server(URI.create("http://localhost:8081/"), resourceConfig, null)
        server.pipeline().addLast("logger", LoggingHandler())
        Runtime.getRuntime().addShutdownHook(Thread { server.close() })
    }
}
