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

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.wcaokaze.probosqis.capsiqum.deck.Deck
import com.wcaokaze.probosqis.capsiqum.page.PageId
import com.wcaokaze.probosqis.capsiqum.page.PageStack
import com.wcaokaze.probosqis.capsiqum.page.PageStateStore
import com.wcaokaze.probosqis.capsiqum.page.SavedPageState
import com.wcaokaze.probosqis.error.PErrorListState
import com.wcaokaze.probosqis.ext.compose.layout.MultiDevicePreview
import com.wcaokaze.probosqis.ext.compose.layout.MultiFontScalePreview
import com.wcaokaze.probosqis.ext.compose.layout.MultiLanguagePreview
import com.wcaokaze.probosqis.ext.compose.layout.SafeDrawingWindowInsetsProvider
import com.wcaokaze.probosqis.pagedeck.CombinedPageSwitcherState
import com.wcaokaze.probosqis.pagedeck.LazyPageStackState
import com.wcaokaze.probosqis.pagedeck.MultiColumnPageDeckState
import com.wcaokaze.probosqis.pagedeck.PageStackRepository
import com.wcaokaze.probosqis.pagedeck.SingleColumnPageDeckState
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import com.wcaokaze.probosqis.resources.ProbosqisTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import org.koin.compose.KoinIsolatedContext
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.coroutines.EmptyCoroutineContext

private val allPageComposables = persistentListOf(
   testPageComposable,
)

private val deckCache = run {
   val children = List(4) { pageStackId ->
      val pageStack = PageStack(
         PageStack.Id(pageStackId.toLong()),
         SavedPageState(
            PageId(0L),
            TestPage(0)
         )
      )
      val lazyPageStackState = LazyPageStackState(
         pageStack.id,
         WritableCache(pageStack),
         initialVisibility = true
      )
      Deck.Card(lazyPageStackState)
   } .toImmutableList()

   val rootRow = Deck.Row(children)
   val deck = Deck(rootRow)
   WritableCache(deck)
}

private val koinModule = module {
   single { CombinedPageSwitcherState(allPageComposables) }

   single {
      PageStateStore(
         allPageStateFactories = allPageComposables.map { it.pageStateFactory },
         appCoroutineScope = CoroutineScope(EmptyCoroutineContext)
      )
   }

   single<PageStackRepository> {
      object : PageStackRepository {
         override fun savePageStack(pageStack: PageStack): WritableCache<PageStack>
               = throw NotImplementedError()
         override fun loadPageStack(id: PageStack.Id): WritableCache<PageStack>
               = throw NotImplementedError()
         override fun deleteAllPageStacks()
               = throw NotImplementedError()
      }
   }

   factory {
      MultiColumnPageDeckState(deckCache, pageStackRepository = get())
   }

   factory {
      SingleColumnPageDeckState(deckCache, pageStackRepository = get())
   }

   single {
      PErrorListState(
         errorListCache = WritableCache(emptyList()),
         itemComposables = emptyList()
      )
   }
}

@MultiDevicePreview
@Composable
private fun SingleColumnProbosqisPreview(
   @PreviewParameter(SafeDrawingWindowInsetsProvider::class)
   safeDrawingWindowInsets: WindowInsets
) {
   KoinIsolatedContext(koinApplication { modules(koinModule) }) {
      ProbosqisTheme {
         SingleColumnProbosqis(
            remember { ProbosqisState() },
            safeDrawingWindowInsets = safeDrawingWindowInsets
         )
      }
   }
}

@MultiDevicePreview
@Composable
private fun MultiColumnProbosqisPreview(
   @PreviewParameter(SafeDrawingWindowInsetsProvider::class)
   safeDrawingWindowInsets: WindowInsets
) {
   KoinIsolatedContext(koinApplication { modules(koinModule) }) {
      ProbosqisTheme {
         MultiColumnProbosqis(
            remember { ProbosqisState() },
            safeDrawingWindowInsets = safeDrawingWindowInsets
         )
      }
   }
}

@MultiFontScalePreview
@Composable
private fun ProbosqisFontScalePreview() {
   KoinIsolatedContext(koinApplication { modules(koinModule) }) {
      ProbosqisTheme {
         MultiColumnProbosqis(
            remember { ProbosqisState() }
         )
      }
   }
}

@MultiLanguagePreview
@Composable
private fun ProbosqisLanguagePreview() {
   KoinIsolatedContext(koinApplication { modules(koinModule) }) {
      ProbosqisTheme {
         MultiColumnProbosqis(
            remember { ProbosqisState() }
         )
      }
   }
}
