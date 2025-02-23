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

import com.wcaokaze.probosqis.mastodon.entity.Account.ProfileField
import com.wcaokaze.probosqis.panoptiqon.Cache
import kotlinx.datetime.Instant

data class Account(
   val instance: Cache<Instance>,
   val localId: LocalId,
   val username: String?,
   val acct: String?,
   val url: String?,
   val displayName: String?,
   val profileNote: String?,
   val avatarImageUrl: String?,
   val avatarStaticImageUrl: String?,
   val headerImageUrl: String?,
   val headerStaticImageUrl: String?,
   val isLocked: Boolean?,
   val profileFields: List<ProfileField>,
   val emojisInProfile: List<CustomEmoji>,
   val isBot: Boolean?,
   val isGroup: Boolean?,
   val isDiscoverable: Boolean?,
   val isNoindex: Boolean?,
   val movedTo: Cache<Account>?,
   val isSuspended: Boolean?,
   val isLimited: Boolean?,
   val createdTime: Instant?,
   val lastStatusPostTime: Instant?,
   val statusCount: Long?,
   val followerCount: Long?,
   val followeeCount: Long?,
) {
   constructor(
      instance: Cache<Instance>,
      rawLocalId: String,
      username: String?,
      acct: String?,
      url: String?,
      displayName: String?,
      profileNote: String?,
      avatarImageUrl: String?,
      avatarStaticImageUrl: String?,
      headerImageUrl: String?,
      headerStaticImageUrl: String?,
      isLocked: Boolean?,
      profileFields: List<ProfileField>,
      emojisInProfile: List<CustomEmoji>,
      isBot: Boolean?,
      isGroup: Boolean?,
      isDiscoverable: Boolean?,
      isNoindex: Boolean?,
      movedTo: Cache<Account>?,
      isSuspended: Boolean?,
      isLimited: Boolean?,
      createdTime: Long?,
      lastStatusPostTime: Long?,
      statusCount: Long?,
      followerCount: Long?,
      followeeCount: Long?,
   ) : this(
      instance,
      LocalId(rawLocalId),
      username,
      acct,
      url,
      displayName,
      profileNote,
      avatarImageUrl,
      avatarStaticImageUrl,
      headerImageUrl,
      headerStaticImageUrl,
      isLocked,
      profileFields,
      emojisInProfile,
      isBot,
      isGroup,
      isDiscoverable,
      isNoindex,
      movedTo,
      isSuspended,
      isLimited,
      createdTime?.let { Instant.fromEpochMilliseconds(it) },
      lastStatusPostTime?.let { Instant.fromEpochMilliseconds(it) },
      statusCount,
      followerCount,
      followeeCount,
   )

   @JvmInline
   value class LocalId(val value: String)

   data class ProfileField(
      val name: String?,
      val value: String?,
      val verifiedTime: Instant?,
   ) {
      constructor(
         name: String?,
         value: String?,
         verifiedTime: Long?,
      ) : this(
         name, value, verifiedTime?.let { Instant.fromEpochMilliseconds(it) },
      )

      val verifiedTimeEpochMillis: Long?
         get() = verifiedTime?.toEpochMilliseconds()
   }

   val rawLocalId: String
      get() = localId.value

   val createdTimeEpochMillis: Long?
      get() = createdTime?.toEpochMilliseconds()

   val lastStatusPostTimeEpochMillis: Long?
      get() = lastStatusPostTime?.toEpochMilliseconds()
}

data class CredentialAccount(
   val account: Cache<Account>,
   val rawProfileNote: String?,
   val rawProfileFields: List<ProfileField>,
   val defaultPostVisibility: Status.Visibility?,
   val defaultPostSensitivity: Boolean?,
   val defaultPostLanguage: String?,
   val followRequestCount: Long?,
   val role: Role?,
)

data class RelationalAccount(
   val account: Cache<Account>,
   val muteExpireTime: Instant?,
) {
   constructor(
      account: Cache<Account>,
      muteExpireTime: Long?,
   ) : this(
      account,
      muteExpireTime?.let { Instant.fromEpochMilliseconds(it) },
   )

   val muteExpireTimeEpochMillis: Long?
      get() = muteExpireTime?.toEpochMilliseconds()
}
