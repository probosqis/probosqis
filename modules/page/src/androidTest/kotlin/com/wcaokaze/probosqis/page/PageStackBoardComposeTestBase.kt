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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.wcaokaze.probosqis.cache.core.WritableCache
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

abstract class PageStackBoardComposeTestBase {
   protected abstract val pageStackRepository: PageStackRepository

   protected val pageStackBoardTag = "PageStackBoard"

   protected class TestPage(val i: Int) : Page()

   private val testPageComposable = pageComposable<TestPage>(
      content = { page, pageStackState ->
         Column {
            val coroutineScope = rememberCoroutineScope()

            Text(
               "${page.i}",
               modifier = Modifier.fillMaxWidth()
            )

            Button(
               onClick = {
                  coroutineScope.launch {
                     val newPageStack = PageStack(
                        PageStack.Id(pageStackState.pageStack.id.value + 100L),
                        TestPage(page.i + 100)
                     )
                     pageStackState.addColumn(newPageStack)
                  }
               }
            ) {
               Text("Add PageStack ${page.i}")
            }

            Button(
               onClick = {
                  coroutineScope.launch {
                     pageStackState.removeFromBoard()
                  }
               }
            ) {
               Text("Remove PageStack ${page.i}")
            }
         }
      },
      header = { _, _ -> },
      footer = null
   )

   protected val pageComposableSwitcher = PageComposableSwitcher(
      allPageComposables = listOf(
         testPageComposable,
      )
   )

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
      val page = TestPage(i)
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
