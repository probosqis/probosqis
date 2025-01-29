/*
 * Copyright 2024-2025 wcaokaze
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

class TestEntity(
   val z: Boolean,
   val b: Byte,
   val s: Short,
   val i: Int,
   val j: Long,
   val f: Float,
   val d: Double,
   val c: Char,
   val str: String,
)

class ConvertJniHelperTest {
   init {
      loadNativeLib()
   }

   @Test
   external fun variantStrsOnInit()

   @Test
   external fun variantJvmIdsAfterCloneIntoJvm()

   @Test
   fun variantJvmIdsAfterGet() {
      val entity = TestEntity(false, 0, 0, 0, 0L, 0.0f, 0.0, 'a', "")
      `variantJvmIdsAfterGet$assert`(entity)
   }

   external fun `variantJvmIdsAfterGet$assert`(entity: TestEntity)

   @Test
   fun cloneIntoJvm() {
      val entity = `cloneIntoJvm$createEntity`()
      assertEquals(false, entity.z)
      assertEquals(0, entity.b)
      assertEquals(1, entity.s)
      assertEquals(2, entity.i)
      assertEquals(3L, entity.j)
      assertEquals(4.5f, entity.f)
      assertEquals(6.75, entity.d)
      assertEquals('8', entity.c)
      assertEquals("9012345", entity.str)
   }

   external fun `cloneIntoJvm$createEntity`(): TestEntity

   @Test
   fun cloneFromJvm() {
      val entity = TestEntity(
         z = false,
         b = 0,
         s = 1,
         i = 2,
         j = 3L,
         f = 4.5f,
         d = 6.75,
         c = '8',
         str = "9012345",
      )
      `cloneFromJvm$assert`(entity)
   }

   external fun `cloneFromJvm$assert`(entity: TestEntity)
}
