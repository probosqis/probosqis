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

package com.wcaokaze.probosqis.pagedeck.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.capsiqum.deck.Deck
import com.wcaokaze.probosqis.capsiqum.page.Page
import com.wcaokaze.probosqis.capsiqum.page.PageId
import com.wcaokaze.probosqis.capsiqum.page.PageStack
import com.wcaokaze.probosqis.capsiqum.page.PageState
import com.wcaokaze.probosqis.capsiqum.page.SavedPageState
import com.wcaokaze.probosqis.capsiqum.page.preview.rememberPreviewStateSaver
import com.wcaokaze.probosqis.pagedeck.CombinedPageComposable
import com.wcaokaze.probosqis.pagedeck.LazyPageStackState
import com.wcaokaze.probosqis.pagedeck.MultiColumnPageDeckState
import com.wcaokaze.probosqis.pagedeck.PPageStackState
import com.wcaokaze.probosqis.pagedeck.PageContent
import com.wcaokaze.probosqis.pagedeck.PageFooter
import com.wcaokaze.probosqis.pagedeck.PageStackAppBar
import com.wcaokaze.probosqis.pagedeck.PageStackRepository
import com.wcaokaze.probosqis.panoptiqon.WritableCache

@Composable
fun <P : Page, S : PageState<P>> PagePreview(
   page: P,
   pageComposable: CombinedPageComposable<P, S>,
   windowInsets: WindowInsets = WindowInsets(0, 0, 0, 0)
) {
   val savedPageState = remember {
      SavedPageState(PageId(0L), page)
   }

   val pageStackCache = remember {
      val pageStack = PageStack(PageStack.Id(0L), savedPageState)
      WritableCache(pageStack)
   }

   val pageDeckState = remember {
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

   val coroutineScope = rememberCoroutineScope()

   val pageStackState = remember {
      PPageStackState(
         pageStackId = pageStackCache.value.id,
         pageStackCache,
         pageDeckState,
         allPageStateFactories = listOf(pageComposable.pageStateFactory),
         coroutineScope
      )
   }

   val stateSaver = rememberPreviewStateSaver {}

   val pageState = remember {
      pageComposable.pageStateFactory
         .createPageState(page, savedPageState.id, coroutineScope, stateSaver)
   }

   Surface(
      shape = MaterialTheme.shapes.large,
      tonalElevation = 3.dp,
      shadowElevation = 4.dp
   ) {
      val footerComposable = pageComposable.footerComposable

      Box {
         Column {
            @OptIn(ExperimentalMaterial3Api::class)
            PageStackAppBar(
               pageComposable,
               page,
               pageState,
               pageStackState,
               colors = TopAppBarDefaults.topAppBarColors(
                  containerColor = MaterialTheme.colorScheme.primaryContainer
               ),
               windowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
            )

            PageContent(
               pageComposable.contentComposable,
               page,
               pageState,
               pageStackState,
               isFooterShown = footerComposable != null,
               contentColor = MaterialTheme.colorScheme.onSurface,
               windowInsets.only(WindowInsetsSides.Horizontal)
            )
         }

         if (footerComposable != null) {
            Box(Modifier.align(Alignment.BottomCenter)) {
               PageFooter(
                  pageComposable.footerComposable,
                  page,
                  pageState,
                  pageStackState,
                  backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                  contentColor = MaterialTheme.colorScheme.onSurface,
                  windowInsets
                     .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
               )
            }
         }
      }
   }
}

internal class PreviewPageStackRepository : PageStackRepository {
   override fun deleteAllPageStacks() {}
   override fun loadPageStack(id: PageStack.Id): WritableCache<PageStack>
         = throw NotImplementedError()
   override fun savePageStack(pageStack: PageStack): WritableCache<PageStack>
         = throw NotImplementedError()
}
