/*
 * Copyright 2025 wcaokaze
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

package com.wcaokaze.probosqis.ext.panoptiqon

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.wcaokaze.probosqis.panoptiqon.Cache
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import com.wcaokaze.probosqis.panoptiqon.compose.asMutableState
import com.wcaokaze.probosqis.panoptiqon.compose.asState
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class MappedCacheTest {
   @get:Rule
   val rule = createComposeRule()

   private class MappedCacheImpl(
      cache: Cache<Int>
   ) : MappedCache<Int, String>(cache) {
      override fun map(value: Int) = value.toString()
   }

   private class MappedWritableCacheImpl(
      cache: WritableCache<Int>
   ) : MappedWritableCache<Int, String>(cache) {
      override fun map(value: Int) = value.toString()
      override fun reverseMap(value: String) = value.toInt()
   }

   @Test
   fun initialValue() {
      val cache = Cache(0)
      val mapped = MappedCacheImpl(cache)
      assertEquals("0", mapped.value)

      val writableCache = WritableCache(0)
      val writableMapped = MappedWritableCacheImpl(writableCache)
      assertEquals("0", writableMapped.value)
   }

   @Test
   fun value_afterOriginRewrote() {
      val cache = WritableCache(0)
      val mapped = MappedCacheImpl(cache.asCache())
      assertEquals("0", mapped.value)
      cache.value = 1
      assertEquals("1", mapped.value)

      val writableCache = WritableCache(0)
      val writableMapped = MappedWritableCacheImpl(writableCache)
      assertEquals("0", writableMapped.value)
      writableCache.value = 1
      assertEquals("1", writableMapped.value)
   }

   @Test
   fun mappedWritableCache_writeToOrigin() {
      val writableCache = WritableCache(0)
      val writableMapped = MappedWritableCacheImpl(writableCache)
      assertEquals(0, writableCache.value)
      writableMapped.value = "1"
      assertEquals(1, writableCache.value)
   }

   @Test
   fun mappedWritableCache_asCache_value() {
      val writableCache = WritableCache(0)
      val writableMapped = MappedWritableCacheImpl(writableCache)
      val mapped = writableMapped.asCache()
      assertEquals("0", mapped.value)
      writableCache.value = 1
      assertEquals("1", mapped.value)
      writableMapped.value = "2"
      assertEquals("2", mapped.value)
   }

   @Test
   fun recomposition() {
      val cache = WritableCache(0)
      val writableCache = WritableCache(0)
      val mapped = MappedCacheImpl(cache.asCache())
      val writableMapped = MappedWritableCacheImpl(writableCache)

      var value: String? = null
      var writableValue: String? = null

      rule.setContent {
         val composeValue by mapped.asState()
         val composeWritableValue by writableMapped.asState()

         LaunchedEffect(composeValue) {
            value = composeValue
         }
         LaunchedEffect(composeWritableValue) {
            writableValue = composeWritableValue
         }
      }

      rule.runOnIdle {
         assertEquals("0", value)
         assertEquals("0", writableValue)
      }

      cache.value = 1

      rule.runOnIdle {
         assertEquals("1", value)
         assertEquals("0", writableValue)
      }

      writableCache.value = 1

      rule.runOnIdle {
         assertEquals("1", value)
         assertEquals("1", writableValue)
      }
   }

   @Test
   fun writeViaComposeState() {
      val cache = WritableCache(0)
      val mapped = MappedWritableCacheImpl(cache)

      rule.setContent {
         var value by mapped.asMutableState()

         Column {
            Text(value)

            Button(
               onClick = {
                  value = "1"
               }
            ) {
               Text("Set value")
            }
         }
      }

      rule.runOnIdle {
         assertEquals(0, cache.value)
         assertEquals("0", mapped.value)
      }

      rule.onNodeWithText("Set value").performClick()

      rule.runOnIdle {
         assertEquals(1, cache.value)
         assertEquals("1", mapped.value)
      }
   }
}
