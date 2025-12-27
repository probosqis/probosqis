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

import com.wcaokaze.probosqis.ext.panoptiqon.MappedCache
import com.wcaokaze.probosqis.ext.panoptiqon.MappedWritableCache
import com.wcaokaze.probosqis.panoptiqon.Cache
import com.wcaokaze.probosqis.panoptiqon.WritableCache
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

   fun saveCredential(credential: Credential)
   fun loadAllCredentials(): WritableCache<List<Cache<Credential>>>
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

   /** @throws Exception */
   override fun saveCredential(credential: Credential) {
      synchronized(this) {
         val credentialJson = json.encodeToString(credential)
         val id = buildString {
            append(credential::class.qualifiedName)
            append('-')
            append(credential.id)
         }

         val serializedCredential = SerializedCredential(id, credentialJson)
         val credentialCache = savePanoptiqon(serializedCredential).asCache()

         val credentialListCache = try {
            loadAllCredentialsPanoptiqon()
         } catch (_: Exception) {
            val emptyList = CredentialList(emptyList())
            saveAllCredentialsPanoptiqon(emptyList)
         }

         credentialListCache.value += credentialCache
      }
   }

   /** @throws Exception */
   override fun loadAllCredentials(): WritableCache<List<Cache<Credential>>> {
      val origin = try {
         loadAllCredentialsPanoptiqon()
      } catch (_: Exception) {
         val emptyList = CredentialList(emptyList())
         saveAllCredentialsPanoptiqon(emptyList)
      }

      return CredentialListCache(json, origin)
   }

   protected abstract fun savePanoptiqon(
      credential: SerializedCredential
   ): WritableCache<SerializedCredential>

   protected abstract fun saveAllCredentialsPanoptiqon(
      credentialList: CredentialList
   ): WritableCache<CredentialList>

   protected abstract fun loadAllCredentialsPanoptiqon(): WritableCache<CredentialList>

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

data class SerializedCredential(
   val id: String,
   val json: String
)

data class CredentialList(
   val credentials: List<Cache<SerializedCredential>>
)

private class CredentialCache(
   private val json: Json,
   val origin: Cache<SerializedCredential>
) : MappedCache<SerializedCredential, Credential>(origin) {
   override fun map(value: SerializedCredential): Credential {
      return json.decodeFromString(value.json)
   }
}

private class CredentialListCache(
   private val json: Json,
   origin: WritableCache<CredentialList>
) : MappedWritableCache<CredentialList, List<Cache<Credential>>>(origin) {
   override fun map(value: CredentialList): List<Cache<Credential>> {
      return value.credentials.map { CredentialCache(json, it) }
   }

   override fun reverseMap(value: List<Cache<Credential>>): CredentialList {
      val originCacheList = value.map { (it as CredentialCache).origin }
      return CredentialList(originCacheList)
   }
}

private operator fun CredentialList.plus(
   cache: Cache<SerializedCredential>
) = CredentialList(
   credentials.filter { it.id != cache.id } + cache
)
