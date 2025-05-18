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

data class Status(
   val id: Id,
   val noCredential: Cache<NoCredential>,
   val boostedStatus: Cache<Status>?,
   val poll: Poll?,
   val isFavorited: Boolean?,
   val isBoosted: Boolean?,
   val isMuted: Boolean?,
   val isBookmarked: Boolean?,
   val isPinned: Boolean?,
   val filterResults: List<FilterResult>,
) {
   constructor(
      rawInstanceUrl: String,
      rawLocalId: String,
      noCredential: Cache<NoCredential>,
      boostedStatus: Cache<Status>?,
      poll: Poll?,
      isFavorited: Boolean?,
      isBoosted: Boolean?,
      isMuted: Boolean?,
      isBookmarked: Boolean?,
      isPinned: Boolean?,
      filterResults: List<FilterResult>,
   ) : this(
      Id(Url(rawInstanceUrl), LocalId(rawLocalId)),
      noCredential,
      boostedStatus,
      poll,
      isFavorited,
      isBoosted,
      isMuted,
      isBookmarked,
      isPinned,
      filterResults,
   )

   val rawInstanceUrl: String
      get() = id.instanceUrl.raw

   val rawLocalId: String
      get() = id.local.raw

   data class Id(
      val instanceUrl: Url,
      val local: LocalId
   ) {
      constructor(
         rawInstanceUrl: String,
         rawLocalId: String,
         @Suppress("UNUSED_PARAMETER")
         dummy: Unit?
      ) : this(
         Url(rawInstanceUrl),
         LocalId(rawLocalId)
      )

      val rawInstanceUrl: String
         get() = instanceUrl.raw

      val rawLocalId: String
         get() = local.raw

      val dummy: Unit?
         get() = null
   }

   @JvmInline
   value class LocalId(val raw: String)

   data class NoCredential(
      val id: Id,
      val uri: String?,
      val createdTime: Instant?,
      val account: Cache<Account>?,
      val content: String?,
      val visibility: Visibility?,
      val isSensitive: Boolean?,
      val spoilerText: String?,
      val mediaAttachments: List<MediaAttachment>,
      val application: Application?,
      val mentions: List<Mention>,
      val hashtags: List<Hashtag>,
      val emojis: List<CustomEmoji>,
      val boostCount: Long?,
      val favoriteCount: Long?,
      val replyCount: Long?,
      val url: Url?,
      val repliedStatusId: Id?,
      val repliedAccountId: Account.Id?,
      val boostedStatus: Cache<NoCredential>?,
      val poll: Cache<Poll.NoCredential>?,
      val card: PreviewCard?,
      val language: String?,
      val text: String?,
      val editedTime: Instant?,
   ) {
      constructor(
         rawInstanceUrl: String,
         rawLocalId: String,
         uri: String?,
         createdTimeEpochMillis: Long?,
         account: Cache<Account>?,
         content: String?,
         rawVisibility: String?,
         isSensitive: Boolean?,
         spoilerText: String?,
         mediaAttachments: List<MediaAttachment>,
         application: Application?,
         mentions: List<Mention>,
         hashtags: List<Hashtag>,
         emojis: List<CustomEmoji>,
         boostCount: Long?,
         favoriteCount: Long?,
         replyCount: Long?,
         rawUrl: String?,
         rawRepliedStatusLocalId: String?,
         rawRepliedAccountLocalId: String?,
         boostedStatus: Cache<NoCredential>?,
         poll: Cache<Poll.NoCredential>?,
         card: PreviewCard?,
         language: String?,
         text: String?,
         editedTimeEpochMillis: Long?,
      ) : this(
         Id(Url(rawInstanceUrl), LocalId(rawLocalId)),
         uri,
         createdTimeEpochMillis?.let(Instant::fromEpochMilliseconds),
         account,
         content,
         rawVisibility?.let(::Visibility),
         isSensitive,
         spoilerText,
         mediaAttachments,
         application,
         mentions,
         hashtags,
         emojis,
         boostCount,
         favoriteCount,
         replyCount,
         rawUrl?.let(::Url),
         repliedStatusId = rawRepliedStatusLocalId?.let {
            Id(Url(rawInstanceUrl), LocalId(it))
         },
         repliedAccountId = rawRepliedAccountLocalId?.let {
            Account.Id(Url(rawInstanceUrl), Account.LocalId(it))
         },
         boostedStatus,
         poll,
         card,
         language,
         text,
         editedTimeEpochMillis?.let(Instant::fromEpochMilliseconds),
      )

      val rawInstanceUrl: String
         get() = id.instanceUrl.raw

      val rawLocalId: String
         get() = id.local.raw

      val createdTimeEpochMillis: Long?
         get() = createdTime?.toEpochMilliseconds()

      val rawVisibility: String?
         get() = visibility?.raw

      val rawUrl: String?
         get() = url?.raw

      val rawRepliedStatusLocalId: String?
         get() = repliedStatusId?.local?.raw

      val rawRepliedAccountLocalId: String?
         get() = repliedAccountId?.local?.value

      val editedTimeEpochMillis: Long?
         get() = editedTime?.toEpochMilliseconds()
   }

   @JvmInline
   value class Visibility(val raw: String) {
      companion object {
         val PUBLIC   = Visibility("public")
         val UNLISTED = Visibility("unlisted")
         val PRIVATE  = Visibility("private")
         val DIRECT   = Visibility("direct")
      }
   }

   data class Mention(
      val mentionedAccountId: Account.Id?,
      val mentionedAccountUsername: String?,
      val mentionedAccountUrl: Url?,
      val mentionedAccountAcct: String?,
   ) {
      constructor(
         rawInstanceUrl: String?,
         rawMentionedAccountLocalId: String?,
         mentionedAccountUsername: String?,
         rawMentionedAccountUrl: String?,
         mentionedAccountAcct: String?,
      ) : this(
         if (rawInstanceUrl == null || rawMentionedAccountLocalId == null) {
            null
         } else {
            Account.Id(
               Url(rawInstanceUrl),
               Account.LocalId(rawMentionedAccountLocalId)
            )
         },
         mentionedAccountUsername,
         rawMentionedAccountUrl?.let(::Url),
         mentionedAccountAcct,
      )

      val rawInstanceUrl: String?
         get() = mentionedAccountId?.instanceUrl?.raw

      val rawMentionedAccountLocalId: String?
         get() = mentionedAccountId?.local?.value

      val rawMentionedAccountUrl: String?
         get() = mentionedAccountUrl?.raw
   }

   data class Hashtag(
      val name: String?,
      val url: Url?,
   ) {
      constructor(
         name: String?,
         rawUrl: String?,
         @Suppress("UNUSED_PARAMETER")
         dummy: Unit?
      ) : this(
         name,
         rawUrl?.let(::Url),
      )

      val rawUrl: String?
         get() = url?.raw

      val dummy: Unit?
         get() = null
   }
}

tailrec fun Status.resolveBoostedStatus(): Status {
   if (boostedStatus == null) { return this }
   return boostedStatus.value.resolveBoostedStatus()
}

tailrec fun Status.NoCredential.resolveBoostedStatus(): Status.NoCredential {
   if (boostedStatus == null) { return this }
   return boostedStatus.value.resolveBoostedStatus()
}
