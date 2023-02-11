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

import com.wcaokaze.probosqis.page.core.Column
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File

class JvmColumnRepository(
   allPageSerializers: List<PageSerializer<*>>,
   directory: File
) : ColumnRepository(allPageSerializers) {
   private val file = File(directory, "U61Jfjj954X8OrvZ")

   override fun writeColumn(column: Column) {
      file.outputStream().buffered().use {
         @OptIn(ExperimentalSerializationApi::class)
         json.encodeToStream(column, it)
      }
   }

   override fun loadColumn(): Column {
      return file.inputStream().buffered().use {
         @OptIn(ExperimentalSerializationApi::class)
         json.decodeFromStream(it)
      }
   }
}
