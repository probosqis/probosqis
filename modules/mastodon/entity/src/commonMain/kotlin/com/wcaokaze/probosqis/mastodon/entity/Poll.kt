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

data class Poll(
   val id: Id,
   val noCredential: Cache<NoCredential>,
   val isVoted: Boolean?,
   val votedOptions: List<Long>,
) {
   constructor(
      rawInstanceUrl: String,
      rawLocalId: String,
      noCredential: Cache<NoCredential>,
      isVoted: Boolean?,
      votedOptions: List<Long>,
   ) : this(
      Id(Url(rawInstanceUrl), LocalId(rawLocalId)),
      noCredential,
      isVoted,
      votedOptions,
   )

   val rawInstanceUrl: String
      get() = id.instanceUrl.raw

   val rawLocalId: String
      get() = id.local.raw

   data class Id(val instanceUrl: Url, val local: LocalId)

   @JvmInline
   value class LocalId(val raw: String)

   data class NoCredential(
      val id: Id,
      val expireTime: Instant?,
      val isExpired: Boolean?,
      val allowsMultipleChoices: Boolean?,
      val voteCount: Long?,
      val voterCount: Long?,
      val pollOptions: List<Option>,
      val emojis: List<CustomEmoji>,
   ) {
      constructor(
         rawInstanceUrl: String,
         rawLocalId: String,
         expireTimeEpochMillis: Long?,
         isExpired: Boolean?,
         allowsMultipleChoices: Boolean?,
         voteCount: Long?,
         voterCount: Long?,
         pollOptions: List<Option>,
         emojis: List<CustomEmoji>,
      ) : this(
         Id(Url(rawInstanceUrl), LocalId(rawLocalId)),
         expireTimeEpochMillis?.let(Instant::fromEpochMilliseconds),
         isExpired,
         allowsMultipleChoices,
         voteCount,
         voterCount,
         pollOptions,
         emojis,
      )

      val rawInstanceUrl: String
         get() = id.instanceUrl.raw

      val rawLocalId: String
         get() = id.local.raw

      val expireTimeEpochMillis: Long?
         get() = expireTime?.toEpochMilliseconds()
   }

   data class Option(
      val title: String?,
      val voteCount: Long?,
   )
}
