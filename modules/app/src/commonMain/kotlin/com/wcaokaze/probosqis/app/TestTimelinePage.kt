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

package com.wcaokaze.probosqis.app

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wcaokaze.probosqis.page.Page
import com.wcaokaze.probosqis.page.transition.PageLayoutIds
import com.wcaokaze.probosqis.page.transition.PageLayoutInfo
import com.wcaokaze.probosqis.page.PageStackState
import com.wcaokaze.probosqis.page.PageState
import com.wcaokaze.probosqis.page.transition.SharedElementAnimations
import com.wcaokaze.probosqis.page.transition.SharedElementAnimatorElement
import com.wcaokaze.probosqis.page.transition.animatePosition
import com.wcaokaze.probosqis.page.transition.animateScale
import com.wcaokaze.probosqis.page.pageComposable
import com.wcaokaze.probosqis.page.pageStateFactory
import com.wcaokaze.probosqis.page.transition.sharedElement
import com.wcaokaze.probosqis.page.transition.transitionElement
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlin.math.pow

@Serializable
@SerialName("com.wcaokaze.probosqis.app.TestTimelinePage")
class TestTimelinePage : Page()

@Stable
class TestTimelinePageState(stateSaver: StateSaver) : PageState() {
   val lazyListState by stateSaver
      .save("lazyListState", LazyListState.Saver) { LazyListState() }

   val notes: List<Int> by stateSaver
      .save("notes", ListSerializer(Int.serializer())) { (0..200).toList() }

   var clickedNoteIndex: Int? by stateSaver
      .save("clickedNoteIndex", Int.serializer().nullable) { null }
}

val testTimelinePageComposable = pageComposable<TestTimelinePage, TestTimelinePageState>(
   pageStateFactory { _, stateSaver -> TestTimelinePageState(stateSaver) },
   content = { _, pageState, pageStackState ->
      TestTimeline(pageState, pageStackState)
   },
   header = { _, _, _ -> },
   footer = null,
   pageTransitions = {
      transitionTo<TestNotePage>(
         enter = {
            val currentIds = TestTimelinePageLayoutIds
            val targetIds  = TestNotePageLayoutIds

            targetPageElement(targetIds.background) {
               val a by transition.animateFloat(
                  transitionSpec = {
                     tween(easing = { (it - 1.0f).pow(3.0f) + 1.0f })
                  },
                  label = "background-Alpha",
               ) {
                  if (it.isTargetPage) { 1.0f } else { 0.0f }
               }
               val p by animatePosition(
                  currentIds.clickedNote, targetIds.background, label = "background-Offset")
               val s by animateScale(
                  currentIds.clickedNote, targetIds.background, label = "background-Scale")

               Modifier.graphicsLayer {
                  transformOrigin = TransformOrigin(0.0f, 0.0f)
                  alpha = a
                  translationY = p.y
                  scaleY = s.scaleY
               }
            }

            sharedElement(
               currentIds.clickedNoteAccountIcon,
               targetIds.accountIcon,
               label = "accountIcon",
               SharedElementAnimatorElement.Target,
               SharedElementAnimations.Offset
            )

            sharedElement(
               currentIds.clickedNoteAccountNameRow,
               targetIds.accountNameColumn,
               label = "accountName"
            )

            sharedElement(
               currentIds.clickedNoteContentText,
               targetIds.contentText,
               label = "content"
            )
         },
         exit = {
            val currentIds = TestNotePageLayoutIds
            val targetIds  = TestTimelinePageLayoutIds

            currentPageElement(currentIds.background) {
               val a by transition.animateFloat(
                  transitionSpec = {
                     tween(easing = { it.pow(3.0f) })
                  },
                  label = "background-Alpha",
               ) {
                  if (it.isCurrentPage) { 1.0f } else { 0.0f }
               }
               val p by animatePosition(
                  currentIds.background, targetIds.clickedNote, label = "background-Offset")
               val s by animateScale(
                  currentIds.background, targetIds.clickedNote, label = "background-Scale")

               Modifier.graphicsLayer {
                  transformOrigin = TransformOrigin(0.0f, 0.0f)
                  alpha = a
                  translationY = p.y
                  scaleY = s.scaleY
               }
            }

            sharedElement(
               currentIds.accountIcon,
               targetIds.clickedNoteAccountIcon,
               label = "accountIcon",
               SharedElementAnimatorElement.Current,
               SharedElementAnimations.Offset
            )

            sharedElement(
               currentIds.accountNameColumn,
               targetIds.clickedNoteAccountNameRow,
               label = "accountName"
            )

            sharedElement(
               currentIds.contentText,
               targetIds.clickedNoteContentText,
               label = "content"
            )
         }
      )
   }
)

