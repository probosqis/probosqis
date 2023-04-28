/*
 * Copyright 2023 wcaokaze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wcaokaze.probosqis.page.compose

import com.wcaokaze.probosqis.cache.core.WritableCache
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ElementAsCacheTest {
   @Test
   fun getValue() {
      val origin = WritableCache(buildJsonObject {
         put("key", JsonPrimitive(42))
      })

      val elementCache = origin
         .elementAsWritableCache("key", Int.serializer()) { fail() }

      assertEquals(42, elementCache.value)

      origin.value = buildJsonObject {
         put("key", JsonPrimitive(0))
      }

      assertEquals(0, elementCache.value)
   }
}
