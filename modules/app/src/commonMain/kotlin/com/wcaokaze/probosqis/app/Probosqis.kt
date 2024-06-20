/*
 * Copyright 2023-2024 wcaokaze
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

import androidx.compose.runtime.Stable
import com.wcaokaze.probosqis.error.PError
import com.wcaokaze.probosqis.error.PErrorListRepository
import com.wcaokaze.probosqis.pagedeck.PageDeckState
import com.wcaokaze.probosqis.panoptiqon.WritableCache

@Stable
class ProbosqisState {
   private var _pageDeckState: PageDeckState? = null
   var pageDeckState: PageDeckState
      get() {
         return _pageDeckState ?: throw IllegalStateException(
            "attempt to get pageDeckState before the first Composition")
      }
      internal set(value) {
         _pageDeckState = value
      }
}

fun loadErrorListOrDefault(
   errorListRepository: PErrorListRepository
): WritableCache<List<PError>> {
   return try {
      errorListRepository.loadErrorList()
   } catch (e: Exception) {
      errorListRepository.saveErrorList(emptyList())
   }
}
