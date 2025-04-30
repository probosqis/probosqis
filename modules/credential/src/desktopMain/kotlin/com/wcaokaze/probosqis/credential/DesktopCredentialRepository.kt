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

package com.wcaokaze.probosqis.credential

import com.wcaokaze.probosqis.panoptiqon.Cache
import com.wcaokaze.probosqis.panoptiqon.TemporaryCacheApi
import com.wcaokaze.probosqis.panoptiqon.loadCache
import com.wcaokaze.probosqis.panoptiqon.saveCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class DesktopCredentialRepository(
   allCredentialSerializers: List<CredentialRepository.CredentialSerializer<*>>,
   directory: File
) : AbstractCredentialRepository(allCredentialSerializers) {
   private val mutex = Mutex()

   private val dir = File(directory, "YeNl4QfY6KDSixTZ")
      .also { dir ->
         if (dir.exists()) {
            require(dir.isDirectory)
         } else {
            if (!dir.mkdirs()) { throw IOException() }
         }
      }

   private val credentialListFile = File(dir, "pI9mnCtxBMhYGJpJ")

   /** @throws IOException */
   @TemporaryCacheApi
   override suspend fun saveCredential(credential: Credential) {
      withContext(Dispatchers.IO) {
         mutex.withLock {
            val fileName = getFileNameFor(credential)
            val file = File(dir, fileName)
            saveCache(credential, file, json)

            if (!credentialListFile.exists() || credentialListFile.length() == 0L) {
               credentialListFile.writeText(fileName)
            } else {
               credentialListFile.appendText("\n" + fileName)
            }
         }
      }
   }

   /** @throws IOException */
   @TemporaryCacheApi
   override suspend fun loadAllCredentials(): List<Cache<Credential>> {
      return withContext(Dispatchers.IO) {
         mutex.withLock {
            val fileNames = credentialListFile.readLines()

            fileNames.map {
               val file = File(dir, it)
               loadCache<Credential>(file, json).asCache()
            }
         }
      }
   }
}
