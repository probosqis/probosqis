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

package com.wcaokaze.probosqis.app.pagedeck.preview

import androidx.compose.animation.core.ExperimentalTransitionApi
import androidx.compose.animation.core.createChildTransition
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.wcaokaze.probosqis.app.pagedeck.CombinedPageComposable
import com.wcaokaze.probosqis.app.pagedeck.CombinedPageSwitcherState
import com.wcaokaze.probosqis.app.pagedeck.LazyPageStackState
import com.wcaokaze.probosqis.app.pagedeck.MultiColumnPageDeckState
import com.wcaokaze.probosqis.app.pagedeck.PageContentFooter
import com.wcaokaze.probosqis.app.pagedeck.PageStackState
import com.wcaokaze.probosqis.app.pagedeck.PageTransitionStateImpl
import com.wcaokaze.probosqis.capsiqum.deck.Deck
import com.wcaokaze.probosqis.capsiqum.page.Page
import com.wcaokaze.probosqis.capsiqum.page.PageStack
import com.wcaokaze.probosqis.capsiqum.page.PageId
import com.wcaokaze.probosqis.capsiqum.page.PageState
import com.wcaokaze.probosqis.capsiqum.page.PageStateStore
import com.wcaokaze.probosqis.capsiqum.page.SavedPageState
import com.wcaokaze.probosqis.capsiqum.page.preview.StateSaverBuilder
import com.wcaokaze.probosqis.capsiqum.page.preview.buildPreviewStateSaver
import com.wcaokaze.probosqis.capsiqum.transition.PageTransitionPreview
import com.wcaokaze.probosqis.panoptiqon.WritableCache

enum class PageTransitionPreviewValue {
   Parent,
   Child
}

@Composable
fun <P : Page, C : Page, PS : PageState, CS : PageState> PageTransitionPreview(
   parentPage: P,
   childPage:  C,
   parentPageComposable: CombinedPageComposable<P, PS>,
   childPageComposable:  CombinedPageComposable<C, CS>,
   parentPageStateSaver: StateSaverBuilder.() -> Unit = {},
   childPageStateSaver:  StateSaverBuilder.() -> Unit = {},
   parentPageState: (P, PageState.StateSaver) -> PS = parentPageComposable.pageStateFactory.pageStateFactory,
   childPageState:  (C, PageState.StateSaver) -> CS = childPageComposable .pageStateFactory.pageStateFactory,
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

   val pageStackState = remember {
      PageStackState(
         pageStackId = pageStackCache.value.id,
         pageStackCache,
         deckState
      )
   }

   val parentPageStateFactory = remember {
      parentPageComposable.pageStateFactory.copy(
         pageStateFactory = { page, _ ->
            val stateSaver = buildPreviewStateSaver(parentPageStateSaver, coroutineScope)
            parentPageState(page, stateSaver).apply(parentPageStateModification)
         }
      )
   }

   val childPageStateFactory = remember {
      childPageComposable.pageStateFactory.copy(
         pageStateFactory = { page, _ ->
            val stateSaver = buildPreviewStateSaver(childPageStateSaver, coroutineScope)
            childPageState(page, stateSaver).apply(childPageStateModification)
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

   val pageStateStore = remember {
      PageStateStore(
         listOf(
            parentPageStateFactory,
            childPageStateFactory,
         ),
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
      PageContentFooter(pageStack.head, pageStackState,
         pageComposableSwitcher, pageStateStore,
         backgroundColor = MaterialTheme.colorScheme.surface,
         WindowInsets(0, 0, 0, 0)
      )
   }
}
