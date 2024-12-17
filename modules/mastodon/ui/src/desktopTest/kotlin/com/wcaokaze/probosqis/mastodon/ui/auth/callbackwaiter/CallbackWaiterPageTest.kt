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

package com.wcaokaze.probosqis.mastodon.ui.auth.callbackwaiter

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.input.TextFieldValue
import com.wcaokaze.probosqis.capsiqum.page.Page
import com.wcaokaze.probosqis.capsiqum.page.PageId
import com.wcaokaze.probosqis.capsiqum.page.PageStack
import com.wcaokaze.probosqis.capsiqum.page.SavedPageState
import com.wcaokaze.probosqis.capsiqum.page.test.rememberTestPageState
import com.wcaokaze.probosqis.mastodon.entity.Application
import com.wcaokaze.probosqis.mastodon.entity.Instance
import com.wcaokaze.probosqis.mastodon.entity.Token
import com.wcaokaze.probosqis.mastodon.repository.AppRepository
import com.wcaokaze.probosqis.mastodon.ui.auth.urlinput.UrlInputPage
import com.wcaokaze.probosqis.page.PPageState
import com.wcaokaze.probosqis.panoptiqon.Cache
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Rule
import org.koin.compose.KoinIsolatedContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import java.time.Month
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNull

class CallbackWaiterPageTest {
   @get:Rule
   val rule = createComposeRule()

   @AfterTest
   fun afterTest() {
      stopKoin()
   }

   @Composable
   private fun rememberPageState(
      page: CallbackWaiterPage = CallbackWaiterPage("https://example.com/"),
      pageStateBase: PPageState.Interface = mockk()
   ): CallbackWaiterPageState {
      val pageState = callbackWaiterPageComposable.pageStateFactory.rememberTestPageState(page)
      pageState.injectTestable(pageStateBase)
      return pageState
   }

   @Composable
   private fun CallbackWaiterPage(
      pageState: CallbackWaiterPageState,
      appRepository: AppRepository = mockk(),
   ) {
      val koinApplication = remember {
         koinApplication {
            modules(
               module {
                  single { appRepository }
               }
            )
         }
      }

      remember {
         startKoin(koinApplication)
      }

      KoinIsolatedContext(koinApplication) {
         callbackWaiterPageComposable.contentComposable(
            pageState.page, pageState, WindowInsets(0)
         )
      }
   }

   private fun pageStack(vararg pages: Page, id: Long = 0L): PageStack {
      var pageStack = PageStack(
         PageStack.Id(id),
         SavedPageState(PageId(0L), pages.first())
      )

      for ((pageId, page) in pages.withIndex().drop(1)) {
         pageStack = pageStack.added(
            SavedPageState(PageId(pageId.toLong()), page)
         )
      }

      return pageStack
   }

   private fun application(instanceBaseUrl: String): Application {
      val instance = Instance(
         instanceBaseUrl,
         version = "0.0.0",
         versionCheckedTime = LocalDateTime(2000, Month.JANUARY, 1, 0, 0).toInstant(TimeZone.UTC)
      )

      return Application(
         Cache(instance), "Probosqis", website = null, clientId = null,
         clientSecret = null
      )
   }

   private fun token(instance: Cache<Instance>): Token {
      return Token(
         instance,
         "access token",
         "token type",
         "scope",
         LocalDateTime(2000, Month.JANUARY, 1, 0, 0).toInstant(TimeZone.UTC)
      )
   }

   @Test
   fun textField_focused_afterPageStarted() {
      rule.setContent {
         val state = rememberPageState()
         CallbackWaiterPage(state)
      }

      rule.onNode(hasSetTextAction()).assertIsFocused()
   }

   @Test
   fun verifyButton_disabledUnlessCodeInput() {
      lateinit var state: CallbackWaiterPageState

      rule.setContent {
         state = rememberPageState()
         CallbackWaiterPage(state)
      }

      rule.runOnIdle {
         state.inputCode = TextFieldValue("")
      }

      rule.onNodeWithText("Verify the Code").assertIsNotEnabled()

      rule.runOnIdle {
         state.inputCode = TextFieldValue("a")
      }

      rule.onNodeWithText("Verify the Code").assertIsEnabled()
   }

   @Test
   fun verifyButton_repositoryCalled() {
      lateinit var state: CallbackWaiterPageState

      val appRepository = mockk<AppRepository> {
         every { loadAppCache(any()) } answers {
            val application = application(instanceBaseUrl = firstArg())
            Cache(application)
         }

         every { getToken(any(), any()) } answers {
            token(firstArg<Application>().instance)
         }
      }

      val page = CallbackWaiterPage("https://mastodon.social/")

      val pageState = mockk<PPageState.Interface> {
         every { pageStack } returns pageStack(page)
         every { removeFromDeck() } just runs
      }

      rule.setContent {
         state = rememberPageState(page, pageState)
         CallbackWaiterPage(state, appRepository)
      }

      rule.runOnIdle {
         state.inputCode = TextFieldValue("abcdefghijklmnopqrstuvwxyz0123456789")
      }

      rule.onNodeWithText("Verify the Code").performClick()

      rule.runOnIdle {
         verify {
            appRepository.getToken(
               application("https://mastodon.social/"),
               "abcdefghijklmnopqrstuvwxyz0123456789"
            )
         }
      }
   }