object TestTimelinePageLayoutIds : PageLayoutIds() {
   val clickedNote                  = PageLayoutInfo.LayoutId()
   val clickedNoteAccountIcon       = PageLayoutInfo.LayoutId()
   val clickedNoteAccountNameRow    = PageLayoutInfo.LayoutId()
   val clickedNoteAccountName       = PageLayoutInfo.LayoutId()
   val clickedNoteAccountScreenName = PageLayoutInfo.LayoutId()
   val clickedNoteContentText       = PageLayoutInfo.LayoutId()
}

@Composable
private fun TestTimeline(
   pageState: TestTimelinePageState,
   pageStackState: PageStackState
) {
   LazyColumn(
      state = pageState.lazyListState,
      modifier = Modifier.fillMaxSize()
   ) {
      items(pageState.notes) { i ->
         val isClickedNote = i == pageState.clickedNoteIndex

         Note(
            i,
            isClickedNote,
            modifier = Modifier
               .fillMaxWidth()
               .clickable(
                  onClick = {
                     pageState.clickedNoteIndex = i

                     val notePage = TestNotePage(i)
                     pageStackState.startPage(notePage)
                  }
               )
               .transitionElement(
                  TestTimelinePageLayoutIds.clickedNote,
                  enabled = isClickedNote
               )
         )
      }
   }
}

@Composable
private fun Note(
   i: Int,
   isClickedNote: Boolean,
   modifier: Modifier = Modifier
) {
   Row(modifier.padding(8.dp)) {
      Box(
         modifier = Modifier
            .padding(8.dp)
            .transitionElement(
               TestTimelinePageLayoutIds.clickedNoteAccountIcon,
               enabled = isClickedNote
            )
            .size(56.dp)
            .background(Color.Gray)
      ) {
         Text(
            "$i",
            color = Color.White,
            fontSize = 32.sp,
            modifier = Modifier.align(Alignment.Center)
         )
      }

      Column(Modifier.padding(horizontal = 8.dp)) {
         Row(
            Modifier
               .transitionElement(
                  TestTimelinePageLayoutIds.clickedNoteAccountNameRow,
                  enabled = isClickedNote
               )
               .fillMaxWidth()
         ) {
            Box(
               Modifier
                  .padding(vertical = 8.dp)
                  .weight(2.0f)
                  .height(16.dp)
                  .background(Color.Gray)
                  .transitionElement(
                     TestTimelinePageLayoutIds.clickedNoteAccountName,
                     enabled = isClickedNote
                  )
            )

            Box(
               Modifier
                  .padding(vertical = 8.dp)
                  .weight(1.0f)
                  .height(16.dp)
                  .background(Color.Gray)
                  .transitionElement(
                     TestTimelinePageLayoutIds.clickedNoteAccountScreenName,
                     enabled = isClickedNote
                  )
            )
         }

         Column(
            Modifier
               .transitionElement(
                  TestTimelinePageLayoutIds.clickedNoteContentText,
                  enabled = isClickedNote
               )
         ) {
            Box(
               Modifier
                  .padding(vertical = 4.dp)
                  .fillMaxWidth()
                  .height(12.dp)
                  .background(Color.Gray)
            )

            Box(
               Modifier
                  .padding(vertical = 4.dp)
                  .fillMaxWidth()
                  .height(12.dp)
                  .background(Color.Gray)
            )
         }
      }
   }
}
