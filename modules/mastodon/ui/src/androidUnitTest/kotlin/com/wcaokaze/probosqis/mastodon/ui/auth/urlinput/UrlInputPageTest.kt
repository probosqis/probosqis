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
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.input.TextFieldValue
import com.wcaokaze.probosqis.capsiqum.page.test.rememberTestPageState
import com.wcaokaze.probosqis.ext.compose.BrowserLauncher
import com.wcaokaze.probosqis.ext.compose.LocalBrowserLauncher
import com.wcaokaze.probosqis.mastodon.repository.AppRepository
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
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class UrlInputPageTest {
   @get:Rule
   val rule = createComposeRule()

   @AfterTest
   fun afterTest() {
      stopKoin()
   }

   @Composable
   private fun UrlInputPage(
      pageState: UrlInputPageState,
      browserLauncher: BrowserLauncher = mockk(),
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
         val page = UrlInputPage()
         val state = urlInputPageComposable.pageStateFactory.rememberTestPageState(page)

         UrlInputPage(state)
      }

      rule.onNodeWithText("https://mastodon.social/")
         .assertIsFocused()
   }

   @Test
   fun textField_domain_selected_afterPageStarted() {
      lateinit var state: UrlInputPageState

      rule.setContent {
         val page = UrlInputPage()
         state = urlInputPageComposable.pageStateFactory.rememberTestPageState(page)

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
   fun goButton_repositoryAndBrowserLaunchedCalled() {
      lateinit var state: UrlInputPageState

      val appRepository = mockk<AppRepository> {
         every { getAuthorizeUrl(any<String>()) } returns "https://auth.wcaokaze.com/"
      }

      val browserLauncher = mockk<BrowserLauncher> {
         every { launchBrowser(any()) } returns Unit
      }

      rule.setContent {
         val page = UrlInputPage()
         state = urlInputPageComposable.pageStateFactory.rememberTestPageState(page)

         UrlInputPage(
            state,
            browserLauncher = browserLauncher,
            appRepository = appRepository
         )
      }

      rule.runOnIdle {
         state.inputUrl = TextFieldValue("https://example.wcaokaze.com/")
      }

      rule.onNodeWithText("GO").performClick()

      rule.runOnIdle {
         verify { appRepository.getAuthorizeUrl("https://example.wcaokaze.com/") }
         verify { browserLauncher.launchBrowser("https://auth.wcaokaze.com/") }
      }
   }
}
