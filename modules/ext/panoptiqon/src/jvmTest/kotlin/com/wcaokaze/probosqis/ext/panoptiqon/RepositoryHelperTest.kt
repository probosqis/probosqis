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
import kotlin.concurrent.thread
import kotlin.test.Test

class RepositoryHolderTest {
   init {
      loadNativeLib()
   }

   @Test
   external fun initializeRepositoryByReading()

   @Test
   external fun initializeRepositoryByWriting()

   @Test
   fun readWriteLock() {
      thread {
         `readWriteLock$thread2`()
      }

      `readWriteLock$thread1`()
   }

   private external fun `readWriteLock$thread1`()
   private external fun `readWriteLock$thread2`()
}
