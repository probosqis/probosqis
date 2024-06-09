/*
 * Copyright 2024 wcaokaze
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

package com.wcaokaze.probosqis.testpages

import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import com.wcaokaze.probosqis.error.PError
import com.wcaokaze.probosqis.error.PErrorItemComposable
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("com.wcaokaze.probosqis.testpages.TestError")
class TestError(val time: Instant) : PError()

val testErrorComposable = PErrorItemComposable<TestError> { error ->
   val message = remember(error) {
      val timeZone = TimeZone.currentSystemDefault()
      val localDateTime = error.time.toLocalDateTime(timeZone)

      "TestError %d/%d/%d %02d:%02d".format(
         localDateTime.year, localDateTime.monthNumber, localDateTime.dayOfMonth,
         localDateTime.hour, localDateTime.minute
      )
   }

   Text(message)
}
