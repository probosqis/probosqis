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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.wcaokaze.probosqis.page.pagestackboard.PageStackBoard
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * 画面遷移した[Page]をStack状に積んだもの。
 *
 * 必ず1つ以上の[Page]を持つ（空のPageStackという概念はない）。
 *
 * UI上はカラムと呼ばれるが列ではない（列は[PageStackColumn]）ので
 * 型としての名称はPageStack
 */
@Serializable
class PageStack private constructor(
   private val pages: List<Page>,
   val createdTime: Instant
) : PageStackBoard.LayoutElement {
   @Immutable
   @JvmInline
   value class Id(val value: Long)

   constructor(page: Page, clock: Clock = Clock.System)
         : this(listOf(page), clock.now())

   /** このPageStackの一番上の[Page] */
   val head: Page get() = pages.last()

   /**
    * @return
    * このPageStackの一番上の[Page]を取り除いたPageStack。
    * このPageStackにPageがひとつしかない場合はnull
    */
   fun tailOrNull(): PageStack? {
      val tailPages = pages.dropLast(1)
      return if (tailPages.isEmpty()) {
         null
      } else {
         PageStack(tailPages, createdTime)
      }
   }

   fun added(page: Page) = PageStack(pages + page, createdTime)
}

@Stable
class PageStackState internal constructor(
   internal val pageStack: PageStack,
   private val pageStackBoardState: PageStackBoardState
) {
   suspend fun addColumn(pageStack: PageStack) {
      val index = pageStackBoardState.pageStackBoard.indexOf(this.pageStack)
      pageStackBoardState.addPageStack(index + 1, pageStack)
   }
}

@Composable
internal fun PageStackContent(
   state: PageStackState,
   pageComposableSwitcher: PageComposableSwitcher
) {
   PageContent(
      page = state.pageStack.head,
      pageComposableSwitcher,
      pageStackState = state
   )
}
