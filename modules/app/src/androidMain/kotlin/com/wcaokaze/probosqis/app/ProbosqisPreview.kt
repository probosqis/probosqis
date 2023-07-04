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

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.wcaokaze.probosqis.cache.core.WritableCache
import com.wcaokaze.probosqis.ext.compose.layout.MultiDevicePreview
import com.wcaokaze.probosqis.ext.compose.layout.MultiFontScalePreview
import com.wcaokaze.probosqis.ext.compose.layout.MultiLanguagePreview
import com.wcaokaze.probosqis.ext.compose.layout.SafeDrawingWindowInsetsProvider
import com.wcaokaze.probosqis.ext.kotlin.datetime.MockClock
import com.wcaokaze.probosqis.page.*
import kotlinx.collections.immutable.persistentListOf

private object PreviewDI : DI {
   private val clock = MockClock()

   override val pageComposableSwitcher = PageComposableSwitcher(
      allPageComposables = persistentListOf(
         testPageComposable,
      )
   )

   override val pageStackBoardRepository = object : PageStackBoardRepository {
      override fun savePageStackBoard(pageStackBoard: PageStackBoard)
            = throw NotImplementedError()

      override fun loadPageStackBoard() = WritableCache(
         PageStackBoard(
            pageStacks = persistentListOf(
               PageStack(TestPage(0), clock),
               PageStack(TestPage(1), clock),
               PageStack(TestPage(2), clock),
               PageStack(TestPage(3), clock),
            )
         )
      )
   }
}

@MultiDevicePreview
@Composable
private fun ProbosqisPreview(
   @PreviewParameter(SafeDrawingWindowInsetsProvider::class)
   safeDrawingWindowInsets: WindowInsets
) {
   val di = remember { PreviewDI }
   Probosqis(di, safeDrawingWindowInsets)
}

@MultiFontScalePreview
@Composable
private fun ProbosqisFontScalePreview() {
   val di = remember { PreviewDI }
   Probosqis(di)
}

@MultiLanguagePreview
@Composable
private fun ProbosqisLanguagePreview() {
   val di = remember { PreviewDI }
   Probosqis(di)
}
