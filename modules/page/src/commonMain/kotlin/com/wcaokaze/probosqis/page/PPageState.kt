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

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wcaokaze.probosqis.capsiqum.page.PageState

@Stable
abstract class PPageState : PageState() {
   @VisibleForTesting
   @Stable
   internal class RC<T> {
      private var referenceCount by mutableIntStateOf(0)
      private var ref: T? by mutableStateOf(null)

      fun get(): T {
         if (referenceCount <= 0) { throw IllegalStateException() }

         @Suppress("UNCHECKED_CAST")
         return ref as T
      }

      fun set(value: T) {
         if (referenceCount <= 0) {
            referenceCount = 1
            ref = value
         } else {
            check(value == ref)
            referenceCount++
         }
      }

      fun release() {
         referenceCount--
      }
   }
}
