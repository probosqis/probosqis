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

import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.wcaokaze.probosqis.app.App
import com.wcaokaze.probosqis.app.MultiColumnProbosqis
import com.wcaokaze.probosqis.app.PErrorImpl
import com.wcaokaze.probosqis.app.ProbosqisState
import com.wcaokaze.probosqis.app.TestNotePage
import com.wcaokaze.probosqis.app.TestPage
import com.wcaokaze.probosqis.app.TestTimelinePage
import com.wcaokaze.probosqis.app.errorItemComposableImpl
import com.wcaokaze.probosqis.app.testNotePageComposable
import com.wcaokaze.probosqis.app.testPageComposable
import com.wcaokaze.probosqis.app.testTimelinePageComposable
import com.wcaokaze.probosqis.error.DesktopPErrorListRepository
import com.wcaokaze.probosqis.error.errorSerializer
import com.wcaokaze.probosqis.pagedeck.DesktopPageDeckRepository
import com.wcaokaze.probosqis.pagedeck.DesktopPageStackRepository
import com.wcaokaze.probosqis.pagedeck.pageSerializer
import com.wcaokaze.probosqis.resources.ProbosqisTheme
import com.wcaokaze.probosqis.resources.Strings
import kotlinx.collections.immutable.persistentListOf
import java.io.File

fun main() {
   application {
      ProbosqisTheme {
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

               val pageDeckRepository = DesktopPageDeckRepository(
                  pageStackRepository,
                  probosqisDataDir
               )

               val allErrorItemComposables = persistentListOf(
                  errorItemComposableImpl,
               )

               val errorListRepository = DesktopPErrorListRepository(
                  allErrorSerializers = listOf(
                     errorSerializer<PErrorImpl>(),
                  ),
                  probosqisDataDir
               )

               ProbosqisState(
                  allPageComposables, pageDeckRepository, pageStackRepository,
                  allErrorItemComposables, errorListRepository,
                  coroutineScope
               )
            }

            MultiColumnProbosqis(probosqisState)
         }
      }
   }
}
