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
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.input.TextFieldValue
import com.wcaokaze.probosqis.capsiqum.page.test.rememberTestPageState
import com.wcaokaze.probosqis.mastodon.entity.Application
import com.wcaokaze.probosqis.mastodon.entity.Instance
import com.wcaokaze.probosqis.mastodon.entity.Token
import com.wcaokaze.probosqis.mastodon.repository.AppRepository
import com.wcaokaze.probosqis.panoptiqon.Cache
import io.mockk.every
import io.mockk.mockk
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

class CallbackWaiterPageTest {
   @get:Rule
   val rule = createComposeRule()

   @AfterTest
   fun afterTest() {
      stopKoin()
   }

   @Composable
   private fun rememberPageState(
      page: CallbackWaiterPage = CallbackWaiterPage("https://example.com/")
   ): CallbackWaiterPageState {
      val pageState = callbackWaiterPageComposable.pageStateFactory.rememberTestPageState(page)
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

   @Test
   fun textField_focused_afterPageStarted() {
      rule.setContent {
         val state = rememberPageState()
         CallbackWaiterPage(state)
      }

      rule.onNode(hasSetTextAction()).assertIsFocused()
   }

   @Test
   fun verifyButton_repositoryCalled() {
      lateinit var state: CallbackWaiterPageState

      fun application(instanceBaseUrl: String): Application {
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

      val appRepository = mockk<AppRepository> {
         every { loadAppCache(any()) } answers {
            val application = application(firstArg())
            Cache(application)
         }

         every { getToken(any(), any()) } answers {
            Token(
               firstArg<Application>().instance,
               "access token",
               "token type",
               "scope",
               LocalDateTime(2000, Month.JANUARY, 1, 0, 0).toInstant(TimeZone.UTC)
            )
         }
      }

      rule.setContent {
         val page = CallbackWaiterPage("https://mastodon.social/")
         state = rememberPageState(page)
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
}
