/*
 * Copyright 2023 wcaokaze
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

package com.wcaokaze.probosqis.page.perpetuation

import com.wcaokaze.probosqis.cache.core.WritableCache
import com.wcaokaze.probosqis.page.core.ColumnBoard
import com.wcaokaze.probosqis.page.core.Page
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

inline fun <reified P : Page>
      pageSerializer(): ColumnBoardRepository.PageSerializer<P>
{
   return ColumnBoardRepository.PageSerializer(P::class, serializer())
}

interface ColumnBoardRepository {
   data class PageSerializer<P : Page>(
      val pageClass: KClass<P>,
      val serializer: KSerializer<P>
   )

   fun saveColumnBoard(columnBoard: ColumnBoard): WritableCache<ColumnBoard>
   fun loadColumnBoard(): WritableCache<ColumnBoard>
}

abstract class AbstractColumnBoardRepository
   internal constructor(
      allPageSerializers: List<ColumnBoardRepository.PageSerializer<*>>
   )
   : ColumnBoardRepository
{
   private fun <P : Page> PolymorphicModuleBuilder<Page>.subclass(
      pageSerializer: ColumnBoardRepository.PageSerializer<P>
   ) {
      subclass(pageSerializer.pageClass, pageSerializer.serializer)
   }

   protected val json = Json {
      serializersModule = SerializersModule {
         polymorphic(Page::class) {
            for (s in allPageSerializers) {
               subclass(s)
            }
         }
      }
   }
}
