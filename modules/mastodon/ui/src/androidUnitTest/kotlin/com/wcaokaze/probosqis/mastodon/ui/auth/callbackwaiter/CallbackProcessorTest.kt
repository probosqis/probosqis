/*
 * Copyright 2024-2025 wcaokaze
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

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import com.wcaokaze.probosqis.capsiqum.page.test.rememberTestPageState
import com.wcaokaze.probosqis.credential.CredentialRepository
import com.wcaokaze.probosqis.ext.kotlin.Url
import com.wcaokaze.probosqis.mastodon.repository.AccountRepository
import com.wcaokaze.probosqis.mastodon.repository.AppRepository
import com.wcaokaze.probosqis.panoptiqon.Cache
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.Rule
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
class CallbackProcessorTest {
   @get:Rule
   val rule = createComposeRule()

   @AfterTest
   fun afterTest() {
      stopKoin()
   }

   @Test
   fun pageStateCalled() {
      val pageState: CallbackWaiterPageState = mockk {
         every { saveAuthorizedAccountByCode(any()) } just runs
      }

      val intent = Intent()
         .setData(Uri.parse("http://probosqis.wcaokaze.com/auth/callback?code=abcdefghijk"))

      CallbackProcessor.onNewIntent(intent, sequenceOf(pageState))

      verify { pageState.saveAuthorizedAccountByCode("abcdefghijk") }
   }

   @Test
   fun repositoryCalled() {
      val appRepository: AppRepository = mockk {
         every { loadAppCache(any()) } returns Cache(mockk())
         every { getToken(any(), any()) } returns mockk()
         every { getCredentialAccount(any()) } returns mockk {
            every { account } returns Cache(mockk())
         }
      }

      val accountRepository: AccountRepository = mockk {
         every { getAccountIcon(any()) } returns Cache(mockk())
      }

      val credentialRepository: CredentialRepository = mockk {
         coEvery { saveCredential(any()) } just runs
      }

      startKoin {
         modules(
            module {
               single { appRepository }
               single { accountRepository }
               single { credentialRepository }
            }
         )
      }

      lateinit var pageState: CallbackWaiterPageState

      rule.setContent {
         val page = CallbackWaiterPage(Url("https://example.com/"))
         pageState = callbackWaiterPageComposable.pageStateFactory
            .rememberTestPageState(page)

         Box(Modifier)
      }

      rule.runOnIdle {
         val intent = Intent()
            .setData(Uri.parse("http://probosqis.wcaokaze.com/auth/callback?code=abcdefghijk"))

         CallbackProcessor.onNewIntent(intent, sequenceOf(pageState))
      }

      rule.waitUntil {
         pageState.credentialAccountLoadState is CredentialAccountLoadState.Success
      }

      rule.runOnIdle {
         verify { appRepository.loadAppCache(Url("https://example.com/")) }
         verify { appRepository.getToken(any(), "abcdefghijk") }
         verify { appRepository.getCredentialAccount(any()) }
         verify { accountRepository.getAccountIcon(any()) }
         coVerify { credentialRepository.saveCredential(any()) }
      }
   }

   @Test
   fun doNothing_dataIsNull() {
      val pageState: CallbackWaiterPageState = mockk {
         every { saveAuthorizedAccountByCode(any()) } just runs
      }

      CallbackProcessor.onNewIntent(Intent(), sequenceOf(pageState))

      verify(exactly = 0) { pageState.saveAuthorizedAccountByCode(any()) }
   }

   @Test
   fun doNothing_hostIsNotProbosqis() {
      val pageState: CallbackWaiterPageState = mockk {
         every { saveAuthorizedAccountByCode(any()) } just runs
      }

      val intent = Intent()
         .setData(Uri.parse("http://example.wcaokaze.com/auth/callback?code=abcdefghijk"))

      CallbackProcessor.onNewIntent(intent, sequenceOf(pageState))

      verify(exactly = 0) { pageState.saveAuthorizedAccountByCode(any()) }
   }

   @Test
   fun doNothing_pathDoesntMatch() {
      val pageState: CallbackWaiterPageState = mockk {
         every { saveAuthorizedAccountByCode(any()) } just runs
      }

      val intent = Intent()
         .setData(Uri.parse("http://probosqis.wcaokaze.com/callback?code=abcdefghijk"))

      CallbackProcessor.onNewIntent(intent, sequenceOf(pageState))

      verify(exactly = 0) { pageState.saveAuthorizedAccountByCode(any()) }
   }

   @Test
   fun doNothing_codeNotSpecified() {
      val pageState: CallbackWaiterPageState = mockk {
         every { saveAuthorizedAccountByCode(any()) } just runs
      }

      val intent = Intent()
         .setData(Uri.parse("http://probosqis.wcaokaze.com/auth/callback"))

      CallbackProcessor.onNewIntent(intent, sequenceOf(pageState))

      verify(exactly = 0) { pageState.saveAuthorizedAccountByCode(any()) }
   }
}
