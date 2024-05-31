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

package com.wcaokaze.probosqis.page

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.saveable.Saver
import com.wcaokaze.probosqis.capsiqum.page.Page
import com.wcaokaze.probosqis.capsiqum.page.PageStack
import com.wcaokaze.probosqis.capsiqum.page.PageState
import kotlinx.serialization.KSerializer

abstract class PPageState(
   context: PageStateContext
) : PageState(), PageStateContext by context

interface PageStateContext {
   fun startPage(page: Page)
   fun finishPage()

   fun addColumn(page: Page)
   fun addColumn(pageStack: PageStack)
   fun removeFromDeck()

   fun <T> save(
      key: String,
      serializer: KSerializer<T>,
      init: () -> T
   ): MutableState<T>

   fun <T> save(
      key: String,
      saver: Saver<T, *>,
      init: () -> T
   ): MutableState<T>
}
