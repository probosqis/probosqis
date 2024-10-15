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

package com.wcaokaze.probosqis.pagedeck

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import com.wcaokaze.probosqis.capsiqum.page.Page
import com.wcaokaze.probosqis.capsiqum.page.PageId
import com.wcaokaze.probosqis.capsiqum.page.PageStack
import com.wcaokaze.probosqis.capsiqum.page.PageState
import com.wcaokaze.probosqis.capsiqum.page.PageStateFactory
import com.wcaokaze.probosqis.capsiqum.page.SavedPageState
import com.wcaokaze.probosqis.capsiqum.transition.PageTransition
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import io.mockk.mockk
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.CoroutineScope
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.KoinIsolatedContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class CombinedPageComposableTest {
   @get:Rule
   val rule = createComposeRule()

   @AfterTest
   fun after() {
      stopKoin()
   }

   @Composable
   private fun KoinIsolatedContext(
      pageSwitcherState: CombinedPageSwitcherState,
      coroutineScope: CoroutineScope = rememberCoroutineScope(),
      content: @Composable () -> Unit
   ) {
      val koinApplication = remember {
         koinApplication {
            modules(
               module {
                  single { coroutineScope }
                  single { pageSwitcherState }
               }
            )
         }
      }

      /// XXX: LaunchedEffectでは初回コンポジションで注入が間に合わないので
      //       rememberでやっている
      remember {
         startKoin(koinApplication)
      }

      KoinIsolatedContext(koinApplication, content)
   }

   private class PageA : Page()
   private class PageB : Page()

   private class PageAState : PageState<PageA>()
   private class PageBState : PageState<PageB>()

   @Test
   fun pageComposableCalled() {
      var pageAHeaderComposed = false
      var pageBHeaderComposed = false
      var pageAHeaderActionsComposed = false
      var pageBHeaderActionsComposed = false
      var pageAContentComposed = false
      var pageBContentComposed = false
      var pageAFooterComposed = false
      var pageBFooterComposed = false

      val pageAComposable = CombinedPageComposable<PageA, PageAState>(
         PageStateFactory { _, _ -> PageAState() },
         content = { _, _, _, _ ->
            DisposableEffect(Unit) {
               pageAContentComposed = true
               onDispose {
                  pageAContentComposed = false
               }
            }
         },
         header = { _, _, _ ->
            DisposableEffect(Unit) {
               pageAHeaderComposed = true
               onDispose {
                  pageAHeaderComposed = false
               }
            }
         },
         headerActions = { _, _, _ ->
            DisposableEffect(Unit) {
               pageAHeaderActionsComposed = true
               onDispose {
                  pageAHeaderActionsComposed = false
               }
            }
         },
         footer = { _, _, _ ->
            DisposableEffect(Unit) {
               pageAFooterComposed = true
               onDispose {
                  pageAFooterComposed = false
               }
            }
         },
         outgoingTransitions = persistentMapOf(),
         incomingTransitions = persistentMapOf()
      )

      val pageBComposable = CombinedPageComposable<PageB, PageBState>(
         PageStateFactory { _, _ -> PageBState() },
         content = { _, _, _, _ ->
            DisposableEffect(Unit) {
               pageBContentComposed = true
               onDispose {
                  pageBContentComposed = false
               }
            }
         },
         header = { _, _, _ ->
            DisposableEffect(Unit) {
               pageBHeaderComposed = true
               onDispose {
                  pageBHeaderComposed = false
               }
            }
         },
         headerActions = { _, _, _ ->
            DisposableEffect(Unit) {
               pageBHeaderActionsComposed = true
               onDispose {
                  pageBHeaderActionsComposed = false
               }
            }
         },
         footer = { _, _, _ ->
            DisposableEffect(Unit) {
               pageBFooterComposed = true
               onDispose {
                  pageBFooterComposed = false
               }
            }
         },
         outgoingTransitions = persistentMapOf(),
         incomingTransitions = persistentMapOf()
      )

      lateinit var pageStackState: PPageStackState

      rule.setContent {
         val pageSwitcherState = remember {
            CombinedPageSwitcherState(
               listOf(pageAComposable, pageBComposable)
            )
         }

         KoinIsolatedContext(
            pageSwitcherState = pageSwitcherState
         ) {
            pageStackState = remember {
               val savedPageState = SavedPageState(PageId(0L), PageA())
               val pageStack = PageStack(PageStack.Id(0L), savedPageState)
               PPageStackState(
                  PageStack.Id(0L),
                  WritableCache(pageStack),
                  pageDeckState = mockk()
               )
            }

            val transitionState = remember {
               PageTransitionStateImpl(pageSwitcherState)
            }

            Column {
               @OptIn(ExperimentalMaterial3Api::class)
               PageStackAppBar(
                  pageStackState, pageSwitcherState,
                  TopAppBarDefaults.topAppBarColors(), WindowInsets(0)
               )

               PageTransition(
                  transitionState,
                  targetState = pageStackState.pageStack
               ) {
                  PageContentFooter(
                     savedPageState = it.head, pageStackState,
                     pageSwitcherState,
                     PageStackColors(
                        background = Color.Transparent,
                        content = Color.Black,
                        activationAnimColor = Color.DarkGray,
                        footer = Color.Transparent,
                        footerContent = Color.Black,
                     ),
                     WindowInsets(0)
                  )
               }
            }
         }
      }

      rule.runOnIdle {
         assertTrue(pageAHeaderComposed)
         assertTrue(pageAHeaderActionsComposed)
         assertTrue(pageAContentComposed)
         assertTrue(pageAFooterComposed)
         assertFalse(pageBHeaderComposed)
         assertFalse(pageBHeaderActionsComposed)
         assertFalse(pageBContentComposed)
         assertFalse(pageBFooterComposed)
      }

      pageStackState.startPage(PageB())

      rule.runOnIdle {
         assertFalse(pageAHeaderComposed)
         assertFalse(pageAHeaderActionsComposed)
         assertFalse(pageAContentComposed)
         assertFalse(pageAFooterComposed)
         assertTrue(pageBHeaderComposed)
         assertTrue(pageBHeaderActionsComposed)
         assertTrue(pageBContentComposed)
         assertTrue(pageBFooterComposed)
      }

      pageStackState.finishPage()

      rule.runOnIdle {
         assertTrue(pageAHeaderComposed)
         assertTrue(pageAHeaderActionsComposed)
         assertTrue(pageAContentComposed)
         assertTrue(pageAFooterComposed)
         assertFalse(pageBHeaderComposed)
         assertFalse(pageBHeaderActionsComposed)
         assertFalse(pageBContentComposed)
         assertFalse(pageBFooterComposed)
      }
   }

   @Test
   fun pageComposableArguments() {
      var contentArgumentPage:       PageA? = null
      var headerArgumentPage:        PageA? = null
      var headerActionsArgumentPage: PageA? = null
      var footerArgumentPage:        PageA? = null
      var contentArgumentPageState:       PageAState? = null
      var headerArgumentPageState:        PageAState? = null
      var headerActionsArgumentPageState: PageAState? = null
      var footerArgumentPageState:        PageAState? = null
      var contentArgumentPageStackState:       PPageStackState? = null
      var headerArgumentPageStackState:        PPageStackState? = null
      var headerActionsArgumentPageStackState: PPageStackState? = null
      var footerArgumentPageStackState:        PPageStackState? = null

      val pageComposable = CombinedPageComposable<PageA, PageAState>(
         PageStateFactory { _, _ -> PageAState() },
         content = { page, pageState, pageStackState, _ ->
            contentArgumentPage = page
            contentArgumentPageState = pageState
            contentArgumentPageStackState = pageStackState
         },
         header = { page, pageState, pageStackState ->
            headerArgumentPage = page
            headerArgumentPageState = pageState
            headerArgumentPageStackState = pageStackState
         },
         headerActions = { page, pageState, pageStackState ->
            headerActionsArgumentPage = page
            headerActionsArgumentPageState = pageState
            headerActionsArgumentPageStackState = pageStackState
         },
         footer = { page, pageState, pageStackState ->
            footerArgumentPage = page
            footerArgumentPageState = pageState
            footerArgumentPageStackState = pageStackState
         },
         outgoingTransitions = persistentMapOf(),
         incomingTransitions = persistentMapOf()
      )

      val page = PageA()

      lateinit var pageStackState: PPageStackState

      rule.setContent {
         val pageSwitcherState = remember {
            CombinedPageSwitcherState(
               listOf(pageComposable)
            )
         }

         KoinIsolatedContext(
            pageSwitcherState = pageSwitcherState
         ) {
            pageStackState = remember {
               val savedPageState = SavedPageState(PageId(0L), page)
               val pageStack = PageStack(PageStack.Id(0L), savedPageState)
               PPageStackState(
                  PageStack.Id(0L),
                  WritableCache(pageStack),
                  pageDeckState = mockk()
               )
            }

            val transitionState = remember {
               PageTransitionStateImpl(pageSwitcherState)
            }

            Column {
               @OptIn(ExperimentalMaterial3Api::class)
               PageStackAppBar(
                  pageStackState, pageSwitcherState,
                  TopAppBarDefaults.topAppBarColors(), WindowInsets(0)
               )

               PageTransition(
                  transitionState,
                  targetState = pageStackState.pageStack
               ) {
                  PageContentFooter(
                     savedPageState = it.head, pageStackState,
                     pageSwitcherState,
                     PageStackColors(
                        background = Color.Transparent,
                        content = Color.Black,
                        activationAnimColor = Color.DarkGray,
                        footer = Color.Transparent,
                        footerContent = Color.Black,
                     ),
                     WindowInsets(0)
                  )
               }
            }
         }
      }

      rule.runOnIdle {
         assertSame(page, contentArgumentPage)
         assertSame(page, headerArgumentPage)
         assertSame(page, headerActionsArgumentPage)
         assertSame(page, footerArgumentPage)

         assertSame(contentArgumentPageState, headerArgumentPageState)
         assertSame(contentArgumentPageState, headerActionsArgumentPageState)
         assertSame(contentArgumentPageState, footerArgumentPageState)

         assertSame(pageStackState, contentArgumentPageStackState)
         assertSame(pageStackState, headerArgumentPageStackState)
         assertSame(pageStackState, headerActionsArgumentPageStackState)
         assertSame(pageStackState, footerArgumentPageStackState)
      }
   }
}
