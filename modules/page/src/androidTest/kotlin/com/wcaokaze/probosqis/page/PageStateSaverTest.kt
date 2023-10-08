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

package com.wcaokaze.probosqis.page

import androidx.compose.ui.test.junit4.createComposeRule
import com.wcaokaze.probosqis.cache.core.WritableCache
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.fail

@RunWith(RobolectricTestRunner::class)
class PageStateSaverTest {
   @get:Rule
   val rule = createComposeRule()

   @Test
   fun getValue() {
      val cache = WritableCache(buildJsonObject {
         put("key", JsonPrimitive(42))
      })

      val saver = PageState.StateSaver(cache)
      val savedState = saver.save("key", Int.serializer()) { fail() }

      assertEquals(42, savedState.value)

      cache.value = buildJsonObject {
         put("key", JsonPrimitive(0))
      }

      assertEquals(0, savedState.value)
   }

   @Test
   fun initialize() {
      val cache = WritableCache(buildJsonObject {})

      val saver = PageState.StateSaver(cache)
      val savedState = saver.save("key", Int.serializer()) { 42 }

      assertEquals(1, cache.value.size)
      cache.value["key"].let {
         assertNotNull(it)
         assertIs<JsonPrimitive>(it)
         assertEquals(42, it.int)
      }

      assertEquals(42, savedState.value)

      cache.value = buildJsonObject {}
      assertFalse(cache.value.containsKey("key"))
      assertEquals(42, savedState.value)
      cache.value["key"].let {
         assertNotNull(it)
         assertIs<JsonPrimitive>(it)
         assertEquals(42, it.int)
      }

      cache.value = buildJsonObject {
         put("key", JsonPrimitive("Lorem ipsum"))
      }
      assertEquals(42, savedState.value)
      cache.value["key"].let {
         assertNotNull(it)
         assertIs<JsonPrimitive>(it)
         assertEquals(42, it.int)
      }
   }

   @Test
   fun setValue() {
      val cache = WritableCache(buildJsonObject {
         put("key", JsonPrimitive(3))
      })

      val saver = PageState.StateSaver(cache)
      val savedState = saver.save("key", Int.serializer()) { fail() }

      savedState.value = 42

      cache.value["key"].let {
         assertNotNull(it)
         assertIs<JsonPrimitive>(it)
         assertEquals(42, it.int)
      }
      assertEquals(42, savedState.value)
   }

   @Test
   fun nullable() {
      val cache = WritableCache(buildJsonObject {})
      val saver = PageState.StateSaver(cache)

      val savedState = saver.save("key", Int.serializer().nullable) { 42 }
      assertEquals(42, savedState.value)
      cache.value["key"].let {
         assertNotNull(it)
         assertIs<JsonPrimitive>(it)
         assertEquals(42, it.int)
      }

      savedState.value = null
      assertEquals(null, savedState.value)
      cache.value["key"].let {
         assertNotNull(it)
         assertIs<JsonNull>(it)
      }
   }
}
