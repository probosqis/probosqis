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

package com.wcaokaze.probosqis.foundation.page

import kotlin.test.Test
import kotlin.test.assertEquals

class PageLayoutInfoTest {
   @Test
   fun globalIds_companionObjectEqualsSubclasses() {
      val someLayoutIds = object : PageLayoutIds() {}

      assertEquals(someLayoutIds.root,       PageLayoutIds.root)
      assertEquals(someLayoutIds.background, PageLayoutIds.background)
      assertEquals(someLayoutIds.content,    PageLayoutIds.content)
   }
}
