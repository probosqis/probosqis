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

import com.wcaokaze.probosqis.ext.kotlintest.loadNativeLib
import com.wcaokaze.probosqis.panoptiqon.Repository
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals

class CredentialRepositoryTest {
   init {
      loadNativeLib()
   }

   @Serializable
   data class StringCredential(val token: String) : Credential() {
      override val id: String
         get() = token
   }

   @Serializable
   data class IntCredential(val token: Int) : Credential() {
      override val id: String
         get() = token.toString()
   }

   private val stringCredentialSerializer
      = credentialSerializer<StringCredential> { "string" + it.id }

   private val intCredentialSerializer
      = credentialSerializer<IntCredential> { "int" + it.id }

   private inner class CredentialRepository : AbstractCredentialRepository(
      allCredentialSerializers = listOf(
         stringCredentialSerializer,
         intCredentialSerializer,
      )
   ) {
      private val panoptiqonCredentialRepository: Repository<String, SerializedCredential>
      private val panoptiqonCredentialListRepository: Repository<Unit, CredentialList>

      init {
         val repositories = createRepositories()
         panoptiqonCredentialRepository     = repositories.first
         panoptiqonCredentialListRepository = repositories.second
      }

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

      private external fun createRepositories(
      ): Pair<Repository<String, SerializedCredential>, Repository<Unit, CredentialList>>
   }

   @Test
   fun loadAllCredentials_emptyIfFileNotFound() {
      val credentialRepository = CredentialRepository()
      assertEquals(
         emptyList(),
         credentialRepository.loadAllCredentials().value
      )
   }

   @Test
   fun saveLoad() {
      val credentialRepository = CredentialRepository()

      assertEquals(
         emptyList(),
         credentialRepository.loadAllCredentials().value
      )

      credentialRepository.saveCredential(StringCredential("1"))
      assertEquals(
         listOf(
            StringCredential("1"),
         ),
         credentialRepository.loadAllCredentials().value.map { it.value }
      )

      credentialRepository.saveCredential(StringCredential("2"))
      assertEquals(
         listOf(
            StringCredential("1"),
            StringCredential("2"),
         ),
         credentialRepository.loadAllCredentials().value.map { it.value }
      )

      credentialRepository.saveCredential(IntCredential(3))
      assertEquals(
         listOf(
            StringCredential("1"),
            StringCredential("2"),
            IntCredential(3),
         ),
         credentialRepository.loadAllCredentials().value.map { it.value }
      )
   }
}
