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
import com.wcaokaze.probosqis.capsiqum.page.test.buildTestStateSaver
import com.wcaokaze.probosqis.capsiqum.page.test.rememberTestStateSaver
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals

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

   @Test
   fun textField_domain_selected_afterPageStarted() {
      lateinit var state: UrlInputPageState

      rule.setContent {
         val page = UrlInputPage()
         state = UrlInputPageState(rememberTestStateSaver())

         urlInputPageComposable.contentComposable(
            page, state, WindowInsets(0)
         )
      }

      rule.runOnIdle {
         val inputUrl = state.inputUrl
         assertEquals(
            "https://",
            inputUrl.text.substring(0, inputUrl.selection.min)
         )
         assertEquals(
            "mastodon.social/",
            inputUrl.text.substring(inputUrl.selection.min, inputUrl.selection.max)
         )
         assertEquals(
            "",
            inputUrl.text.substring(inputUrl.selection.max)
         )
      }
   }
}
