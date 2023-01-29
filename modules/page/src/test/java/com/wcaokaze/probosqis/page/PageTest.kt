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
import kotlinx.serialization.json.*
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(JUnit4::class)
class PageTest {
   @OptIn(ExperimentalSerializationApi::class)
   @Test
   fun serialization() {
      val page = Page(42)

      val outputStream = ByteArrayOutputStream()
      outputStream.use { stream ->
         Json.encodeToStream(page, stream)
      }
      val bytes = outputStream.toByteArray()
      val inputStream = ByteArrayInputStream(bytes)
      val jsonElement = inputStream.use { stream ->
         Json.decodeFromStream<JsonElement>(stream)
      }
      val page2 = Json.decodeFromJsonElement<Page>(jsonElement)

      assertEquals(page.value, page2.value)
   }
}
