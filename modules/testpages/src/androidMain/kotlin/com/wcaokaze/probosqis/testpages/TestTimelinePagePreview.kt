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

package com.wcaokaze.probosqis.testpages

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.wcaokaze.probosqis.ext.compose.layout.SafeDrawingWindowInsetsProvider
import com.wcaokaze.probosqis.foundation.page.preview.PPagePreview
import com.wcaokaze.probosqis.foundation.page.preview.PPageTransitionPreview

@Preview
@Composable
private fun TestTimelinePagePreview(
   @PreviewParameter(SafeDrawingWindowInsetsProvider::class)
   safeDrawingWindowInsets: WindowInsets
) {
   PPagePreview(
      page = TestTimelinePage(),
      pageComposable = testTimelinePageComposable,
      safeDrawingWindowInsets
   )
}

@Preview
@Composable
private fun TestTimelinePageToTestNotePageTransitionPreview() {
   PPageTransitionPreview(
      parentPage = TestTimelinePage(),
      childPage = TestNotePage(2),
      parentPageStateModification = {
         clickedNoteIndex = 2
      },
      parentPageComposable = testTimelinePageComposable,
      childPageComposable = testNotePageComposable
   )
}
