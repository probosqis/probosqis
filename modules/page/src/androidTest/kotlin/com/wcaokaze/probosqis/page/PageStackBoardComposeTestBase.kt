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

package com.wcaokaze.probosqis.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import com.wcaokaze.probosqis.cache.core.WritableCache
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope

abstract class PageStackBoardComposeTestBase {
   protected abstract val pageStackRepository: PageStackRepository

   protected val pageStackBoardTag = "PageStackBoard"

   protected class TestPage(override val id: Id, val i: Int) : Page()

   protected inline fun <reified P : Page> pageComposableSwitcher(
      noinline pageComposable: @Composable (P, PageStackState) -> Unit,
   ): PageComposableSwitcher {
      val headerComposable: @Composable (Page, PageStackState) -> Unit = { _, _ -> }

      val testPageComposable
            = pageComposable(pageComposable, headerComposable, footer = null)

      return PageComposableSwitcher(
         allPageComposables = listOf(testPageComposable)
      )
   }

   protected val defaultPageComposableSwitcher
         = pageComposableSwitcher<TestPage> { page, pageStackState ->
            Column {
               Text(
                  "${page.i}",
                  modifier = Modifier.fillMaxWidth()
               )

               Button(
                  onClick = {
                     val newPageStack = PageStack(
                        PageStack.Id(pageStackState.pageStack.id.value + 100L),
                        TestPage(
                           Page.Id(page.id.value + 100L),
                           page.i + 100
                        )
                     )
                     pageStackState.addColumn(newPageStack)
                  }
               ) {
                  Text("Add PageStack ${page.i}")
               }

               Button(
                  onClick = {
                     pageStackState.removeFromBoard()
                  }
               ) {
                  Text("Remove PageStack ${page.i}")
               }
            }
         }

   protected fun createPageStackBoard(
      pageStackCount: Int
   ): WritableCache<PageStackBoard> {
      val rootRow = PageStackBoard.Row(
         List(pageStackCount) { createPageStack(it) }.toImmutableList()
      )
      val pageStackBoard = PageStackBoard(rootRow)
      return WritableCache(pageStackBoard)
   }

   protected fun createPageStack(i: Int): PageStackBoard.PageStack {
      val page = TestPage(Page.Id(i.toLong()), i)
      val pageStack = PageStack(
         PageStack.Id(i.toLong()),
         page
      )
      val cache = pageStackRepository.savePageStack(pageStack)
      return PageStackBoard.PageStack(cache)
   }
}

@Stable
internal class RememberedPageStackBoardState<S : PageStackBoardState>(
   val pageStackBoardState: S,
   val coroutineScope: CoroutineScope
) {
   operator fun component1() = pageStackBoardState
   operator fun component2() = coroutineScope
}
