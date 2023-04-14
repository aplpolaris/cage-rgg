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
package edu.jhuapl.game.rgg.provider

import edu.jhuapl.data.parsnip.gen.BooleanConstraint
import edu.jhuapl.game.rgg.policy.impl.RggConsolePolicy
import edu.jhuapl.game.rgg.policy.impl.RggRandomPolicy
import edu.jhuapl.game.rgg.rules.impl.Resources
import edu.jhuapl.game.rgg.rules.impl.WithAllVectors
import edu.jhuapl.util.internal.shouldBe
import org.junit.Test
import kotlin.test.fail

internal class RggExtensionsTest {

    @Test
    fun testLookupDimensionConstraint() {
        RggExtensions.dimensionConstraintLookup("BooleanConstraint") shouldBe BooleanConstraint::class.java
        RggExtensions.dimensionConstraintLookup("edu.jhuapl.data.parsnip.gen.BooleanConstraint") shouldBe BooleanConstraint::class.java

        try {
            RggExtensions.dimensionConstraintLookup("")
            fail("Should throw exception")
        } catch (x: ClassNotFoundException) {
            // expected
        }
    }

    @Test
    fun testLookupResultDelegate() {
        RggExtensions.resultDelegateLookup("Resources") shouldBe Resources::class.java
        RggExtensions.resultDelegateLookup("edu.jhuapl.game.rgg.rules.impl.WithAllVectors") shouldBe WithAllVectors::class.java

        try {
            RggExtensions.resultDelegateLookup("")
            fail("Should throw exception")
        } catch (x: ClassNotFoundException) {
            // expected
        }
    }

    @Test
    fun testLookupPolicy() {
        RggExtensions.policyLookup("RggRandomPolicy") shouldBe RggRandomPolicy::class.java
        RggExtensions.policyLookup("edu.jhuapl.game.rgg.policy.impl.RggRandomPolicy") shouldBe RggRandomPolicy::class.java

        try {
            RggExtensions.policyLookup("")
            fail("Should throw exception")
        } catch (x: ClassNotFoundException) {
            // expected
        }
    }

    @Test
    fun testCreatePolicy() {
        val testPolicy = RggExtensions.createPolicy("RggConsolePolicy", mapOf("teamId" to "red"))
        testPolicy.javaClass shouldBe RggConsolePolicy::class.java
        (testPolicy as RggConsolePolicy).teamId shouldBe "red"
    }

}
