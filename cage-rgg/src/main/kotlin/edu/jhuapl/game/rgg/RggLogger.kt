/*-
 * #%L
 * cage-rgg-0.1.2-SNAPSHOT
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
package edu.jhuapl.game.rgg

import edu.jhuapl.utilkt.core.loggerFor
import org.checkerframework.checker.units.qual.s
import java.io.PrintWriter
import java.io.StringWriter
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.*
import java.util.logging.Formatter


/** Central logger utilities for Rgg simulations. */
object RggLogger {

    private val LOGGER = Logger.getLogger("edu.jhuapl.game.rgg")

    /** Logger level. */
    var logLevel: Level = Level.INFO
        set(value) {
            field = value
            LOGGER.handlers[0].level = value
        }

    /** Root logger. */
    init {
        LOGGER.apply {
            useParentHandlers = false
            addHandler(ConsoleHandler().apply {
                formatter = object : SimpleFormatter() {
                    private val format = "${ANSI_WHITE}[${ANSI_PURPLE}%1\$tT.%1\$tL %2\$s${ANSI_WHITE}] ${ANSI_GREEN}%3\$s ${ANSI_RESET}%n"

                    override fun format(lr: LogRecord) =
                        String.format(format, Date(lr.millis), lr.level.localizedName, lr.message)
                }
            })
        }
    }

    /** Logs a general status message for a game. */
    inline fun <reified T : Any> logStatus(message: Any?) {
        loggerFor<T>().log(logLevel, message.toString())
    }

    //region CONSOLE TOOLS, FOR LOCAL USE ONLY

    /** Prints to local console. */
    fun printConsole(message: Any?) {
        print(message)
    }

    /** Prints to local console. */
    fun printlnConsole(message: Any?) {
        println(message)
    }

    private const val ANSI_GREEN = "\u001B[32m"
    private const val ANSI_YELLOW = "\u001B[33m"
    private const val ANSI_PURPLE = "\u001B[35m"
    private const val ANSI_CYAN = "\u001B[36m"
    private const val ANSI_WHITE = "\u001B[37m"
    private const val ANSI_RESET = "\u001B[0m"

    fun printConsoleNote(text: String) = println("[${ANSI_PURPLE}NOTE${ANSI_RESET}] $text")

    //endregion

}
