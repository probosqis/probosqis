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

package com.wcaokaze.probosqis.page.pagestackboard

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.coroutineScope

@Stable
internal class PageStackBoardScrollState : ScrollableState {
   private val scrollMutex = MutatorMutex()
   private var isScrolling by mutableStateOf(false)

   override val isScrollInProgress: Boolean
      get() = isScrolling

   var scrollOffset by mutableStateOf(0f)
      private set

   private var maxScrollOffset by mutableStateOf(0f)

   override fun dispatchRawDelta(delta: Float): Float {
      val oldScrollOffset = scrollOffset

      // deltaは指が動いた量。正のとき右に動いたことを表す。
      // scrollOffsetは正のとき「右が見える」状態を表す。
      // よってこの2つは符号が逆であるため、-演算となる
      val newScrollOffset = (oldScrollOffset - delta).coerceIn(0f, maxScrollOffset)

      scrollOffset = newScrollOffset
      return newScrollOffset - oldScrollOffset
   }

   private val scrollScope = object : ScrollScope {
      override fun scrollBy(pixels: Float): Float = dispatchRawDelta(pixels)
   }

   override suspend fun scroll(
      scrollPriority: MutatePriority,
      block: suspend ScrollScope.() -> Unit
   ) {
      coroutineScope {
         scrollMutex.mutateWith(scrollScope, scrollPriority) {
            isScrolling = true
            try {
               block()
            } finally {
               isScrolling = false
            }
         }
      }
   }

   internal fun setMaxScrollOffset(value: Float) {
      maxScrollOffset = value
      scrollOffset = scrollOffset.coerceIn(0f, value)
   }
}