   @Test
   fun finishPage_onlyCallbackWaiter() {
      class DummyPage : Page()

      lateinit var state: CallbackWaiterPageState

      val appRepository = mockk<AppRepository> {
         every { loadAppCache(any()) } answers {
            val application = application(instanceBaseUrl = firstArg())
            Cache(application)
         }

         every { getToken(any(), any()) } answers {
            token(firstArg<Application>().instance)
         }
      }

      val page = CallbackWaiterPage("https://mastodon.social/")
      val pageStackSlot = slot<PageStack>()

      val pageState = mockk<PPageState.Interface> {
         every { pageStack } returns pageStack(DummyPage(), page)
         every { pageStack = capture(pageStackSlot) } just runs
      }

      rule.setContent {
         state = rememberPageState(page, pageState)
         CallbackWaiterPage(state, appRepository)
      }

      rule.runOnIdle {
         state.inputCode = TextFieldValue("abcdefghijklmnopqrstuvwxyz0123456789")
      }

      rule.onNodeWithText("Verify the Code").performClick()

      rule.runOnIdle {
         assertIs<DummyPage>(pageStackSlot.captured.head.page)
         assertNull(pageStackSlot.captured.tailOrNull())
      }
   }

   @Test
   fun finishPage_allAuthorizationPages() {
      class DummyPage : Page()

      lateinit var state: CallbackWaiterPageState

      val appRepository = mockk<AppRepository> {
         every { loadAppCache(any()) } answers {
            val application = application(instanceBaseUrl = firstArg())
            Cache(application)
         }

         every { getToken(any(), any()) } answers {
            token(firstArg<Application>().instance)
         }
      }

      val page = CallbackWaiterPage("https://mastodon.social/")
      val pageStackSlot = slot<PageStack>()

      val pageState = mockk<PPageState.Interface> {
         every { pageStack } returns pageStack(DummyPage(), UrlInputPage(), page)
         every { pageStack = capture(pageStackSlot) } just runs
      }

      rule.setContent {
         state = rememberPageState(page, pageState)
         CallbackWaiterPage(state, appRepository)
      }

      rule.runOnIdle {
         state.inputCode = TextFieldValue("abcdefghijklmnopqrstuvwxyz0123456789")
      }

      rule.onNodeWithText("Verify the Code").performClick()

      rule.runOnIdle {
         assertIs<DummyPage>(pageStackSlot.captured.head.page)
         assertNull(pageStackSlot.captured.tailOrNull())
      }
   }

   @Test
   fun removePageStack_onlyCallBackWaiter() {
      lateinit var state: CallbackWaiterPageState

      val appRepository = mockk<AppRepository> {
         every { loadAppCache(any()) } answers {
            val application = application(instanceBaseUrl = firstArg())
            Cache(application)
         }

         every { getToken(any(), any()) } answers {
            token(firstArg<Application>().instance)
         }
      }

      val page = CallbackWaiterPage("https://mastodon.social/")

      val pageState = mockk<PPageState.Interface> {
         every { pageStack } returns pageStack(page)
         every { removeFromDeck() } just runs
      }

      rule.setContent {
         state = rememberPageState(page, pageState)
         CallbackWaiterPage(state, appRepository)
      }

      rule.runOnIdle {
         state.inputCode = TextFieldValue("abcdefghijklmnopqrstuvwxyz0123456789")
      }

      rule.onNodeWithText("Verify the Code").performClick()

      rule.runOnIdle {
         verify {
            pageState.removeFromDeck()
         }
      }
   }

   @Test
   fun removePageStack_onlyAuthorizationPages() {
      lateinit var state: CallbackWaiterPageState

      val appRepository = mockk<AppRepository> {
         every { loadAppCache(any()) } answers {
            val application = application(instanceBaseUrl = firstArg())
            Cache(application)
         }

         every { getToken(any(), any()) } answers {
            token(firstArg<Application>().instance)
         }
      }

      val page = CallbackWaiterPage("https://mastodon.social/")

      val pageState = mockk<PPageState.Interface> {
         every { pageStack } returns pageStack(UrlInputPage(), page)
         every { removeFromDeck() } just runs
      }

      rule.setContent {
         state = rememberPageState(page, pageState)
         CallbackWaiterPage(state, appRepository)
      }

      rule.runOnIdle {
         state.inputCode = TextFieldValue("abcdefghijklmnopqrstuvwxyz0123456789")
      }

      rule.onNodeWithText("Verify the Code").performClick()

      rule.runOnIdle {
         verify {
            pageState.removeFromDeck()
         }
      }
   }
}
