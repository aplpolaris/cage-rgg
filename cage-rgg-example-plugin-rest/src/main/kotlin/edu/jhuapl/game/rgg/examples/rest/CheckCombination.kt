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

import edu.jhuapl.data.parsnip.datum.Datum
import edu.jhuapl.game.rgg.rules.RggResultServiceDelegate

/**
 * Result delegate for check combination. Any parameters that should be configurable from within the YAML specification file
 * can be provided as constructor parameters here. This example uses the [CheckCombinationService] locally by invoking
 * the check method directly.
 */
class CheckCombination(input: String, output: String)
    : RggResultServiceDelegate<Datum>(CheckCombinationService(input, output))
