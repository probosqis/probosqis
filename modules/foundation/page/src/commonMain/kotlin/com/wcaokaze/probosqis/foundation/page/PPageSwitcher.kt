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

package com.wcaokaze.probosqis.foundation.page

import com.wcaokaze.probosqis.app.pagedeck.CombinedPageComposable
import com.wcaokaze.probosqis.app.pagedeck.CombinedPageSwitcherState

fun PPageSwitcherState(
   allPageComposables: List<PPageComposable<*, *>>
) = CombinedPageSwitcherState(
   allPageComposables.map { it.asCombinedPageComposable() }
)

internal fun <P : PPage, S : PPageState<P>>
      PPageComposable<P, S>.asCombinedPageComposable(): CombinedPageComposable<P, S>
{
   return CombinedPageComposable(
      pageClass,
      pageStateClass,
      pageStateFactory,
      contentComposable = { page, pageState, pageStackState, windowInsets ->
         pageState.inject(pageStackState)
         contentComposable(page, pageState, windowInsets)
      },
      headerComposable = { page, pageState, pageStackState ->
         pageState.inject(pageStackState)
         headerComposable(page, pageState)
      },
      headerActionsComposable = headerActionsComposable?.let { pHeaderActionsComposable ->
         { page, pageState, pageStackState ->
            pageState.inject(pageStackState)
            pHeaderActionsComposable(page, pageState)
         }
      },
      footerComposable = footerComposable?.let { pFooterComposable ->
         { page, pageState, pageStackState ->
            pageState.inject(pageStackState)
            pFooterComposable(page, pageState)
         }
      },
      outgoingTransitions = pageTransitionSet.outgoingTransitions,
      incomingTransitions = pageTransitionSet.incomingTransitions
   )
}
