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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.wcaokaze.probosqis.capsiqum.PageStack
import com.wcaokaze.probosqis.capsiqum.PageStackBoard
import com.wcaokaze.probosqis.capsiqum.PageStackBoardRepository
import com.wcaokaze.probosqis.capsiqum.PageStackRepository
import com.wcaokaze.probosqis.ext.compose.layout.MultiDevicePreview
import com.wcaokaze.probosqis.ext.compose.layout.MultiFontScalePreview
import com.wcaokaze.probosqis.ext.compose.layout.MultiLanguagePreview
import com.wcaokaze.probosqis.ext.compose.layout.SafeDrawingWindowInsetsProvider
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
private fun rememberPreviewProbosqisState(): ProbosqisState {
   val allPageComposables = persistentListOf(
      testPageComposable,
   )

   val pageStackBoardRepository = object : PageStackBoardRepository {
      override fun savePageStackBoard(pageStackBoard: PageStackBoard)
            = throw NotImplementedError()

      override fun loadPageStackBoard(): WritableCache<PageStackBoard> {
         val children = List(4) { pageStackId ->
            val pageStack = PageStack(
               PageStack.Id(pageStackId.toLong()),
               PageStack.SavedPageState(
                  PageStack.PageId(0L),
                  TestPage(0)
               )
            )
            PageStackBoard.PageStack(
               PageStackBoard.PageStackId(pageStackId.toLong()),
               WritableCache(pageStack)
            )
         } .toImmutableList()

         val rootRow = PageStackBoard.Row(children)
         val pageStackBoard = PageStackBoard(rootRow)
         return WritableCache(pageStackBoard)
      }
   }

   val pageStackRepository = object : PageStackRepository {
      override fun savePageStack(pageStack: PageStack): WritableCache<PageStack>
            = throw NotImplementedError()
      override fun loadPageStack(id: PageStack.Id): WritableCache<PageStack>
            = throw NotImplementedError()
      override fun deleteAllPageStacks()
            = throw NotImplementedError()
   }

   val coroutineScope = rememberCoroutineScope()

   return remember {
      ProbosqisState(allPageComposables, pageStackBoardRepository,
         pageStackRepository, coroutineScope)
   }
}

@MultiDevicePreview
@Composable
private fun ProbosqisPreview(
   @PreviewParameter(SafeDrawingWindowInsetsProvider::class)
   safeDrawingWindowInsets: WindowInsets
) {
   Probosqis(rememberPreviewProbosqisState(), safeDrawingWindowInsets)
}

@MultiFontScalePreview
@Composable
private fun ProbosqisFontScalePreview() {
   Probosqis(rememberPreviewProbosqisState())
}

@MultiLanguagePreview
@Composable
private fun ProbosqisLanguagePreview() {
   Probosqis(rememberPreviewProbosqisState())
}
