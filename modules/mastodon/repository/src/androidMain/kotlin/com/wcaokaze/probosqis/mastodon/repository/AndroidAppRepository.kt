/*
 * Copyright 2024 wcaokaze
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

import android.content.Context
import com.wcaokaze.probosqis.mastodon.entity.Application
import com.wcaokaze.probosqis.mastodon.entity.Instance
import com.wcaokaze.probosqis.mastodon.entity.Token
import com.wcaokaze.probosqis.panoptiqon.Cache
import com.wcaokaze.probosqis.panoptiqon.TemporaryCacheApi
import com.wcaokaze.probosqis.panoptiqon.loadCache
import com.wcaokaze.probosqis.panoptiqon.saveCache
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import java.net.URLEncoder

class AndroidAppRepository(context: Context) : AppRepository {
   private val dir = File(context.filesDir, "fFDFXHfgze7i3Ihs")

   @TemporaryCacheApi
   override fun createApp(instance: Instance): Cache<Application> {
      val application = postApp(instance)

      val fileName = URLEncoder.encode(instance.url, "UTF-8")
      val file = File(dir, fileName)
      return saveCache(application, file, Json).asCache()
   }

   private external fun postApp(instance: Instance): Application

   @TemporaryCacheApi
   override fun loadAppCache(instanceBaseUrl: String): Cache<Application> {
      val fileName = URLEncoder.encode(instanceBaseUrl, "UTF-8")
      val file = File(dir, fileName)
      return loadCache<Application>(file, Json).asCache()
   }

   override fun getAuthorizeUrl(application: Application): String {
      return getAuthorizeUrl(
         application.instance,
         application.clientId ?: throw IOException()
      )
   }

   private external fun getAuthorizeUrl(instance: Cache<Instance>, clientId: String): String

   override fun getToken(application: Application, code: String): Token {
      return getToken(
         application.instance,
         code,
         application.clientId     ?: throw IOException(),
         application.clientSecret ?: throw IOException()
      )
   }

   private external fun getToken(
      instance: Cache<Instance>,
      code: String,
      clientId: String,
      clientSecret: String
   ): Token
}
