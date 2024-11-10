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

package com.wcaokaze.probosqis.mastodon.ui.auth.urlinput

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.input.TextFieldValue
import com.github.takahirom.roborazzi.captureRoboImage
import com.wcaokaze.probosqis.capsiqum.page.test.rememberTestPageState
import com.wcaokaze.probosqis.ext.compose.BrowserLauncher
import com.wcaokaze.probosqis.ext.compose.LocalBrowserLauncher
import com.wcaokaze.probosqis.mastodon.repository.AppRepository
import com.wcaokaze.probosqis.mastodon.ui.auth.callbackwaiter.CallbackWaiterPage
import com.wcaokaze.probosqis.nodeinfo.entity.FediverseSoftware
import com.wcaokaze.probosqis.nodeinfo.repository.NodeInfoRepository
import com.wcaokaze.probosqis.page.PPageState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.runner.RunWith
import org.koin.compose.KoinIsolatedContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import java.io.IOException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.test.AfterTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class UrlInputPageTest {
   @get:Rule
   val rule = createComposeRule()

   @AfterTest
   fun afterTest() {
      stopKoin()
   }

   @Composable
   private fun rememberPageState(
      page: UrlInputPage = UrlInputPage(),
      pageStateBase: PPageState.Interface = mockk()
   ): UrlInputPageState {
      val pageState = urlInputPageComposable.pageStateFactory.rememberTestPageState(page)
      pageState.injectTestable(pageStateBase)
      return pageState
   }

   @Composable
   private fun UrlInputPage(
      pageState: UrlInputPageState,
      browserLauncher: BrowserLauncher = mockk(),
      nodeInfoRepository: NodeInfoRepository = mockk(),
      appRepository: AppRepository = mockk(),
   ) {
      val koinApplication = remember {
         koinApplication {
            modules(
               module {
                  single { nodeInfoRepository }
                  single { appRepository }
               }
            )
         }
      }

      remember {
         startKoin(koinApplication)
      }

      KoinIsolatedContext(koinApplication) {
         CompositionLocalProvider(
            LocalBrowserLauncher provides browserLauncher,
         ) {
            urlInputPageComposable.contentComposable(
               pageState.page, pageState, WindowInsets(0)
            )
         }
      }
   }

   @Test
   fun textField_focused_afterPageStarted() {
      rule.setContent {
         val state = rememberPageState()
         UrlInputPage(state)
      }

      rule.onNodeWithText("https://mastodon.social/")
         .assertIsFocused()
   }

   @Test
   fun textField_domain_selected_afterPageStarted() {
      lateinit var state: UrlInputPageState

      rule.setContent {
         state = rememberPageState()
         UrlInputPage(state)
      }

      rule.runOnIdle {
         val inputUrl = state.inputUrl
         assertEquals(
            "https://",
            inputUrl.text.substring(0, inputUrl.selection.min)
         )
         assertEquals(
            "mastodon.social/",
            inputUrl.text.substring(inputUrl.selection.min, inputUrl.selection.max)
         )
         assertEquals(
            "",
            inputUrl.text.substring(inputUrl.selection.max)
         )
      }
   }

   @Test
   fun goButton_repositoryCalled() {
      lateinit var state: UrlInputPageState

      val nodeInfoRepository = mockk<NodeInfoRepository> {
         every { getServerSoftware(any()) } answers {
            FediverseSoftware.Mastodon(firstArg(), "1.0.0")
         }
      }

      val appRepository = mockk<AppRepository> {
         every { getAuthorizeUrl(any<String>()) } returns "https://auth.wcaokaze.com/"
      }

      val browserLauncher = mockk<BrowserLauncher> {
         every { launchBrowser(any()) } returns Unit
      }

      val pageState = mockk<PPageState.Interface> {
         every { finishPage() } returns Unit
         every { startPage(any()) } returns Unit
      }

      rule.setContent {
         state = rememberPageState(pageStateBase = pageState)

         UrlInputPage(
            state,
            browserLauncher = browserLauncher,
            nodeInfoRepository = nodeInfoRepository,
            appRepository = appRepository
         )
      }

      rule.runOnIdle {
         state.inputUrl = TextFieldValue("https://example.wcaokaze.com/")
      }

      rule.onNodeWithText("GO").performClick()

      rule.runOnIdle {
         verify { nodeInfoRepository.getServerSoftware("https://example.wcaokaze.com/") }
         verify { appRepository.getAuthorizeUrl("https://example.wcaokaze.com/") }
      }
   }

   @Test
   fun goButton_browserLaunchedAndPageFinished() {
      lateinit var state: UrlInputPageState

      val nodeInfoRepository = mockk<NodeInfoRepository> {
         every { getServerSoftware(any()) } answers {
            FediverseSoftware.Mastodon(firstArg(), "1.0.0")
         }
      }

      val appRepository = mockk<AppRepository> {
         every { getAuthorizeUrl(any<String>()) } returns "https://auth.wcaokaze.com/"
      }

      val browserLauncher = mockk<BrowserLauncher> {
         every { launchBrowser(any()) } returns Unit
      }

      val pageState = mockk<PPageState.Interface> {
         every { finishPage() } returns Unit
         every { startPage(any()) } returns Unit
      }

      rule.setContent {
         state = rememberPageState(pageStateBase = pageState)

         UrlInputPage(
            state,
            browserLauncher = browserLauncher,
            nodeInfoRepository = nodeInfoRepository,
            appRepository = appRepository
         )
      }

      rule.onNodeWithText("GO").performClick()

      rule.runOnIdle {
         verify { browserLauncher.launchBrowser("https://auth.wcaokaze.com/") }
         verify { pageState.finishPage() }
         verify { pageState.startPage(ofType<CallbackWaiterPage>()) }
      }
   }

   @Test
   fun goButton_disabled_whileGettingAuthUrl() {
      lateinit var state: UrlInputPageState

      val lock = ReentrantLock()

      val nodeInfoRepository = mockk<NodeInfoRepository> {
         every { getServerSoftware(any()) } answers {
            FediverseSoftware.Mastodon(firstArg(), "1.0.0")
         }
      }

      val appRepository = mockk<AppRepository> {
         every { getAuthorizeUrl(any<String>()) } answers {
            lock.withLock {
               "https://auth.wcaokaze.com/"
            }
         }
      }

      val browserLauncher = mockk<BrowserLauncher> {
         every { launchBrowser(any()) } returns Unit
      }

      val pageState = mockk<PPageState.Interface> {
         every { finishPage() } returns Unit
         every { startPage(any()) } returns Unit
      }

      rule.setContent {
         state = rememberPageState(pageStateBase = pageState)

         UrlInputPage(
            state,
            browserLauncher = browserLauncher,
            nodeInfoRepository = nodeInfoRepository,
            appRepository = appRepository
         )
      }

      lock.withLock {
         state.inputUrl = TextFieldValue("https://example.wcaokaze.com/")

         rule.onNodeWithText("GO").performClick()

         rule.onNodeWithText("GO").assertIsNotEnabled()
         rule.onNodeWithText("https://example.wcaokaze.com/").assertIsNotEnabled()
      }

      rule.onNodeWithText("GO").assertIsEnabled()
      rule.onNodeWithText("https://example.wcaokaze.com/").assertIsEnabled()
   }

   @Ignore("https://github.com/probosqis/probosqis/issues/82")
   @Test
   fun screenshot_usual() {
      rule.setContent {
         val state = rememberPageState()
         UrlInputPage(state)
      }

      rule.onRoot().captureRoboImage("urlInputPage/usual.png")
   }

   @Ignore("https://github.com/probosqis/probosqis/issues/82")
   @Test
   fun screenshot_loading() {
      val lock = ReentrantLock()

      val appRepository = mockk<AppRepository> {
         every { getAuthorizeUrl(any<String>()) } answers {
            lock.withLock {
               "https://auth.wcaokaze.com/"
            }
         }
      }

      val browserLauncher = mockk<BrowserLauncher> {
         every { launchBrowser(any()) } returns Unit
      }

      val pageState = mockk<PPageState.Interface> {
         every { finishPage() } returns Unit
         every { startPage(any()) } returns Unit
      }

      rule.setContent {
         val state = rememberPageState(pageStateBase = pageState)

         UrlInputPage(
            state,
            browserLauncher = browserLauncher,
            appRepository = appRepository
         )
      }

      lock.withLock {
         rule.onNodeWithText("GO").performClick()
         rule.onRoot().captureRoboImage("urlInputPage/loading.png")
      }
   }

   @Ignore("https://github.com/probosqis/probosqis/issues/82")
   @Test
   fun screenshot_error() {
      val lock = ReentrantLock()

      val appRepository = mockk<AppRepository> {
         every { getAuthorizeUrl(any<String>()) } answers {
            lock.withLock {
               throw IOException()
            }
         }
      }

      rule.setContent {
         val state = rememberPageState()

         UrlInputPage(
            state,
            appRepository = appRepository
         )
      }

      lock.withLock {
         rule.onNodeWithText("GO").performClick()
         rule.waitForIdle()
      }

      rule.onRoot().captureRoboImage("urlInputPage/error.png")
   }
}
