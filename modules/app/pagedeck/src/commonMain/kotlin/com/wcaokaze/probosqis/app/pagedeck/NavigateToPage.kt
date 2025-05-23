/*
 * Copyright 2024-2025 wcaokaze
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

package com.wcaokaze.probosqis.app.pagedeck

import com.wcaokaze.probosqis.capsiqum.deck.sequence
import com.wcaokaze.probosqis.capsiqum.page.Page
import com.wcaokaze.probosqis.capsiqum.page.PageId
import com.wcaokaze.probosqis.capsiqum.page.PageStack
import com.wcaokaze.probosqis.capsiqum.page.SavedPageState
import kotlin.experimental.ExperimentalTypeInference

suspend fun PageDeckState.navigateToPage(
   pageId: PageId,
   fallbackPage: () -> Page
) {
   navigateToPage(
      pageId,
      fallbackPage = {
         val page = fallbackPage()
         SavedPageState(
            PageId(),
            page
         )
      }
   )
}

@JvmName("navigateToPageWithFallbackSavedPageState")
@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
internal suspend fun PageDeckState.navigateToPage(
   pageId: PageId,
   fallbackPage: () -> SavedPageState
) {
   val idx = deck.sequence()
      .indexOfFirst { it.content.pageStackCache.value.head.id == pageId }

   if (idx < 0) {
      val pageStack = PageStack(fallbackPage())
      addColumn(activeCardIndex + 1, pageStack)
   } else {
      activate(idx)
   }
}
