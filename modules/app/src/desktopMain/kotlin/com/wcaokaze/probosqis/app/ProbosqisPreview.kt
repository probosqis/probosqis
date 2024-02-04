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

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.wcaokaze.probosqis.capsiqum.PageStack
import com.wcaokaze.probosqis.capsiqum.PageStackBoard
import com.wcaokaze.probosqis.capsiqum.PageStackBoardRepository
import com.wcaokaze.probosqis.capsiqum.PageStackRepository
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import kotlinx.collections.immutable.persistentListOf

@Preview
@Composable
private fun ProbosqisPreview() {
   val allPageComposables = persistentListOf(
      testPageComposable,
   )

   val pageStackBoardRepository = object : PageStackBoardRepository {
      override fun savePageStackBoard(pageStackBoard: PageStackBoard)
            = throw NotImplementedError()

      override fun loadPageStackBoard(): WritableCache<PageStackBoard> {
         val pageStack = PageStack(
            PageStack.Id(0L),
            PageStack.SavedPageState(
               PageStack.PageId(0L),
               TestPage(0)
            )
         )
         val children = persistentListOf(
            PageStackBoard.PageStack(
               PageStackBoard.PageStackId(0L),
               WritableCache(pageStack)
            ),
         )
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

   val probosqisState = remember {
      ProbosqisState(allPageComposables, pageStackBoardRepository,
         pageStackRepository, coroutineScope)
   }

   Probosqis(probosqisState)
}
