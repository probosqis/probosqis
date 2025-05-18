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

package com.wcaokaze.probosqis.app.pagedeck.preview

import androidx.compose.animation.core.ExperimentalTransitionApi
import androidx.compose.animation.core.createChildTransition
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.app.pagedeck.CombinedPageComposable
import com.wcaokaze.probosqis.app.pagedeck.CombinedPageSwitcherState
import com.wcaokaze.probosqis.app.pagedeck.LazyPageStackState
import com.wcaokaze.probosqis.app.pagedeck.MultiColumnPageDeckState
import com.wcaokaze.probosqis.app.pagedeck.PPageStackState
import com.wcaokaze.probosqis.app.pagedeck.PageContentFooter
import com.wcaokaze.probosqis.app.pagedeck.PageStackColors
import com.wcaokaze.probosqis.app.pagedeck.PageTransitionStateImpl
import com.wcaokaze.probosqis.capsiqum.deck.Deck
import com.wcaokaze.probosqis.capsiqum.page.Page
import com.wcaokaze.probosqis.capsiqum.page.PageId
import com.wcaokaze.probosqis.capsiqum.page.PageStack
import com.wcaokaze.probosqis.capsiqum.page.PageState
import com.wcaokaze.probosqis.capsiqum.page.SavedPageState
import com.wcaokaze.probosqis.capsiqum.transition.PageTransitionPreview
import com.wcaokaze.probosqis.panoptiqon.WritableCache

enum class PageTransitionPreviewValue {
   Parent,
   Child
}

@Composable
fun <P : Page, C : Page, PS : PageState<P>, CS : PageState<C>> PageTransitionPreview(
   parentPage: P,
   childPage:  C,
   parentPageComposable: CombinedPageComposable<P, PS>,
   childPageComposable:  CombinedPageComposable<C, CS>,
   parentPageState: (P, PageId) -> PS = parentPageComposable.pageStateFactory.pageStateFactory,
   childPageState:  (C, PageId) -> CS = childPageComposable .pageStateFactory.pageStateFactory,
   parentPageStateModification: PS.() -> Unit = {},
   childPageStateModification:  CS.() -> Unit = {},
) {
   val parentSavedPageState = remember {
      SavedPageState(PageId(0L), parentPage)
   }
   val childSavedPageState = remember {
      SavedPageState(PageId(1L), childPage)
   }

   val parentPageStack = remember { PageStack(PageStack.Id(0L), parentSavedPageState) }
   val childPageStack = remember { parentPageStack.added(childSavedPageState) }

   val pageStackCache = remember { WritableCache(childPageStack) }

   val coroutineScope = rememberCoroutineScope()

   val deckState = remember {
      val lazyPageStackState = LazyPageStackState(
         pageStackCache.value.id,
         pageStackCache,
         initialVisibility = true
      )
      val deck = Deck(
         Deck.Card(lazyPageStackState),
      )

      MultiColumnPageDeckState(
         WritableCache(deck),
         PreviewPageStackRepository()
      )
   }

   val parentPageStateFactory = remember {
      parentPageComposable.pageStateFactory.copy(
         pageStateFactory = { page, _ ->
            parentPageState(page, parentSavedPageState.id)
               .apply(parentPageStateModification)
         }
      )
   }

   val childPageStateFactory = remember {
      childPageComposable.pageStateFactory.copy(
         pageStateFactory = { page, _ ->
            childPageState(page, childSavedPageState.id)
               .apply(childPageStateModification)
         }
      )
   }

   val pageComposableSwitcher = remember {
      CombinedPageSwitcherState(
         listOf(
            parentPageComposable.copy(pageStateFactory = parentPageStateFactory),
            childPageComposable .copy(pageStateFactory = childPageStateFactory),
         )
      )
   }

   val pageStackState = remember {
      PPageStackState(
         pageStackId = pageStackCache.value.id,
         pageStackCache,
         deckState,
         allPageStateFactories = pageComposableSwitcher
            .allPageComposables.map { it.pageStateFactory },
         coroutineScope
      )
   }

   val transition = updateTransition(
      PageTransitionPreviewValue.Parent, label = "PageTransition")

   @OptIn(ExperimentalTransitionApi::class)
   val pageStateTransition = transition.createChildTransition {
      when (it) {
         PageTransitionPreviewValue.Parent -> parentPageStack
         PageTransitionPreviewValue.Child -> childPageStack
      }
   }

   val transitionState = remember(pageComposableSwitcher) {
      PageTransitionStateImpl(pageComposableSwitcher)
   }

   PageTransitionPreview(
      transitionState,
      pageStateTransition
   ) { pageStack ->
      PageContentFooter(
         pageStack.head, pageStackState, pageComposableSwitcher,
         PageStackColors(
            background = MaterialTheme.colorScheme.surface,
            content = MaterialTheme.colorScheme.onSurface,
            activationAnimColor = MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.04f),
            footer = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
            footerContent = MaterialTheme.colorScheme.onSurface,
         ),
         WindowInsets(0, 0, 0, 0)
      )
   }
}
