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

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class CredentialRepositoryTest {
   private val testDir = File(".pageStackRepositoryTest")

   private lateinit var credentialRepository: CredentialRepository

   @Serializable
   data class StringCredential(val id: String) : Credential()

   @Serializable
   data class IntCredential(val id: Int) : Credential()

   private val stringCredentialSerializer
      = credentialSerializer<StringCredential> { "string" + it.id }

   private val intCredentialSerializer
      = credentialSerializer<IntCredential> { "int" + it.id }

   @BeforeTest
   fun initializeRepository() {
      credentialRepository = DesktopCredentialRepository(
         allCredentialSerializers = listOf(
            stringCredentialSerializer,
            intCredentialSerializer,
         ),
         testDir
      )
   }

   @AfterTest
   fun deleteRepository() {
      testDir.deleteRecursively()
   }

   @Test
   fun loadAllCredentials_failsIfFileNotFound() = runTest {
      assertFails { credentialRepository.loadAllCredentials() }
   }

   @Test
   fun saveLoad() = runTest {
      assertFails { credentialRepository.loadAllCredentials() }

      credentialRepository.saveCredential(StringCredential("1"))
      assertEquals(
         listOf(
            StringCredential("1"),
         ),
         credentialRepository.loadAllCredentials().map { it.value }
      )

      credentialRepository.saveCredential(StringCredential("2"))
      assertEquals(
         listOf(
            StringCredential("1"),
            StringCredential("2"),
         ),
         credentialRepository.loadAllCredentials().map { it.value }
      )

      credentialRepository.saveCredential(IntCredential(3))
      assertEquals(
         listOf(
            StringCredential("1"),
            StringCredential("2"),
            IntCredential(3),
         ),
         credentialRepository.loadAllCredentials().map { it.value }
      )
   }
}
