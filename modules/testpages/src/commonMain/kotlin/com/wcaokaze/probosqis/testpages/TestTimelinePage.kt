/*
 * Copyright 2023-2024 wcaokaze
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

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wcaokaze.probosqis.capsiqum.page.PageStateFactory
import com.wcaokaze.probosqis.capsiqum.transition.PageLayoutInfo
import com.wcaokaze.probosqis.capsiqum.transition.SharedElementAnimations
import com.wcaokaze.probosqis.capsiqum.transition.SharedElementAnimatorElement
import com.wcaokaze.probosqis.capsiqum.transition.animatePosition
import com.wcaokaze.probosqis.capsiqum.transition.animateScale
import com.wcaokaze.probosqis.capsiqum.transition.sharedElement
import com.wcaokaze.probosqis.capsiqum.transition.transitionElement
import com.wcaokaze.probosqis.page.FooterButton
import com.wcaokaze.probosqis.page.PPage
import com.wcaokaze.probosqis.page.PPageComposable
import com.wcaokaze.probosqis.page.PPageState
import com.wcaokaze.probosqis.page.PageLayoutIds
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlin.math.pow

@Serializable
@SerialName("com.wcaokaze.probosqis.testpages.TestTimelinePage")
class TestTimelinePage : PPage()

@Stable
class TestTimelinePageState : PPageState<TestTimelinePage>() {
   val lazyListState by save(
      "lazyListState", LazyListState.Saver
   ) { LazyListState() }

   var notes: List<Int> by save(
      "notes", ListSerializer(Int.serializer())
   ) { (0..200).toList() }

   var clickedNoteIndex: Int? by save(
      "clickedNoteIndex", Int.serializer().nullable
   ) { null }
}

val testTimelinePageComposable = PPageComposable<TestTimelinePage, TestTimelinePageState>(
   PageStateFactory { _, _ -> TestTimelinePageState() },
   content = { _, pageState, windowInsets ->
      TestTimeline(pageState, windowInsets)
   },
   header = { _, _ ->
      Text(
         "HomeTimeline",
         maxLines = 1,
         overflow = TextOverflow.Ellipsis
      )
   },
   headerActions = { _, _ ->
      IconButton(
         onClick = {}
      ) {
         Icon(Icons.Default.AccountBox, contentDescription = "Account")
      }
   },
   footer = { _, _ ->
      Row {
         FooterButton(
            onClick = {},
            modifier = Modifier.weight(1.0f)
         ) {
            Icon(Icons.Default.Send, contentDescription = "Send")
         }

         Spacer(Modifier.weight(1.0f))

         FooterButton(
            onClick = {},
            modifier = Modifier.weight(1.0f)
         ) {
            Icon(Icons.Default.Share, contentDescription = "Share")
         }

         FooterButton(
            onClick = {},
            modifier = Modifier.weight(1.0f)
         ) {
            Icon(Icons.Default.Favorite, contentDescription = "Favorite")
         }
      }
   },
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
                  TestTimelinePageLayoutIds.clickedNote, targetIds.background, label = "background-Offset")
               val s by animateScale(
                  TestTimelinePageLayoutIds.clickedNote, targetIds.background, label = "background-Scale")

               Modifier.graphicsLayer {
                  transformOrigin = TransformOrigin(0.0f, 0.0f)
                  alpha = a
                  translationY = p.y
                  scaleY = s.scaleY
               }
            }

            sharedElement(
               TestTimelinePageLayoutIds.clickedNoteAccountIcon,
               TestNotePageLayoutIds.accountIcon,
               label = "accountIcon",
               SharedElementAnimatorElement.Target,
               SharedElementAnimations.Offset
            )

            sharedElement(
               TestTimelinePageLayoutIds.clickedNoteAccountNameRow,
               TestNotePageLayoutIds.accountNameColumn,
               label = "accountName"
            )

            sharedElement(
               TestTimelinePageLayoutIds.clickedNoteContentText,
               TestNotePageLayoutIds.contentText,
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
                  currentIds.background, TestTimelinePageLayoutIds.clickedNote, label = "background-Offset")
               val s by animateScale(
                  currentIds.background, TestTimelinePageLayoutIds.clickedNote, label = "background-Scale")

               Modifier.graphicsLayer {
                  transformOrigin = TransformOrigin(0.0f, 0.0f)
                  alpha = a
                  translationY = p.y
                  scaleY = s.scaleY
               }
            }

            sharedElement(
               TestNotePageLayoutIds.accountIcon,
               TestTimelinePageLayoutIds.clickedNoteAccountIcon,
               label = "accountIcon",
               SharedElementAnimatorElement.Current,
               SharedElementAnimations.Offset
            )

            sharedElement(
               TestNotePageLayoutIds.accountNameColumn,
               TestTimelinePageLayoutIds.clickedNoteAccountNameRow,
               label = "accountName"
            )

            sharedElement(
               TestNotePageLayoutIds.contentText,
               TestTimelinePageLayoutIds.clickedNoteContentText,
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
   windowInsets: WindowInsets
) {
   LazyColumn(
      state = pageState.lazyListState,
      contentPadding = windowInsets.asPaddingValues(),
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
                     pageState.startPage(notePage)
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
