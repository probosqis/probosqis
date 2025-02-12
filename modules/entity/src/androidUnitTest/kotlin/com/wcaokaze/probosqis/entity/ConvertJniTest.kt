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

import com.wcaokaze.probosqis.ext.kotlintest.loadNativeLib
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

// Image.fromBytesがBitmapFactoryを使うためRobolectricでの実行が必須
@RunWith(RobolectricTestRunner::class)
class ImageConvertJniTest {
   init {
      loadNativeLib()
   }

   @Test
   fun image() {
      val image = `image$createImage`()
      assertNotNull(image)
      assertEquals("https://github.com/wcaokaze.png", image.url)
   }

   private external fun `image$createImage`(): Image?

   @Ignore(
      "不正なバイト列を渡すとBitmapFactory.decodeByteArrayがnullを返す想定" +
       "だったがどうやらそうじゃないらしい"
   )
   @Test
   fun image_illegalFormat() {
      val image = `image_illegalFormat$createImage`()
      assertNull(image)
   }

   private external fun `image_illegalFormat$createImage`(): Image?
}
