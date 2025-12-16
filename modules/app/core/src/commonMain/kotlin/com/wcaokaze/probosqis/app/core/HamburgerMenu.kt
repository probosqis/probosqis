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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.app.setting.account.list.AccountListPage
import com.wcaokaze.probosqis.capsiqum.page.Page
import com.wcaokaze.probosqis.ext.compose.LoadState
import com.wcaokaze.probosqis.foundation.credential.Credential
import com.wcaokaze.probosqis.foundation.credential.CredentialRepository
import com.wcaokaze.probosqis.foundation.resources.Strings
import com.wcaokaze.probosqis.mastodon.entity.Token
import com.wcaokaze.probosqis.mastodon.ui.timeline.home.HomeTimelinePage
import com.wcaokaze.probosqis.panoptiqon.Cache
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Stable
internal class HamburgerMenuState : KoinComponent {
   private val credentialRepository: CredentialRepository by inject()

   private var credentialLoadState: LoadState<Cache<List<Cache<Credential>>>>
      by mutableStateOf(LoadState.Loading)

   val accountItemStates: LoadState<ImmutableList<AccountItemState>> by derivedStateOf {
      when (val credential = credentialLoadState) {
         is LoadState.Success -> {
            val accountItemStates
               = credential.data.value.map { AccountItemState(it) }

            LoadState.Success(accountItemStates.toImmutableList())
         }
         is LoadState.Loading -> {
            LoadState.Loading
         }
         is LoadState.Error -> {
            LoadState.Error(credential.exception)
         }
      }
   }

   fun fetchCredentials() {
      credentialLoadState = try {
         val credentials = credentialRepository.loadAllCredentials().asCache()
         LoadState.Success(credentials)
      } catch (e: Exception) {
         LoadState.Error(e)
      }
   }
}

@Stable
internal class AccountItemState(
   val credential: Cache<Credential>
) {
   var isExpanded by mutableStateOf(false)
}

@Composable
internal fun HamburgerMenu(
   state: HamburgerMenuState,
   onRequestAddColumn: (Page) -> Unit
) {
   LaunchedEffect(Unit) {
      state.fetchCredentials()
   }

   ModalDrawerSheet {
      AccountList(
         state.accountItemStates,
         onRequestAddColumn,
         modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
      )

      HorizontalDivider()

      DropdownMenuItem(
         leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
         text = { Text(Strings.App.hamburgerMenuSettingItem) },
         onClick = {
            onRequestAddColumn(AccountListPage())
         }
      )

      Spacer(Modifier.height(40.dp))
   }
}

@Composable
private fun AccountList(
   credentialLoadState: LoadState<ImmutableList<AccountItemState>>,
   onRequestAddColumn: (Page) -> Unit,
   modifier: Modifier = Modifier
) {
   Crossfade(
      credentialLoadState,
      modifier = modifier
   ) { state ->
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
               modifier = modifier
            ) {
               itemsIndexed(state.data) { index, accountItemState ->
                  Column {
                     // TODO :modules:mastodon:uiとかにあるべき
                     AccountItem(accountItemState, onRequestAddColumn)

                     if (index < state.data.lastIndex) {
                        HorizontalDivider()
                     }
                  }
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
private fun AccountItem(
   state: AccountItemState,
   onRequestAddColumn: (Page) -> Unit
) {
   Column {
      DropdownMenuItem(
         text = {
            when (val credential = state.credential.value) {
               is Token -> {
                  MastodonAccountItem(credential)
               }
            }
         },
         trailingIcon = {
            val rotate by animateFloatAsState(
               if (state.isExpanded) { 180f } else { 0f },
               label = "account item expand icon rotation"
            )

            Icon(
               Icons.Default.KeyboardArrowDown,
               contentDescription = null,
               modifier = Modifier.rotate(rotate)
            )
         },
         onClick = { state.isExpanded = !state.isExpanded }
      )

      AnimatedVisibility(
         visible = state.isExpanded,
         label = "account subitem expansion"
      ) {
         HorizontalDivider()

         when (val credential = state.credential.value) {
            is Token -> {
               MastodonAccountExpandedItems(credential, onRequestAddColumn)
            }
         }
      }
   }
}

@Composable
private fun MastodonAccountItem(token: Token) {
   Row(
      verticalAlignment = Alignment.CenterVertically
   ) {
      val credentialAccount = token.account.value
      val account = credentialAccount.account.value
      val username = account.username

      val displayName = account.displayName ?: account.username
      if (displayName != null) {
         Text(
            displayName,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.titleMedium
         )
      }

      if (displayName != null && username != null) {
         Spacer(Modifier.width(4.dp))
      }

      if (username != null) {
         Text(
            "@$username",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.bodyMedium
         )
      }
   }
}

@Composable
private fun MastodonAccountExpandedItems(
   token: Token,
   onRequestAddColumn: (Page) -> Unit
) {
   HomeTimelineItem(
      onClick = {
         onRequestAddColumn(HomeTimelinePage(token))
      }
   )
}

@Composable
private fun HomeTimelineItem(onClick: () -> Unit) {
   DropdownMenuItem(
      contentPadding = PaddingValues(start = 24.dp, end = 12.dp),
      leadingIcon = {
         Icon(
            Icons.Default.Home,
            contentDescription = null
         )
      },
      text = {
         Text(Strings.App.hamburgerMenuHomeTimelineItem)
      },
      onClick = onClick
   )
}
