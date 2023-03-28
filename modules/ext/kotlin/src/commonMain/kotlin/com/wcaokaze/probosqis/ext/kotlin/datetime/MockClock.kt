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

package com.wcaokaze.probosqis.ext.kotlin.datetime

import kotlinx.datetime.*

class MockClock(private val now: Instant) : Clock {
   constructor(
      year: Int = 2000,
      month: Month = Month.JANUARY,
      dayOfMonth: Int = 1,
      hour: Int = 0,
      minute: Int = 0,
      second: Int = 0,
      nanosecond: Int = 0
   ) : this(
      LocalDateTime(year, month, dayOfMonth, hour, minute, second, nanosecond)
         .toInstant(TimeZone.UTC)
   )

   override fun now() = now
}
