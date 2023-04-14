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
package edu.jhuapl.game.rgg.rules.impl

import com.fasterxml.jackson.module.kotlin.readValue
import edu.jhuapl.game.common.ResourceBag
import edu.jhuapl.game.rgg.RggState
import edu.jhuapl.game.rgg.rules.RggBoard
import edu.jhuapl.game.rgg.rules.RggNode
import edu.jhuapl.game.rgg.io.RggMapper
import org.junit.Test
import kotlin.test.assertEquals

internal class ResourcesTest {

    @Test
    fun `test detect change flags`() {
        val res = RggMapper.readValue<Resources>("""
            compute:
              flag1: { Field: n.x1, Gte: 5 }
              flag2: { Field: n.x2, Lte: 2 }
              flag3: { MathOp: { fields: [n.x1, n.x2], operator: GT } }
            put:
              n.alert: { CalculateBoolean: "{flag1} or {flag2} or {flag3}" }
        """)

        val node = RggNode("n")
        val state = RggState().apply {
            nodeResources[node] = ResourceBag().apply {
                set("x1", 4)
                set("x2", 4)
            }
        }
        val board = RggBoard().apply { nodes.add(node) }
        res.resolve(state, board)
        assertEquals(false, state.nodeResources[node]!!.valueOf("alert"))

        state.nodeResources[node]!!["x2"] = 3
        res.resolve(state, board)
        assertEquals(true, state.nodeResources[node]!!.valueOf("alert"))

        state.nodeResources[node]!!["x2"] = 0
        res.resolve(state, board)
        assertEquals(true, state.nodeResources[node]!!.valueOf("alert"))
    }

}
