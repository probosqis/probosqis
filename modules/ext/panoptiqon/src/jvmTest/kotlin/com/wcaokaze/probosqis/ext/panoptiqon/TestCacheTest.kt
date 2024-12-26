/*
 * Copyright 2024 wcaokaze
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

import com.wcaokaze.probosqis.ext.kotlintest.loadNativeLib
import kotlin.test.Test
import kotlin.test.assertEquals

class TestCacheTest {
   init {
      loadNativeLib()
   }

   @Test
   fun convertJava_toRust() {
      val testCache = TestCache("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")
      `convertJava_toRust$assert`(testCache)
   }

   private external fun `convertJava_toRust$assert`(testCache: TestCache<String>)

   @Test
   fun convertJava_fromRust() {
      val testCache = `convertJava_fromRust$createTestCache`()

      assertEquals(
         TestCache("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."),
         testCache
      )
   }

   private external fun `convertJava_fromRust$createTestCache`(): TestCache<String>
}
