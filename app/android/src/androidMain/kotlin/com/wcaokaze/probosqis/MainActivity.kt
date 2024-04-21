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

package com.wcaokaze.probosqis

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.wcaokaze.probosqis.app.Probosqis
import com.wcaokaze.probosqis.app.ProbosqisState
import com.wcaokaze.probosqis.app.TestNotePage
import com.wcaokaze.probosqis.app.TestPage
import com.wcaokaze.probosqis.app.TestTimelinePage
import com.wcaokaze.probosqis.app.pagedeck.AndroidPageDeckRepository
import com.wcaokaze.probosqis.app.pagedeck.AndroidPageStackRepository
import com.wcaokaze.probosqis.app.pagedeck.pageSerializer
import com.wcaokaze.probosqis.app.testNotePageComposable
import com.wcaokaze.probosqis.app.testPageComposable
import com.wcaokaze.probosqis.app.testTimelinePageComposable
import com.wcaokaze.probosqis.resources.ProbosqisTheme
import kotlinx.collections.immutable.persistentListOf

class MainActivity : ComponentActivity() {
   override fun onCreate(savedInstanceState: Bundle?) {
      enableEdgeToEdge(colorScheme = null)
      super.onCreate(savedInstanceState)

      setContent {
         ProbosqisTheme {
            val context = LocalContext.current

            val coroutineScope = rememberCoroutineScope()

            val probosqisState = remember(context) {
               val allPageComposables = persistentListOf(
                  testPageComposable,
                  testTimelinePageComposable,
                  testNotePageComposable,
               )
               val pageStackRepository = AndroidPageStackRepository(
                  context,
                  allPageSerializers = listOf(
                     pageSerializer<TestPage>(),
                     pageSerializer<TestTimelinePage>(),
                     pageSerializer<TestNotePage>(),
                  )
               )
               val pageDeckRepository = AndroidPageDeckRepository(
                  context, pageStackRepository
               )

               ProbosqisState(allPageComposables, pageDeckRepository,
                  pageStackRepository, coroutineScope)
            }

            BackHandler {
               probosqisState.pageDeckState.activePageStackState.finishPage()
            }

            val colorScheme = MaterialTheme.colorScheme

            LaunchedEffect(colorScheme) {
               enableEdgeToEdge(colorScheme)
            }

            Probosqis(probosqisState)
         }
      }
   }

   private fun enableEdgeToEdge(colorScheme: ColorScheme?) {
      val isDarkTheme = (
            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
         ) == Configuration.UI_MODE_NIGHT_YES

      var lightNavigationBarScrim = Color.White
      var darkNavigationBarScrim = Color(0x1B, 0x1B, 0x1B)

      if (colorScheme == null) {
         lightNavigationBarScrim = lightNavigationBarScrim.copy(alpha = 0.9f)
         darkNavigationBarScrim = darkNavigationBarScrim.copy(alpha = 0.5f)
      } else {
         lightNavigationBarScrim = lightNavigationBarScrim.copy(alpha = 0.9f)
            .compositeOver(colorScheme.primaryContainer)
            .copy(alpha = 0.9f)

         darkNavigationBarScrim = darkNavigationBarScrim.copy(alpha = 0.9f)
            .compositeOver(colorScheme.primaryContainer)
            .copy(alpha = 0.8f)
      }

      enableEdgeToEdge(
         navigationBarStyle = if (isDarkTheme) {
            SystemBarStyle.dark(darkNavigationBarScrim.toArgb())
         } else {
            SystemBarStyle.light(
               lightNavigationBarScrim.toArgb(), darkNavigationBarScrim.toArgb())
         }
      )
   }
}
