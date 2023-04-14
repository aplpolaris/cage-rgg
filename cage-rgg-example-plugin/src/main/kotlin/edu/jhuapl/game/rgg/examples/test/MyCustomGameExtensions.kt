/*-
 * #%L
 * cage-rgg-example-plugin-0.1.2-SNAPSHOT
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
package edu.jhuapl.game.rgg.examples.test

import edu.jhuapl.data.parsnip.gen.DimensionConstraint
import edu.jhuapl.game.rgg.policy.RggPolicy
import edu.jhuapl.game.rgg.provider.RggExtensionLookup
import edu.jhuapl.game.rgg.provider.RggExtensions
import edu.jhuapl.game.rgg.rules.RggResultDelegate

class MyCustomGameExtensions: RggExtensionLookup {

    override fun lookupResultDelegate(shortClassName: String): Class<out RggResultDelegate>? {
        // example showing how the RGG yaml file can reference classes in custom locations
        return RggExtensions.packageLookup(shortClassName, "edu.jhuapl.game.rgg.examples.test")
    }

    override fun lookupDimensionConstraint(shortClassName: String): Class<out DimensionConstraint<*>>? = null
    override fun lookupPolicy(shortClassName: String): Class<out RggPolicy>? = null

}
