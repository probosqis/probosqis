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
import com.wcaokaze.probosqis.mastodon.entity.Account.ProfileField
import com.wcaokaze.probosqis.panoptiqon.Cache
import kotlinx.datetime.Instant

data class Account(
   val instance: Cache<Instance>,
   val id: Id,
   val username: String?,
   val acct: String?,
   val url: Url?,
   val displayName: String?,
   val profileNote: String?,
   val avatarImageUrl: Url?,
   val avatarStaticImageUrl: Url?,
   val headerImageUrl: Url?,
   val headerStaticImageUrl: Url?,
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
      rawInstanceUrl: String,
      rawLocalId: String,
      username: String?,
      acct: String?,
      rawUrl: String?,
      displayName: String?,
      profileNote: String?,
      rawAvatarImageUrl: String?,
      rawAvatarStaticImageUrl: String?,
      rawHeaderImageUrl: String?,
      rawHeaderStaticImageUrl: String?,
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
      Id(Url(rawInstanceUrl), LocalId(rawLocalId)),
      username,
      acct,
      rawUrl?.let(::Url),
      displayName,
      profileNote,
      rawAvatarImageUrl?.let(::Url),
      rawAvatarStaticImageUrl?.let(::Url),
      rawHeaderImageUrl?.let(::Url),
      rawHeaderStaticImageUrl?.let(::Url),
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
      createdTime?.let(Instant::fromEpochMilliseconds),
      lastStatusPostTime?.let(Instant::fromEpochMilliseconds),
      statusCount,
      followerCount,
      followeeCount,
   )

   data class Id(val instanceUrl: Url, val local: LocalId)

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

   val rawInstanceUrl: String
      get() = id.instanceUrl.raw

   val rawLocalId: String
      get() = id.local.value

   val rawUrl: String?
      get() = url?.raw

   val rawAvatarImageUrl: String?
      get() = avatarImageUrl?.raw

   val rawAvatarStaticImageUrl: String?
      get() = avatarStaticImageUrl?.raw

   val rawHeaderImageUrl: String?
      get() = headerImageUrl?.raw

   val rawHeaderStaticImageUrl: String?
      get() = headerStaticImageUrl?.raw

   val createdTimeEpochMillis: Long?
      get() = createdTime?.toEpochMilliseconds()

   val lastStatusPostTimeEpochMillis: Long?
      get() = lastStatusPostTime?.toEpochMilliseconds()
}

data class CredentialAccount(
   val id: Account.Id,
   val account: Cache<Account>,
   val rawProfileNote: String?,
   val rawProfileFields: List<ProfileField>,
   val defaultPostVisibility: Status.Visibility?,
   val defaultPostSensitivity: Boolean?,
   val defaultPostLanguage: String?,
   val followRequestCount: Long?,
   val role: Role?,
) {
   constructor(
      rawInstanceUrl: String,
      rawLocalId: String,
      account: Cache<Account>,
      rawProfileNote: String?,
      rawProfileFields: List<ProfileField>,
      rawDefaultPostVisibility: String?,
      defaultPostSensitivity: Boolean?,
      defaultPostLanguage: String?,
      followRequestCount: Long?,
      role: Role?,
      @Suppress("UNUSED_PARAMETER")
      dummy: Unit?
   ) : this(
      Account.Id(Url(rawInstanceUrl), Account.LocalId(rawLocalId)),
      account,
      rawProfileNote,
      rawProfileFields,
      rawDefaultPostVisibility?.let(Status::Visibility),
      defaultPostSensitivity,
      defaultPostLanguage,
      followRequestCount,
      role,
   )

   val rawInstanceUrl: String
      get() = id.instanceUrl.raw

   val rawLocalId: String
      get() = id.local.value

   val rawDefaultPostVisibility: String?
      get() = defaultPostVisibility?.raw

   val dummy: Unit?
      get() = null
}

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
