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

package com.wcaokaze.probosqis

import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.wcaokaze.probosqis.app.App
import com.wcaokaze.probosqis.app.Probosqis
import com.wcaokaze.probosqis.app.ProbosqisState
import com.wcaokaze.probosqis.app.TestNotePage
import com.wcaokaze.probosqis.app.TestPage
import com.wcaokaze.probosqis.app.TestTimelinePage
import com.wcaokaze.probosqis.app.testNotePageComposable
import com.wcaokaze.probosqis.app.testPageComposable
import com.wcaokaze.probosqis.app.testTimelinePageComposable
import com.wcaokaze.probosqis.capsiqum.DesktopPageStackBoardRepository
import com.wcaokaze.probosqis.capsiqum.DesktopPageStackRepository
import com.wcaokaze.probosqis.capsiqum.pageSerializer
import com.wcaokaze.probosqis.resources.Strings
import kotlinx.collections.immutable.persistentListOf
import java.io.File

fun main() {
   application {
      Window(
         title = Strings.App.topAppBar,
         onCloseRequest = { exitApplication() }
      ) {
         val coroutineScope = rememberCoroutineScope()

         val probosqisState = remember {
            val probosqisDataDir
                  = File(System.getProperty("user.home"), ".probosqisData")

            val allPageComposables = persistentListOf(
               testPageComposable,
               testTimelinePageComposable,
               testNotePageComposable,
            )

            val pageStackRepository = DesktopPageStackRepository(
               allPageSerializers = listOf(
                  pageSerializer<TestPage>(),
                  pageSerializer<TestTimelinePage>(),
                  pageSerializer<TestNotePage>(),
               ),
               probosqisDataDir
            )

            val pageStackBoardRepository = DesktopPageStackBoardRepository(
               pageStackRepository,
               probosqisDataDir
            )

            ProbosqisState(allPageComposables, pageStackBoardRepository,
               pageStackRepository, coroutineScope)
         }

         Probosqis(probosqisState)
      }
   }
}
