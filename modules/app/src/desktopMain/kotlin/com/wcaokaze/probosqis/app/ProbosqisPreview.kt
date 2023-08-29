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
import com.wcaokaze.probosqis.cache.core.WritableCache
import com.wcaokaze.probosqis.ext.kotlin.datetime.MockClock
import com.wcaokaze.probosqis.page.PageComposableSwitcher
import com.wcaokaze.probosqis.page.PageStack
import com.wcaokaze.probosqis.page.PageStackBoardRepository
import com.wcaokaze.probosqis.page.pagestackboard.PageStackBoard
import com.wcaokaze.probosqis.page.pagestackboard.PageStackRepository
import kotlinx.collections.immutable.persistentListOf

@Preview
@Composable
private fun ProbosqisPreview() {
   val di = remember {
      object : DI {
         private val clock = MockClock()

         override val pageComposableSwitcher = PageComposableSwitcher(
            allPageComposables = persistentListOf(
               testPageComposable,
            )
         )

         override val pageStackBoardRepository = object : PageStackBoardRepository {
            override fun savePageStackBoard(pageStackBoard: PageStackBoard)
                  = throw NotImplementedError()

            override fun loadPageStackBoard(): WritableCache<PageStackBoard> {
               val pageStack = PageStack(TestPage(0), clock)
               val pageStackCache = WritableCache(pageStack)
               val children = persistentListOf(
                  PageStackBoard.PageStack(pageStackCache),
               )
               val rootRow = PageStackBoard.Row(children)
               val pageStackBoard = PageStackBoard(rootRow)
               return WritableCache(pageStackBoard)
            }
         }

         override val pageStackRepository = object : PageStackRepository {
            override fun savePageStack(pageStack: PageStack): WritableCache<PageStack>
                  = throw NotImplementedError()
            override fun loadPageStack(id: PageStack.Id): WritableCache<PageStack>
                  = throw NotImplementedError()
            override fun deleteAllPageStacks()
                  = throw NotImplementedError()
         }
      }
   }

   Probosqis(di)
}
