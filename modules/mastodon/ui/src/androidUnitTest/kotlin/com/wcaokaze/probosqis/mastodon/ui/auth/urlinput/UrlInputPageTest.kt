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

package com.wcaokaze.probosqis.mastodon.ui.auth.urlinput

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.wcaokaze.probosqis.capsiqum.page.test.rememberTestStateSaver
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
class UrlInputPageTest {
   @get:Rule
   val rule = createComposeRule()

   @Test
   fun textField_focused_afterPageStarted() {
      rule.setContent {
         val page = UrlInputPage()
         val state = UrlInputPageState(rememberTestStateSaver())

         urlInputPageComposable.contentComposable(
            page, state, WindowInsets(0)
         )
      }

      rule.onNodeWithText("https://mastodon.social/")
         .assertIsFocused()
   }
}
