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
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

inline fun <reified C : Credential> credentialSerializer(
   noinline fileNameSupplier: (C) -> String
): CredentialRepository.CredentialSerializer<C> {
   return CredentialRepository.CredentialSerializer(
      C::class,
      serializer(),
      fileNameSupplier
   )
}

interface CredentialRepository {
   data class CredentialSerializer<C : Credential>(
      val credentialClass: KClass<C>,
      val serializer: KSerializer<C>,
      val fileNameSupplier: (C) -> String
   )

   suspend fun saveCredential(credential: Credential)
   suspend fun loadAllCredentials(): List<Cache<Credential>>
}

abstract class AbstractCredentialRepository
   internal constructor(
      private val allCredentialSerializers: List<CredentialRepository.CredentialSerializer<*>>
   )
   : CredentialRepository
{
   private fun <C : Credential> PolymorphicModuleBuilder<Credential>.subclass(
      credentialSerializer: CredentialRepository.CredentialSerializer<C>
   ) {
      subclass(credentialSerializer.credentialClass, credentialSerializer.serializer)
   }

   protected val json = Json {
      serializersModule = SerializersModule {
         polymorphic(Credential::class) {
            for (c in allCredentialSerializers) {
               subclass(c)
            }
         }
      }
   }

   protected fun getFileNameFor(credential: Credential): String {
      fun <C : Credential> impl(credential: C): String {
         @Suppress("UNCHECKED_CAST")
         val serializer = allCredentialSerializers
            .single { it.credentialClass == credential::class }
            as CredentialRepository.CredentialSerializer<C>

         return serializer.fileNameSupplier(credential)
      }

      return impl(credential)
   }
}
