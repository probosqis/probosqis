/*
 * Copyright 2023-2025 wcaokaze
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

package com.wcaokaze.probosqis.app.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import com.wcaokaze.probosqis.app.pagedeck.CombinedPageSwitcherState
import com.wcaokaze.probosqis.app.pagedeck.LazyPageStackState
import com.wcaokaze.probosqis.app.pagedeck.MultiColumnPageDeckState
import com.wcaokaze.probosqis.app.pagedeck.PageDeck
import com.wcaokaze.probosqis.app.pagedeck.PageDeckRepository
import com.wcaokaze.probosqis.app.pagedeck.PageStackRepository
import com.wcaokaze.probosqis.app.pagedeck.SingleColumnPageDeckState
import com.wcaokaze.probosqis.capsiqum.deck.Deck
import com.wcaokaze.probosqis.capsiqum.page.Page
import com.wcaokaze.probosqis.capsiqum.page.PageId
import com.wcaokaze.probosqis.capsiqum.page.PageStack
import com.wcaokaze.probosqis.capsiqum.page.SavedPageState
import com.wcaokaze.probosqis.foundation.credential.CredentialRepository
import com.wcaokaze.probosqis.foundation.error.PErrorListState
import com.wcaokaze.probosqis.mastodon.entity.Token
import com.wcaokaze.probosqis.mastodon.ui.auth.urlinput.UrlInputPage
import com.wcaokaze.probosqis.mastodon.ui.timeline.home.HomeTimelinePage
import com.wcaokaze.probosqis.panoptiqon.Cache
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.koin.compose.KoinIsolatedContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import java.io.IOException
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame

@RunWith(JUnit4::class)
class ProbosqisComposeTest {
   @get:Rule
   val rule = createComposeRule()

   @AfterTest
   fun after() {
      stopKoin()
   }

   @Composable
   private fun KoinIsolatedContext(
      content: @Composable () -> Unit
   ) {
      val koinApplication = remember {
         koinApplication {
            modules(
               module {
                  single { CombinedPageSwitcherState(allPageComposables = emptyList()) }

                  single {
                     MultiColumnPageDeckState(
                        WritableCache(PageDeck()),
                        pageStackRepository = mockk()
                     )
                  }

                  single {
                     SingleColumnPageDeckState(
                        WritableCache(PageDeck()),
                        pageStackRepository = mockk()
                     )
                  }

                  single {
                     PErrorListState(
                        errorListCache = WritableCache(emptyList()),
                        itemComposables = emptyList()
                     )
                  }
               }
            )
         }
      }

      LaunchedEffect(Unit) {
         startKoin(koinApplication)
      }

      KoinIsolatedContext(koinApplication, content)
   }

   @Test
   fun loadPageDeck() {
      class PageImpl : Page()

      val pageDeck = PageDeck(
         Deck.Card(
            LazyPageStackState(
               PageStack.Id(0L),
               WritableCache(PageStack(
                  PageStack.Id(0L),
                  SavedPageState(
                     PageId(0L),
                     PageImpl()
                  )
               )),
               initialVisibility = false
            )
         )
      )

      val pageDeckRepository = mockk<PageDeckRepository> {
         every { loadPageDeck() } returns WritableCache(pageDeck)
      }

      val loadedCache = loadPageDeckOrDefault(
         pageDeckRepository,
         pageStackRepository = mockk(),
         credentialRepository = mockk()
      )

      assertSame(pageDeck, loadedCache.value)
   }

   @Test
   fun loadPageDeck_defaultIfLoadFailed() {
      val pageDeckRepository = mockk<PageDeckRepository> {
         every { loadPageDeck() } throws IOException()
         every { savePageDeck(any()) } answers { WritableCache(firstArg()) }
      }

      val pageStackRepository = mockk<PageStackRepository> {
         every { savePageStack(any()) } answers { WritableCache(firstArg()) }
         every { deleteAllPageStacks() } returns Unit
      }

      val credentialRepository = mockk<CredentialRepository> {
         every { loadAllCredentials() } returns emptyList()
      }

      loadPageDeckOrDefault(
         pageDeckRepository, pageStackRepository, credentialRepository
      )

      verify { pageStackRepository.deleteAllPageStacks() }
      verify(exactly = 1) { pageStackRepository.savePageStack(any()) }
   }

   @Test
   fun loadPageDeck_defaultIfLoadEmpty() {
      val pageDeckRepository = mockk<PageDeckRepository> {
         every { loadPageDeck() } returns WritableCache(PageDeck())
      }

      val pageStackRepository = mockk<PageStackRepository> {
         every { savePageStack(any()) } answers { WritableCache(firstArg()) }
      }

      val credentialRepository = mockk<CredentialRepository> {
         every { loadAllCredentials() } returns emptyList()
      }

      loadPageDeckOrDefault(
         pageDeckRepository, pageStackRepository, credentialRepository
      )

      verify(exactly = 1) { pageStackRepository.savePageStack(any()) }
   }

   @Test
   fun createDefaultPageDeck_noCredentials() {
      val pageStackRepository = mockk<PageStackRepository> {
         every { savePageStack(any()) } answers { WritableCache(firstArg()) }
      }

      val credentialRepository = mockk<CredentialRepository> {
         every { loadAllCredentials() } returns emptyList()
      }

      val pageDeck = createDefaultPageDeck(pageStackRepository, credentialRepository)

      assertEquals(1, pageDeck.rootRow.childCount)

      val card = assertIs<Deck.Card<*>>(pageDeck.rootRow[0])
      val lazyPageStackState = assertIs<LazyPageStackState>(card.content)

      val pageStack = lazyPageStackState.pageStack
      assertIs<UrlInputPage>(pageStack.head.page)
      assertNull(pageStack.tailOrNull())

      verify { pageStackRepository.savePageStack(pageStack) }
   }

   @Test
   fun createDefaultPageDeck_withCredentials() {
      val mockToken = mockk<Token>()

      val pageStackRepository = mockk<PageStackRepository> {
         every { savePageStack(any()) } answers { WritableCache(firstArg()) }
      }

      val credentialRepository = mockk<CredentialRepository> {
         every { loadAllCredentials() } returns listOf(Cache(mockToken))
      }

      val pageDeck = createDefaultPageDeck(pageStackRepository, credentialRepository)

      assertEquals(1, pageDeck.rootRow.childCount)

      val card = assertIs<Deck.Card<*>>(pageDeck.rootRow[0])
      val lazyPageStackState = assertIs<LazyPageStackState>(card.content)

      val pageStack = lazyPageStackState.pageStack
      val page = assertIs<HomeTimelinePage>(pageStack.head.page)
      assertSame(mockToken, page.token)
      assertNull(pageStack.tailOrNull())

      verify { pageStackRepository.savePageStack(pageStack) }
   }

   @Test
   fun getDeckState_singleColumn() {
      val probosqisState = ProbosqisState()

      rule.setContent {
         KoinIsolatedContext {
            SingleColumnProbosqis(
               probosqisState,
               onRequestCloseWindow = {}
            )
         }
      }

      rule.runOnIdle {
         assertIs<SingleColumnPageDeckState>(probosqisState.pageDeckState)
      }
   }

   @Test
   fun getDeckState_multiColumn() {
      val probosqisState = ProbosqisState()

      rule.setContent {
         KoinIsolatedContext {
            MultiColumnProbosqis(
               probosqisState,
               onRequestCloseWindow = {}
            )
         }
      }

      rule.runOnIdle {
         assertIs<MultiColumnPageDeckState>(probosqisState.pageDeckState)
      }
   }

   @Test
   fun getDeckState_switchingSingleColumnMultiColumn() {
      val probosqisState = ProbosqisState()

      var isMultiColumn by mutableStateOf(false)

      rule.setContent {
         KoinIsolatedContext {
            if (isMultiColumn) {
               MultiColumnProbosqis(
                  probosqisState,
                  onRequestCloseWindow = {}
               )
            } else {
               SingleColumnProbosqis(
                  probosqisState,
                  onRequestCloseWindow = {}
               )
            }
         }
      }

      rule.runOnIdle {
         assertIs<SingleColumnPageDeckState>(probosqisState.pageDeckState)
      }

      isMultiColumn = true

      rule.runOnIdle {
         assertIs<MultiColumnPageDeckState>(probosqisState.pageDeckState)
      }

      isMultiColumn = false

      rule.runOnIdle {
         assertIs<SingleColumnPageDeckState>(probosqisState.pageDeckState)
      }
   }

   @Test
   fun getDeckState_beforeComposition() {
      val probosqisState = ProbosqisState()

      assertFails {
         probosqisState.pageDeckState
      }
   }
}
