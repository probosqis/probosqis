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

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

actual class Image(
   actual val url: String,
   actual val composeImageBitmap: ImageBitmap
) {
   val imageBytes: ByteArray
      get() = throw NotImplementedError()

   companion object {
      @JvmStatic
      fun fromBytes(url: String, imageBytes: ByteArray): Image? {
         return try {
            val bitmap = BitmapFactory
               .decodeByteArray(imageBytes, 0, imageBytes.size) ?: return null
            val composeImageBitmap = bitmap.asImageBitmap()
            Image(url, composeImageBitmap)
         } catch (_: Exception) {
            null
         }
      }
   }
}
