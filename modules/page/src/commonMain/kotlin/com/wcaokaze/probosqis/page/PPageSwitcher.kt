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

import com.wcaokaze.probosqis.pagedeck.CombinedPageComposable
import com.wcaokaze.probosqis.pagedeck.CombinedPageSwitcherState

fun PPageSwitcherState(
   allPageComposables: List<PPageComposable<*, *>>
) = CombinedPageSwitcherState(
   allPageComposables.map { map(it) }
)

private fun <P : PPage, S : PPageState>
      map(pPageComposable: PPageComposable<P, S>): CombinedPageComposable<*, *>
{
   return CombinedPageComposable(
      pPageComposable.pageClass,
      pPageComposable.pageStateClass,
      pPageComposable.pageStateFactory,
      contentComposable = { page, pageState, pageStackState, windowInsets ->
         pPageComposable.contentComposable(page, pageState, windowInsets)
      },
      headerComposable = { page, pageState, pageStackState ->
         pPageComposable.headerComposable(page, pageState)
      },
      headerActionsComposable = pPageComposable.headerActionsComposable?.let { pHeaderActionsComposable ->
         { page, pageState, pageStackState ->
            pHeaderActionsComposable(page, pageState)
         }
      },
      footerComposable = pPageComposable.footerComposable?.let { pFooterComposable ->
         { page, pageState, pageStackState ->
            pFooterComposable(page, pageState)
         }
      },
      pageTransitionSet = pPageComposable.pageTransitionSet
   )
}
