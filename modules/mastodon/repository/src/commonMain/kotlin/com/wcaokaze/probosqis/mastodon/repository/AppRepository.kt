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

package com.wcaokaze.probosqis.mastodon.repository

import com.wcaokaze.probosqis.ext.kotlin.Url
import com.wcaokaze.probosqis.mastodon.entity.Application
import com.wcaokaze.probosqis.mastodon.entity.CredentialAccount
import com.wcaokaze.probosqis.mastodon.entity.Instance
import com.wcaokaze.probosqis.mastodon.entity.Token
import com.wcaokaze.probosqis.panoptiqon.Cache
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.io.IOException

internal class InstanceCacheSerializer : KSerializer<Cache<Instance>> {
   override val descriptor: SerialDescriptor
      get() = Instance.serializer().descriptor

   override fun serialize(encoder: Encoder, value: Cache<Instance>) {
      Instance.serializer().serialize(encoder, value.value)
   }

   override fun deserialize(decoder: Decoder): Cache<Instance> {
      return Cache(Instance.serializer().deserialize(decoder))
   }
}

interface AppRepository {
   /**
    * @throws IOException
    */
   fun createApp(instance: Instance): Cache<Application>

   /**
    * @throws IOException
    */
   fun loadAppCache(instanceBaseUrl: Url): Cache<Application>

   /**
    * @throws IOException
    */
   fun getAuthorizeUrl(application: Application): Url

   /**
    * @throws IOException
    */
   fun getAuthorizeUrl(instance: Instance): Url {
      val appCache = try {
         loadAppCache(instance.url)
      } catch (_: Exception) {
         createApp(instance)
      }

      return getAuthorizeUrl(appCache.value)
   }

   /**
    * @throws IOException
    */
   fun getToken(application: Application, code: String): Token

   /**
    * @throws IOException
    */
   fun getCredentialAccount(token: Token): CredentialAccount
}
