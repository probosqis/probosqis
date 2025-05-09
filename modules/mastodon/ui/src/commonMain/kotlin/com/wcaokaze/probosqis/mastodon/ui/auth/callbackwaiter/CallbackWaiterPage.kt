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

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wcaokaze.probosqis.credential.CredentialRepository
import com.wcaokaze.probosqis.entity.Image
import com.wcaokaze.probosqis.ext.kotlin.Url
import com.wcaokaze.probosqis.mastodon.entity.CredentialAccount
import com.wcaokaze.probosqis.mastodon.entity.Token
import com.wcaokaze.probosqis.mastodon.repository.AccountRepository
import com.wcaokaze.probosqis.mastodon.repository.AppRepository
import com.wcaokaze.probosqis.mastodon.ui.auth.urlinput.UrlInputPage
import com.wcaokaze.probosqis.mastodon.ui.timeline.home.HomeTimelinePage
import com.wcaokaze.probosqis.foundation.page.PPage
import com.wcaokaze.probosqis.foundation.page.PPageComposable
import com.wcaokaze.probosqis.foundation.page.PPageState
import com.wcaokaze.probosqis.panoptiqon.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.seconds

@Serializable
@SerialName("com.wcaokaze.probosqis.mastodon.ui.auth.callbackwaiter.CallbackWaiterPage")
class CallbackWaiterPage(
   val instanceBaseUrl: Url
) : PPage()

internal sealed class CredentialAccountLoadState {
   data object Unloading : CredentialAccountLoadState()
   data object Loading   : CredentialAccountLoadState()
   data object Error     : CredentialAccountLoadState()

   class Success(
      val credentialAccount: CredentialAccount,
      val credentialAccountIcon: Cache<Image?>
   ) : CredentialAccountLoadState()
}

abstract class AbstractCallbackWaiterPageState : PPageState<CallbackWaiterPage>() {
   private val appRepository: AppRepository by inject()
   private val accountRepository: AccountRepository by inject()
   private val credentialRepository: CredentialRepository by inject()

   internal var credentialAccountLoadState: CredentialAccountLoadState
      by mutableStateOf(CredentialAccountLoadState.Unloading)

   fun saveAuthorizedAccountByCode(code: String) {
      if (credentialAccountLoadState is CredentialAccountLoadState.Loading) {
         return
      }

      credentialAccountLoadState = CredentialAccountLoadState.Loading

      pageStateScope.launch {
         val token: Token

         try {
            credentialAccountLoadState = withContext(Dispatchers.IO) {
               val application = appRepository.loadAppCache(page.instanceBaseUrl)
               token = appRepository.getToken(application.value, code)
               val credentialAccount = appRepository.getCredentialAccount(token)
               val credentialAccountIcon
                  = accountRepository.getAccountIcon(credentialAccount.account.value)

               credentialRepository.saveCredential(token)

               CredentialAccountLoadState.Success(
                  credentialAccount, credentialAccountIcon
               )
            }
         } catch (e: Exception) {
            e.printStackTrace()
            credentialAccountLoadState = CredentialAccountLoadState.Error
            return@launch
         }

         delay(3.seconds)

         finishAuthPages()
         startPage(HomeTimelinePage(token))
      }
   }

   private fun finishAuthPages() {
      var pageStack = pageStack.tailOrNull()
      if (pageStack == null) {
         removeFromDeck()
         return
      }

      if (pageStack.head.page !is UrlInputPage) {
         this.pageStack = pageStack
         return
      }

      pageStack = pageStack.tailOrNull()
      if (pageStack == null) {
         removeFromDeck()
         return
      }

      this.pageStack = pageStack
   }
}

@Stable
expect class CallbackWaiterPageState : AbstractCallbackWaiterPageState

expect val callbackWaiterPageComposable: PPageComposable<CallbackWaiterPage, CallbackWaiterPageState>
