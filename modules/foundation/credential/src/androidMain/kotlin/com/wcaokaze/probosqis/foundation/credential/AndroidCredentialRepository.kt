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

package com.wcaokaze.probosqis.foundation.credential

import com.wcaokaze.probosqis.panoptiqon.Repository
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import java.io.File

class AndroidCredentialRepository(
   appDataDir: File,
   allCredentialSerializers: List<CredentialRepository.CredentialSerializer<*>>
) : AbstractCredentialRepository(allCredentialSerializers) {
   private val panoptiqonCredentialRepository
       = createCredentialRepository(appDataDir.absolutePath)
   private val panoptiqonCredentialListRepository
       = createCredentialListRepository(appDataDir.absolutePath)

   override fun savePanoptiqon(
      credential: SerializedCredential
   ): WritableCache<SerializedCredential> {
      return panoptiqonCredentialRepository.save(credential)
   }

   override fun saveAllCredentialsPanoptiqon(
      credentialList: CredentialList
   ): WritableCache<CredentialList> {
      return panoptiqonCredentialListRepository.save(credentialList)
   }

   override fun loadAllCredentialsPanoptiqon(): WritableCache<CredentialList> {
      return panoptiqonCredentialListRepository.load(Unit)
   }
}

private external fun createCredentialRepository(
   dataDirPath: String
): Repository<String, SerializedCredential>

private external fun createCredentialListRepository(
   dataDirPath: String
): Repository<Unit, CredentialList>
