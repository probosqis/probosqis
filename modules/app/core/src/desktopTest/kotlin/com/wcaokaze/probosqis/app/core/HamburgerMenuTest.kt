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

package com.wcaokaze.probosqis.app.core

import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.wcaokaze.probosqis.foundation.credential.Credential
import com.wcaokaze.probosqis.foundation.credential.CredentialRepository
import com.wcaokaze.probosqis.mastodon.entity.Token
import com.wcaokaze.probosqis.panoptiqon.Cache
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.Test

class HamburgerMenuTest {
   @get:Rule
   val rule = createComposeRule()

   @AfterTest
   fun after() {
      stopKoin()
   }

   @Test
   fun recomposedAfterCredentialListCacheRewritten() {
      // TODO: mastodon.entity.Token以外が使えるようになったらCredentialImplにする
      // class CredentialImpl(override val id: String) : Credential()

      fun Token(name: String) = mockk<Token> {
         every { account } returns Cache(mockk {
            every { account } returns Cache(mockk {
               every { displayName } returns null
               every { username } returns name
            })
         })
      }

      val credentialsCache = WritableCache<List<Cache<Credential>>>(listOf(
         Cache(Token("abc")),
         Cache(Token("de")),
      ))

      rule.setContent {
         remember {
            val koinApplication = koinApplication {
               modules(
                  module {
                     single {
                        mockk<CredentialRepository> {
                           every { loadAllCredentials() } returns credentialsCache
                        }
                     }
                  }
               )
            }

            startKoin(koinApplication)
         }

         val hamburgerMenuState = remember { HamburgerMenuState() }

         HamburgerMenu(
            hamburgerMenuState,
            onRequestAddColumn = {}
         )
      }

      rule.onNodeWithText("abc").assertExists()
      rule.onNodeWithText("de") .assertExists()
      rule.onNodeWithText("fgh").assertDoesNotExist()

      credentialsCache.value += Cache(Token("fgh"))

      rule.onNodeWithText("abc").assertExists()
      rule.onNodeWithText("de") .assertExists()
      rule.onNodeWithText("fgh").assertExists()
   }
}
