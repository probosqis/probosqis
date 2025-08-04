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
import com.wcaokaze.probosqis.mastodon.entity.Account
import com.wcaokaze.probosqis.mastodon.entity.Application
import com.wcaokaze.probosqis.mastodon.entity.CredentialAccount
import com.wcaokaze.probosqis.mastodon.entity.Instance
import com.wcaokaze.probosqis.mastodon.entity.Token
import com.wcaokaze.probosqis.panoptiqon.Cache
import com.wcaokaze.probosqis.panoptiqon.Repository
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import java.io.File
import java.io.IOException

class DesktopAppRepository(
   appDataDir: File,
   private val instanceCacheRepository: Repository<Instance>,
   private val applicationCacheRepository: Repository<Application>,
   private val accountCacheRepository: Repository<Account>,
   private val credentialAccountCacheRepository: Repository<CredentialAccount>
) : AppRepository {
   private val dir = File(appDataDir, "fFDFXHfgze7i3Ihs")
      .also { dir ->
         if (dir.exists()) {
            require(dir.isDirectory)
         } else {
            if (!dir.mkdirs()) { throw IOException() }
         }
      }

   private val json = Json {
      serializersModule = SerializersModule {
         contextual(InstanceCacheSerializer())
      }
   }

   override fun loadAppCache(instanceBaseUrl: Url): Cache<Application> {
      return loadAppCache(applicationCacheRepository, instanceBaseUrl.raw)
   }

   private external fun loadAppCache(
      applicationCacheRepo: Repository<Application>,
      instanceBaseUrl: String
   ): Cache<Application>

   override fun getAuthorizeUrl(instance: Instance): Url {
      val authorizeUrl = getAuthorizeUrl(
         instance, instanceCacheRepository, applicationCacheRepository
      )

      return Url(authorizeUrl)
   }

   private external fun getAuthorizeUrl(
      instance: Instance,
      instanceCacheRepo: Repository<Instance>,
      applicationCacheRepo: Repository<Application>
   ): String

   override fun getToken(application: Application, code: String): Token {
      return getToken(
         application.instance,
         code,
         application.clientId     ?: throw IOException(),
         application.clientSecret ?: throw IOException(),
         accountCacheRepository, credentialAccountCacheRepository
      )
   }

   private external fun getToken(
      instance: Cache<Instance>,
      code: String,
      clientId: String,
      clientSecret: String,
      accountCacheRepo: Repository<Account>,
      credentialAccountCacheRepo: Repository<CredentialAccount>
   ): Token

   override fun getCredentialAccount(token: Token): Cache<CredentialAccount> {
      return getCredentialAccount(
         token, accountCacheRepository, credentialAccountCacheRepository
      )
   }

   private external fun getCredentialAccount(
      token: Token,
      accountCacheRepo: Repository<Account>,
      credentialAccountCacheRepo: Repository<CredentialAccount>
   ): Cache<CredentialAccount>
}
