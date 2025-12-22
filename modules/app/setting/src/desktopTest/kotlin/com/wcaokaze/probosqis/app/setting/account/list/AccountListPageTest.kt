/*
 * Copyright 2025 wcaokaze
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

package com.wcaokaze.probosqis.app.setting.account.list

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.wcaokaze.probosqis.capsiqum.page.test.rememberTestPageState
import com.wcaokaze.probosqis.ext.compose.LoadState
import com.wcaokaze.probosqis.foundation.credential.Credential
import com.wcaokaze.probosqis.foundation.credential.CredentialRepository
import com.wcaokaze.probosqis.foundation.page.PPageState
import com.wcaokaze.probosqis.mastodon.entity.Token
import com.wcaokaze.probosqis.panoptiqon.Cache
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.koin.compose.KoinIsolatedContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Ignore
import kotlin.test.Test

class AccountListPageTest {
   @get:Rule
   val rule = createComposeRule()

   @Composable
   private fun KoinContext(
      credentialRepository: CredentialRepository = mockk(),
      content: @Composable () -> Unit
   ) {
      val koinApplication = remember {
         koinApplication {
            modules(
               module {
                  single { credentialRepository }
               }
            )
         }
      }

      remember {
         startKoin(koinApplication)
      }

      DisposableEffect(Unit) {
         onDispose {
            stopKoin()
         }
      }

      KoinIsolatedContext(koinApplication, content)
   }

   @Composable
   private fun rememberPageState(
      page: AccountListPage = AccountListPage(),
      pageStateBase: PPageState.Interface = mockk()
   ): AccountListPageState {
      val pageState = accountListPageComposable.pageStateFactory.rememberTestPageState(page)
      pageState.injectTestable(pageStateBase)
      return pageState
   }

   @Composable
   private fun AccountListPage(
      pageState: AccountListPageState,
   ) {
      accountListPageComposable.contentComposable(
         pageState.page, pageState, WindowInsets(0)
      )
   }

   @Ignore(
      "AccountListPageState.credentialLoadStateがSuccessになったあと" +
      "なぜかリコンポーズされない。withContextをコメントアウトすると通る"
   )
   @Test
   fun recomposedAfterCredentialListCacheRewritten() {
      // TODO: mastodon.entity.Token以外が使えるようになったらCredentialImplにする
      // class CredentialImpl(override val id: String) : Credential()

      fun Token(name: String) = mockk<Token> {
         every { account } returns Cache(mockk {
            every { account } returns Cache(mockk {
               every { displayName } returns name
               every { username } returns name
            })
         })
      }

      val credentialsCache = WritableCache<List<Cache<Credential>>>(listOf(
         Cache(Token("abc")),
         Cache(Token("de")),
      ))

      lateinit var pageState: AccountListPageState

      rule.setContent {
         val credentialRepository = mockk<CredentialRepository> {
            every { loadAllCredentials() } answers {
               credentialsCache
            }
         }

         KoinContext(credentialRepository) {
            pageState = rememberPageState()
            AccountListPage(pageState)
         }
      }

      rule.waitUntil { pageState.credentialLoadState is LoadState.Success }

      rule.onNodeWithText("abc").assertExists()
      rule.onNodeWithText("de") .assertExists()
      rule.onNodeWithText("fgh").assertDoesNotExist()

      credentialsCache.value += Cache(Token("fgh"))

      rule.onNodeWithText("abc").assertExists()
      rule.onNodeWithText("de") .assertExists()
      rule.onNodeWithText("fgh").assertExists()
   }
}
