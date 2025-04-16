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

package com.wcaokaze.probosqis.mastodon.entity

import com.wcaokaze.probosqis.ext.kotlin.Url
import com.wcaokaze.probosqis.panoptiqon.Cache
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertSame

class StatusTest {
   private fun id(
      localId: String = "status id",
      instanceUrl: String = "https://example.com",
   ) = Status.Id(
      Url(instanceUrl),
      Status.LocalId(localId),
   )

   private fun status(
      id: Status.Id = id(),
      boostedStatus: Cache<Status>? = null,
      poll: Poll? = null,
      noCredential: Cache<Status.NoCredential> = Cache(noCredential(
         id,
         boostedStatus = boostedStatus?.value?.noCredential,
         poll = poll?.noCredential,
      )),
      isFavorited: Boolean? = null,
      isBoosted: Boolean? = null,
      isMuted: Boolean? = null,
      isBookmarked: Boolean? = null,
      isPinned: Boolean? = null,
      filterResults: List<FilterResult> = emptyList(),
   ) = Status(
      id, noCredential, boostedStatus, poll, isFavorited, isBoosted, isMuted,
      isBookmarked, isPinned, filterResults,
   )

   private fun noCredential(
      id: Status.Id = id(),
      uri: String? = null,
      createdTime: Instant? = null,
      account: Cache<Account>? = null,
      content: String? = null,
      visibility: Status.Visibility? = null,
      isSensitive: Boolean? = null,
      spoilerText: String? = null,
      mediaAttachments: List<MediaAttachment> = emptyList(),
      application: Application? = null,
      mentions: List<Status.Mention> = emptyList(),
      hashtags: List<Status.Hashtag> = emptyList(),
      emojis: List<CustomEmoji> = emptyList(),
      boostCount: Long? = null,
      favoriteCount: Long? = null,
      replyCount: Long? = null,
      url: Url? = null,
      repliedStatusId: Status.Id? = null,
      repliedAccountId: Account.Id? = null,
      boostedStatus: Cache<Status.NoCredential>? = null,
      poll: Cache<Poll.NoCredential>? = null,
      card: PreviewCard? = null,
      language: String? = null,
      text: String? = null,
      editedTime: Instant? = null,
   ) = Status.NoCredential(
      id, uri, createdTime, account, content, visibility, isSensitive,
      spoilerText, mediaAttachments, application, mentions, hashtags, emojis,
      boostCount, favoriteCount, replyCount, url, repliedStatusId,
      repliedAccountId, boostedStatus, poll, card, language, text, editedTime,
   )

   @Test
   fun resolveBoostedStatus() {
      val boostedStatus = status(id("boosted id"))

      val status = status(
         id = id("status id"),
         boostedStatus = Cache(boostedStatus),
      )

      assertSame(
         boostedStatus,
         status.resolveBoostedStatus()
      )
   }

   @Test
   fun resolveBoostedStatus_noBoosted() {
      val status = status(
         id = id("status id"),
         boostedStatus = null,
      )

      assertSame(
         status,
         status.resolveBoostedStatus()
      )
   }

   @Test
   fun resolveBoostedStatus_nested() {
      val boostedStatus = status(id("boosted id"))

      val status = status(
         id = id("status id"),
         boostedStatus = Cache(status(
            id("status id 2"),
            boostedStatus = Cache(status(
               id("status id 3"),
               boostedStatus = Cache(boostedStatus),
            )),
         )),
      )

      assertSame(
         boostedStatus,
         status.resolveBoostedStatus()
      )
   }

   @Test
   fun noCredential_resolveBoostedStatus() {
      val boostedStatus = noCredential(id("boosted id"))

      val status = noCredential(
         id = id("status id"),
         boostedStatus = Cache(boostedStatus),
      )

      assertSame(
         boostedStatus,
         status.resolveBoostedStatus()
      )
   }

   @Test
   fun noCredential_resolveBoostedStatus_noBoosted() {
      val status = noCredential(
         id = id("status id"),
         boostedStatus = null,
      )

      assertSame(
         status,
         status.resolveBoostedStatus()
      )
   }

   @Test
   fun noCredential_resolveBoostedStatus_nested() {
      val boostedStatus = noCredential(id("boosted id"))

      val status = noCredential(
         id = id("status id"),
         boostedStatus = Cache(noCredential(
            id("status id 2"),
            boostedStatus = Cache(noCredential(
               id("status id 3"),
               boostedStatus = Cache(boostedStatus),
            )),
         )),
      )

      assertSame(
         boostedStatus,
         status.resolveBoostedStatus()
      )
   }
}
