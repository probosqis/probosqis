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

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.app.setting.Setting
import com.wcaokaze.probosqis.capsiqum.page.PageStateFactory
import com.wcaokaze.probosqis.ext.compose.LoadState
import com.wcaokaze.probosqis.foundation.credential.CredentialRepository
import com.wcaokaze.probosqis.foundation.page.PPage
import com.wcaokaze.probosqis.foundation.page.PPageComposable
import com.wcaokaze.probosqis.foundation.page.PPageState
import com.wcaokaze.probosqis.foundation.resources.Strings
import com.wcaokaze.probosqis.mastodon.entity.Token
import com.wcaokaze.probosqis.mastodon.repository.AppRepository
import com.wcaokaze.probosqis.mastodon.ui.auth.urlinput.UrlInputPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.component.inject

@Serializable
@SerialName("com.wcaokaze.probosqis.app.setting.account.list.AccountListPage")
class AccountListPage : PPage()

@Stable
class AccountListPageState : PPageState<AccountListPage>() {
   private val credentialRepository: CredentialRepository by inject()
   private val appRepository: AppRepository by inject()

   var credentialLoadState: LoadState<List<Token>>
      by mutableStateOf(LoadState.Loading)
      private set

   init {
      pageStateScope.launch {
         credentialLoadState = withContext (Dispatchers.IO) {
            try {
               val credentials = credentialRepository.loadAllCredentials()
                  .map { credentialCache ->
                     val credential = credentialCache.value as Token

                     val credentialAccount
                        = appRepository.getCredentialAccount(credential)

                     credential.copy(
                        account = credentialAccount,
                     )
                  }

               LoadState.Success(credentials)
            } catch (e: Exception) {
               LoadState.Error(e)
            }
         }
      }
   }
}

val accountListPageComposable = PPageComposable<AccountListPage, AccountListPageState>(
   PageStateFactory { _, _ -> AccountListPageState() },
   header = { _, _ ->
      Text(
         Strings.Setting.accountList.appBar,
         maxLines = 1,
         overflow = TextOverflow.Ellipsis
      )
   },
   content = { _, pageState, _ ->
      AccountListPageContent(
         pageState.credentialLoadState,
         onAddAccountItemClick = {
            pageState.startPage(UrlInputPage())
         }
      )
   },
   footer = null,
   pageTransitions = {
   }
)

@Composable
private fun AccountListPageContent(
   credentialLoadState: LoadState<List<Token>>,
   onAddAccountItemClick: () -> Unit
) {
   Crossfade(credentialLoadState) { state ->
      when (state) {
         is LoadState.Loading -> {
            Box(Modifier.fillMaxSize()) {
               CircularProgressIndicator(
                  modifier = Modifier.align(Alignment.Center)
               )
            }
         }
         is LoadState.Success -> {
            LazyColumn(
               modifier = Modifier.fillMaxSize()
            ) {
               items(state.data) { token ->
                  Column {
                     Column(
                        modifier = Modifier
                           .fillMaxWidth()
                           .padding(horizontal = 16.dp, vertical = 8.dp)
                     ) {
                        val credentialAccount = token.account!!.value
                        val account = credentialAccount.account.value
                        val username = account.username

                        val displayName = account.displayName ?: account.username
                        if (displayName != null) {
                           Text(
                              displayName,
                              style = MaterialTheme.typography.titleMedium
                           )
                        }

                        if (displayName != null && username != null) {
                           Spacer(Modifier.height(4.dp))
                        }

                        if (username != null) {
                           Text(
                              "@$username",
                              style = MaterialTheme.typography.bodyMedium
                           )
                        }
                     }

                     HorizontalDivider()
                  }
               }

               item {
                  AddAccountItem(onClick = onAddAccountItemClick)
               }
            }
         }
         is LoadState.Error -> {
            Text("エラーだよ")
         }
      }
   }
}

@Composable
private fun AddAccountItem(
   onClick: () -> Unit
) {
   Row(
      modifier = Modifier
         .fillMaxWidth()
         .heightIn(min = 48.dp)
         .clickable(onClick = onClick)
         .padding(4.dp)
   ) {
      Icon(
         Icons.Default.Add,
         contentDescription = null,
         modifier = Modifier
            .padding(4.dp)
            .align(Alignment.CenterVertically)
      )

      Text(
         Strings.Setting.accountList.addAccountItem,
         maxLines = 1,
         overflow = TextOverflow.Ellipsis,
         style = MaterialTheme.typography.labelLarge,
         modifier = Modifier
            .padding(8.dp)
            .align(Alignment.CenterVertically)
      )
   }
}
