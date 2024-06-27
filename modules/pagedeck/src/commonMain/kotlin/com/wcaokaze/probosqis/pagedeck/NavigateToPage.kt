/*
 * Copyright 2024 wcaokaze
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

package com.wcaokaze.probosqis.pagedeck

import com.wcaokaze.probosqis.capsiqum.deck.sequence
import com.wcaokaze.probosqis.capsiqum.page.Page
import com.wcaokaze.probosqis.capsiqum.page.PageId

internal suspend fun PageDeckState.navigateToPage(pageId: PageId, fallbackPage: () -> Page) {
   val idx = deck.sequence()
      .indexOfFirst { it.content.pageStackCache.value.head.id == pageId }

   if (idx < 0) {
      TODO()
   } else {
      activate(idx)
   }
}
