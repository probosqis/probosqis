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

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.wcaokaze.probosqis.capsiqum.deck.Deck
import com.wcaokaze.probosqis.capsiqum.page.PageId
import com.wcaokaze.probosqis.capsiqum.page.PageStack
import com.wcaokaze.probosqis.capsiqum.page.SavedPageState
import com.wcaokaze.probosqis.error.PErrorListState
import com.wcaokaze.probosqis.page.PPageSwitcherState
import com.wcaokaze.probosqis.pagedeck.LazyPageStackState
import com.wcaokaze.probosqis.pagedeck.MultiColumnPageDeckState
import com.wcaokaze.probosqis.pagedeck.PageStackRepository
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import com.wcaokaze.probosqis.resources.ProbosqisTheme
import com.wcaokaze.probosqis.testpages.TestPage
import com.wcaokaze.probosqis.testpages.testPageComposable
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.koin.compose.KoinIsolatedContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.koinApplication
import org.koin.dsl.module

private val allPageComposables = persistentListOf(
   testPageComposable,
)

private val koinModule = module {
   single { PPageSwitcherState(allPageComposables) }

   factory {
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

      MultiColumnPageDeckState(
         WritableCache(deck),
         pageStackRepository = object : PageStackRepository {
            override fun savePageStack(pageStack: PageStack): WritableCache<PageStack>
                  = throw NotImplementedError()
            override fun loadPageStack(id: PageStack.Id): WritableCache<PageStack>
                  = throw NotImplementedError()
            override fun deleteAllPageStacks()
                  = throw NotImplementedError()
         }
      )
   }

   single {
      PErrorListState(
         errorListCache = WritableCache(emptyList()),
         itemComposables = emptyList()
      )
   }
}

@Composable
private fun KoinIsolatedContext(content: @Composable () -> Unit) {
   val koinApplication = koinApplication {
      modules(koinModule)
   }

   remember { // LaunchedEffectではstartKoinが初回コンポジションに間に合わないのでrememberで代用する
      stopKoin() // 他のプレビューによって起動済みのKoinを一旦停止する
      startKoin(koinApplication)
   }

   KoinIsolatedContext(
      koinApplication,
      content
   )
}

@Preview
@Composable
private fun ProbosqisPreview() {
   KoinIsolatedContext {
      ProbosqisTheme {
         MultiColumnProbosqis(
            remember { ProbosqisState() }
         )
      }
   }
}
