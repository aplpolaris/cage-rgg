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
package edu.jhuapl.game.rgg.rules

import edu.jhuapl.game.rgg.io.RggMapper
import edu.jhuapl.util.internal.prettyPrintJsonTest
import edu.jhuapl.util.internal.recycleJsonTest
import org.junit.Test

internal class RggBoardTest {

    @Test
    fun testSerialize() {
        RggBoard().let {
            it.prettyPrintJsonTest()
            it.recycleJsonTest()
        }

        RggBoard().apply {
            nodes.addAll(setOf(RggNode("a"), RggNode("b"), RggNode("c")))
            vectorsWithStringIds = listOf(RggVectorById("a", "b"), RggVectorById("b", "c"))
        }.let {
            it.prettyPrintJsonTest()
            it.recycleJsonTest()
        }
    }

    @Test
    fun testDeserialize() {
        val yaml = """
nodes: [ a, b, c, d, e, f, g, h ]
vectors: [
  { from: a, to: b },
  { from: b, to: c },
  { from: c, to: d },
  { from: d, to: e },
  { from: e, to: f },
  { from: f, to: g },
  { from: a, to: h },
  { from: h, to: g } ]
"""

        val board = RggMapper.readValue(yaml, RggBoard::class.java)
        println(board)
    }

}
