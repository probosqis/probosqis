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

package com.wcaokaze.probosqis.mastodon.ui.timeline.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.wcaokaze.probosqis.capsiqum.page.PageStateFactory
import com.wcaokaze.probosqis.mastodon.entity.Status
import com.wcaokaze.probosqis.mastodon.entity.Token
import com.wcaokaze.probosqis.mastodon.repository.TimelineRepository
import com.wcaokaze.probosqis.page.PPage
import com.wcaokaze.probosqis.page.PPageComposable
import com.wcaokaze.probosqis.page.PPageState
import com.wcaokaze.probosqis.panoptiqon.compose.asState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.component.inject

@Serializable
@SerialName("com.wcaokaze.probosqis.mastodon.ui.timeline.home.HomeTimelinePage")
class HomeTimelinePage(val token: Token) : PPage()

@Stable
class HomeTimelinePageState : PPageState<HomeTimelinePage>() {
   private val timelineRepository: TimelineRepository by inject()

   var statuses by mutableStateOf<ImmutableList<Status>>(persistentListOf())
      private set

   init {
      pageStateScope.launch {
         try {
            statuses = withContext(Dispatchers.IO) {
               timelineRepository.getHomeTimeline(page.token).toImmutableList()
            }
         } catch (e: Exception) {
            throw e
         }
      }
   }
}

val homeTimelinePageComposable = PPageComposable<HomeTimelinePage, HomeTimelinePageState>(
   PageStateFactory { _, _ -> HomeTimelinePageState() },
   header = { _, _ ->
   },
   content = { _, state, _ ->
      LazyColumn(
         modifier = Modifier.fillMaxWidth()
      ) {
         items(state.statuses) { status ->
            val noCredentialStatus by status.noCredential.asState()

            Text(
               text = noCredentialStatus.content ?: ""
            )
         }
      }
   },
   footer = null,
   pageTransitions = {
   }
)
