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

package com.wcaokaze.probosqis.page.preview

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import com.wcaokaze.probosqis.page.PPage
import com.wcaokaze.probosqis.page.PPageComposable
import com.wcaokaze.probosqis.page.PPageState
import com.wcaokaze.probosqis.page.asCombinedPageComposable
import com.wcaokaze.probosqis.pagedeck.preview.PagePreview

@Composable
fun <P : PPage, S : PPageState<P>> PPagePreview(
   page: P,
   pageComposable: PPageComposable<P, S>,
   windowInsets: WindowInsets = WindowInsets(0, 0, 0, 0)
) {
   PagePreview(
      page, pageComposable.asCombinedPageComposable(), windowInsets
   )
}
