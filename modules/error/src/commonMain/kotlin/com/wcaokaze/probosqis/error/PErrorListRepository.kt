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

package com.wcaokaze.probosqis.error

import com.wcaokaze.probosqis.app.pagedeck.PageStackRepository
import com.wcaokaze.probosqis.capsiqum.page.Page
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

inline fun <reified E : PError>
      errorSerializer(): PErrorListRepository.PErrorSerializer<E>
{
   return PErrorListRepository.PErrorSerializer(E::class, serializer())
}

interface PErrorListRepository {
   data class PErrorSerializer<E : PError>(
      val errorClass: KClass<E>,
      val serializer: KSerializer<E>
   )

   fun saveErrorList(errorList: List<RaisedError>): WritableCache<List<RaisedError>>
   fun loadErrorList(): WritableCache<List<RaisedError>>
}

abstract class AbstractPErrorListRepository
   internal constructor(
      allErrorSerializers: List<PErrorListRepository.PErrorSerializer<*>>,
      allPageSerializers: List<PageStackRepository.PageSerializer<*>>
   )
   : PErrorListRepository
{
   private fun <E : PError> PolymorphicModuleBuilder<PError>.subclass(
      errorSerializer: PErrorListRepository.PErrorSerializer<E>
   ) {
      subclass(errorSerializer.errorClass, errorSerializer.serializer)
   }

   private fun <P : Page> PolymorphicModuleBuilder<Page>.subclass(
      pageSerializer: PageStackRepository.PageSerializer<P>
   ) {
      subclass(pageSerializer.pageClass, pageSerializer.serializer)
   }

   protected val json = Json {
      serializersModule = SerializersModule {
         polymorphic(PError::class) {
            for (s in allErrorSerializers) {
               subclass(s)
            }
         }

         polymorphic(Page::class) {
            for (s in allPageSerializers) {
               subclass(s)
            }
         }
      }
   }
}
