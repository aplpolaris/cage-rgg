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
package edu.jhuapl.cage.exec

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.Duration

/** Options used for game execution. */
data class CageExecutionOptions(var randomSeed: Long? = null, var timeoutMillis: Long = 2000) {
    @get:JsonIgnore
    val timeout: Duration
        get() = Duration.ofMillis(timeoutMillis)
}