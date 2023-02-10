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

package com.wcaokaze.probosqis.page

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PageTest {
   @OptIn(ExperimentalSerializationApi::class)
   @Test
   fun serialization() {
      @Serializable
      @SerialName("com.wcaokaze.probosqis.page.APage")
      class APage(val i: Int) : Page()

      val json = Json {
         serializersModule = SerializersModule {
            polymorphic(Page::class) {
               subclass(APage::class)
            }
         }
      }

      val aPage = APage(42)
      val outputStream = ByteArrayOutputStream()
      outputStream.use { stream ->
         json.encodeToStream(aPage as Page, stream)
      }
      val bytes = outputStream.toByteArray()
      val inputStream = ByteArrayInputStream(bytes)
      val deserializedPage = inputStream.use { stream ->
         json.decodeFromStream<Page>(stream)
      }

      assertIs<APage>(deserializedPage)
      assertEquals(deserializedPage.i, aPage.i)
   }
}
