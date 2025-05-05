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

package com.wcaokaze.probosqis.mastodon.entity

import com.wcaokaze.probosqis.credential.Credential
import com.wcaokaze.probosqis.ext.kotlin.Url
import com.wcaokaze.probosqis.panoptiqon.Cache
import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.NothingSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

private object InstanceCacheSerializer : KSerializer<Cache<Instance>> {
   override val descriptor = SerialDescriptor(
      "InstanceCache", Instance.serializer().descriptor
   )

   override fun serialize(encoder: Encoder, value: Cache<Instance>) {
      Instance.serializer().serialize(encoder, value.value)
   }

   override fun deserialize(decoder: Decoder): Cache<Instance> {
      return Cache(Instance.serializer().deserialize(decoder))
   }
}

private object AccountCacheSerializer : KSerializer<Cache<CredentialAccount>?> {
   override val descriptor = SerialDescriptor(
      "CredentialAccountCache",
      @OptIn(ExperimentalSerializationApi::class)
      NothingSerializer().descriptor
   )

   override fun serialize(encoder: Encoder, value: Cache<CredentialAccount>?) {
      @OptIn(ExperimentalSerializationApi::class)
      encoder.encodeNull()
   }

   override fun deserialize(decoder: Decoder): Cache<CredentialAccount>? {
      @OptIn(ExperimentalSerializationApi::class)
      return decoder.decodeNull()
   }
}

@Serializable
data class Token(
   @Serializable(with = InstanceCacheSerializer::class)
   val instance: Cache<Instance>,
   @Serializable(with = AccountCacheSerializer::class)
   val account: Cache<CredentialAccount>?,
   val accountId: Account.Id,
   val accessToken: String,
   val tokenType: String,
   val scope: String,
   val createdAt: Instant,
): Credential() {
   constructor(
      instance: Cache<Instance>,
      account: Cache<CredentialAccount>?,
      rawInstanceUrl: String,
      rawLocalId: String,
      accessToken: String,
      tokenType: String,
      scope: String,
      createdAtEpochMillis: Long,
   ) : this(
      instance,
      account,
      Account.Id(Url(rawInstanceUrl), Account.LocalId(rawLocalId)),
      accessToken,
      tokenType,
      scope,
      Instant.fromEpochMilliseconds(createdAtEpochMillis),
   )

   val rawInstanceUrl: String
      get() = accountId.instanceUrl.raw

   val rawLocalId: String
      get() = accountId.local.value

   val createdAtEpochMillis: Long
      get() = createdAt.toEpochMilliseconds()
}
