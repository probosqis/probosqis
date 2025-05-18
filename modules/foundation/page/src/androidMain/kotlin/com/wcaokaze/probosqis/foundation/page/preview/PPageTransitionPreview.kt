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

package com.wcaokaze.probosqis.foundation.page.preview

import androidx.compose.runtime.Composable
import com.wcaokaze.probosqis.app.pagedeck.preview.PageTransitionPreview
import com.wcaokaze.probosqis.capsiqum.page.PageId
import com.wcaokaze.probosqis.foundation.page.PPage
import com.wcaokaze.probosqis.foundation.page.PPageComposable
import com.wcaokaze.probosqis.foundation.page.PPageState
import com.wcaokaze.probosqis.foundation.page.asCombinedPageComposable

@Composable
fun <P : PPage, C : PPage, PS : PPageState<P>, CS : PPageState<C>>
    PPageTransitionPreview(
      parentPage: P,
      childPage:  C,
      parentPageComposable: PPageComposable<P, PS>,
      childPageComposable:  PPageComposable<C, CS>,
      parentPageState: (P, PageId) -> PS
         = parentPageComposable.pageStateFactory.pageStateFactory,
      childPageState:  (C, PageId) -> CS
         = childPageComposable .pageStateFactory.pageStateFactory,
      parentPageStateModification: PS.() -> Unit = {},
      childPageStateModification:  CS.() -> Unit = {},
   )
{
   PageTransitionPreview(
      parentPage, childPage,
      parentPageComposable.asCombinedPageComposable(), childPageComposable.asCombinedPageComposable(),
      parentPageState, childPageState,
      parentPageStateModification, childPageStateModification
   )
}
