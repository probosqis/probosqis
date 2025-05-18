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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.ext.compose.LoadState
import com.wcaokaze.probosqis.foundation.credential.CredentialRepository
import com.wcaokaze.probosqis.foundation.resources.Strings
import com.wcaokaze.probosqis.mastodon.entity.Token
import com.wcaokaze.probosqis.mastodon.repository.AppRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Stable
internal class HamburgerMenuState : KoinComponent {
   private val credentialRepository: CredentialRepository by inject()
   private val appRepository: AppRepository by inject()

   var credentialLoadState: LoadState<ImmutableList<AccountItemState>>
      by mutableStateOf(LoadState.Loading)
      private set

   suspend fun fetchCredentials() {
      credentialLoadState = withContext (Dispatchers.IO) {
         try {
            val credentials = credentialRepository.loadAllCredentials()
               .map { credentialCache ->
                  val credential = credentialCache.value as Token

                  val credentialAccount
                      = appRepository.getCredentialAccount(credential)

                  AccountItemState(
                     credential.copy(
                        account = credentialAccount,
                     )
                  )
               }
               .toImmutableList()

            LoadState.Success(credentials)
         } catch (e: Exception) {
            LoadState.Error(e)
         }
      }
   }
}

@Stable
internal class AccountItemState(
   val credential: Token
) {
   var isExpanded by mutableStateOf(false)
}

@Composable
internal fun HamburgerMenu(
   state: HamburgerMenuState,
   onHomeTimelineItemClick: (Token) -> Unit,
   onSettingItemClick: () -> Unit
) {
   LaunchedEffect(Unit) {
      state.fetchCredentials()
   }

   ModalDrawerSheet {
      AccountList(
         state.credentialLoadState,
         onHomeTimelineItemClick,
         modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
      )

      HorizontalDivider()

      DropdownMenuItem(
         leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
         text = { Text(Strings.App.hamburgerMenuSettingItem) },
         onClick = onSettingItemClick
      )

      Spacer(Modifier.height(40.dp))
   }
}

@Composable
private fun AccountList(
   credentialLoadState: LoadState<ImmutableList<AccountItemState>>,
   onHomeTimelineItemClick: (Token) -> Unit,
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
                     AccountItem(accountItemState)

                     AnimatedVisibility(
                        visible = accountItemState.isExpanded,
                        label = "account subitem expansion"
                     ) {
                        HorizontalDivider()

                        HomeTimelineItem(
                           onClick = {
                              onHomeTimelineItemClick(accountItemState.credential)
                           }
                        )
                     }

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
private fun AccountItem(state: AccountItemState) {
      DropdownMenuItem(
         text = {
            Row(
               verticalAlignment = Alignment.CenterVertically
            ) {
               val credentialAccount = state.credential.account!!.value
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
