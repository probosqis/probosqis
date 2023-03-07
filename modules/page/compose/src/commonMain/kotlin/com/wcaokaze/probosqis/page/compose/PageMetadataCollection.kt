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

package com.wcaokaze.probosqis.page.compose

import androidx.compose.runtime.Stable
import com.wcaokaze.probosqis.page.core.Page

@Stable
internal class PageMetadataCollection(allMetadata: List<PageMetadata<*>>) {
   private val map = buildMap {
      for (m in allMetadata) {
         put(m.pageClass, m)
      }
   }

   @Stable
   operator fun <P : Page> get(page: P): PageMetadata<P>? {
      @Suppress("UNCHECKED_CAST")
      return map[page::class] as PageMetadata<P>?
   }
}
