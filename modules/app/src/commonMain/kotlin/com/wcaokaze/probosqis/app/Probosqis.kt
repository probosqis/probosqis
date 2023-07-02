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

package com.wcaokaze.probosqis.app

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.cache.core.WritableCache
import com.wcaokaze.probosqis.ext.compose.layout.safeDrawing
import com.wcaokaze.probosqis.ext.kotlin.datetime.BehindClock
import com.wcaokaze.probosqis.page.PageStack
import com.wcaokaze.probosqis.page.PageStackBoard
import com.wcaokaze.probosqis.page.PageStackBoardRepository
import com.wcaokaze.probosqis.resources.ProbosqisTheme
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun Probosqis(
   di: DI,
   safeDrawingWindowInsets: WindowInsets = WindowInsets.safeDrawing
) {
   ProbosqisTheme {
      MaterialTheme(colorScheme()) {
         BoxWithConstraints {
            if (maxWidth < 512.dp) {
               SingleColumnProbosqis(di, safeDrawingWindowInsets)
            } else {
               MultiColumnProbosqis(di, safeDrawingWindowInsets)
            }
         }
      }
   }
}

@Composable
expect fun colorScheme(): ColorScheme

internal fun loadPageStackBoardOrDefault(
   pageStackBoardRepository: PageStackBoardRepository
): WritableCache<PageStackBoard> {
   return try {
      pageStackBoardRepository.loadPageStackBoard()
   } catch (e: Exception) {
      val pageStackBoard = PageStackBoard(
         pageStacks = createDefaultPageStacks()
      )
      pageStackBoardRepository.savePageStackBoard(pageStackBoard)
   }
}

private fun createDefaultPageStacks(): List<PageStack> {
   return persistentListOf(
      PageStack(TestPage(0), BehindClock(Duration.ZERO)),
      PageStack(TestPage(1), BehindClock(1.milliseconds)),
   )
}
