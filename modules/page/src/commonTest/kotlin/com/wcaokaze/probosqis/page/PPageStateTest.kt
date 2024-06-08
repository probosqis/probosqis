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

package com.wcaokaze.probosqis.page

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class PPageStateTest {
   @Test
   fun rc_get() {
      val rc = PPageState.RC<Int>()
      rc.set(42)
      assertEquals(42, rc.get())
   }

   @Test
   fun rc_getBeforeSetting() {
      val rc = PPageState.RC<Int>()
      assertFails {
         rc.get()
      }
   }

   @Test
   fun rc_getAfterReleased() {
      val rc = PPageState.RC<Int>()
      rc.set(42)
      rc.release()
      assertFails {
         rc.get()
      }
   }

   @Test
   fun rc_setAnotherValueBeforeReleasing() {
      val rc = PPageState.RC<Int>()
      rc.set(42)
      assertFails {
         rc.set(3)
      }
   }

   @Test
   fun rc_resetAfterReleased() {
      val rc = PPageState.RC<Int>()
      rc.set(42)
      rc.release()
      rc.set(3)
      assertEquals(3, rc.get())
   }

   @Test
   fun rc_referenceCount() {
      val rc = PPageState.RC<Int>()
      rc.set(42)
      assertEquals(42, rc.get())
      rc.set(42)
      assertEquals(42, rc.get())
      rc.release()
      assertEquals(42, rc.get())
      rc.release()
      assertFails {
         rc.get()
      }
   }
}
