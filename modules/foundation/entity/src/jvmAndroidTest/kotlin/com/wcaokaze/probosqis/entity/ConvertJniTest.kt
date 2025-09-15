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

package com.wcaokaze.probosqis.entity

import com.wcaokaze.probosqis.ext.kotlin.Url
import com.wcaokaze.probosqis.ext.kotlintest.loadNativeLib
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ImageConvertJniTest {
   init {
      loadNativeLib()
   }

   @Test
   fun image_rust2Kt() {
      val image = `image_rust2Kt$createImage`()
      assertEquals(Url("https://github.com/wcaokaze.png"), image.url)
      assertContentEquals(
         byteArrayOf(
            0xca.toByte(), 0xfe.toByte(), 0xba.toByte(), 0xbe.toByte(),
         ),
         image.bytes
      )
   }

   private external fun `image_rust2Kt$createImage`(): Image

   @Test
   fun image_kt2Rust() {
      val image = Image(
         Url("https://github.com/wcaokaze.png"),
         byteArrayOf(
            0xca.toByte(), 0xfe.toByte(), 0xba.toByte(), 0xbe.toByte(),
         )
      )
      `image_kt2Rust$assert`(image)
   }

   private external fun `image_kt2Rust$assert`(image: Image)
}
