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
package edu.jhuapl.util.internal

import com.fasterxml.jackson.module.kotlin.readValue
import edu.jhuapl.game.rgg.io.RggMapper
import junit.framework.AssertionFailedError
import java.util.*
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** Inline text for equality testing */
infix fun Any?.shouldBe(x: Any?) = assertEquals(x, this)

/** Inline text for content testing */
infix fun <T> Array<T>?.contentShouldBe(x: Array<*>?) {
    val check = when (this) {
        null -> x == null
        else -> x != null && this.contentEquals(x)
    }
    if (!check) {
        throw AssertionFailedError("Expected ${Arrays.toString(x)} but was ${Arrays.toString(this)}")
    }
}

/** Inline text for throwable testing */
infix fun <T : Throwable> (() -> Any?).shouldThrow(exceptionClass: KClass<T>) =
    assertFailsWith(exceptionClass) { this() }

fun Any.printJsonTest() = println(RggMapper.writeValueAsString(this))
fun Any.prettyPrintJsonTest() = println(RggMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this))
fun Any.recycleJsonTest() = RggMapper.testRecycle(this)

fun RggMapper.testRecycle(value: Any) {
    val firstTime = RggMapper.writeValueAsString(value)
    println(firstTime)
    val this2 = RggMapper.readValue<Any>(firstTime)
    val secondTime = RggMapper.writeValueAsString(this2)
    println(secondTime)
    secondTime shouldBe firstTime
}
