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
import com.wcaokaze.probosqis.app.loadPageDeckOrDefault
import com.wcaokaze.probosqis.app.testNotePageComposable
import com.wcaokaze.probosqis.app.testPageComposable
import com.wcaokaze.probosqis.app.testTimelinePageComposable
import com.wcaokaze.probosqis.capsiqum.page.PageStateStore
import com.wcaokaze.probosqis.error.DesktopPErrorListRepository
import com.wcaokaze.probosqis.error.errorSerializer
import com.wcaokaze.probosqis.pagedeck.CombinedPageSwitcherState
import com.wcaokaze.probosqis.pagedeck.DesktopPageDeckRepository
import com.wcaokaze.probosqis.pagedeck.DesktopPageStackRepository
import com.wcaokaze.probosqis.pagedeck.MultiColumnPageDeckState
import com.wcaokaze.probosqis.pagedeck.PageDeckRepository
import com.wcaokaze.probosqis.pagedeck.PageStackRepository
import com.wcaokaze.probosqis.pagedeck.SingleColumnPageDeckState
import com.wcaokaze.probosqis.pagedeck.pageSerializer
import com.wcaokaze.probosqis.resources.ProbosqisTheme
import com.wcaokaze.probosqis.resources.Strings
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.module
import java.io.File

private val allPageComposables = persistentListOf(
   testPageComposable,
   testTimelinePageComposable,
   testNotePageComposable,
)

private val allPageSerializers = persistentListOf(
   pageSerializer<TestPage>(),
   pageSerializer<TestTimelinePage>(),
   pageSerializer<TestNotePage>(),
)

private val probosqisDataDir = File(System.getProperty("user.home"), ".probosqisData")

private val koinModule = module {
   single { CombinedPageSwitcherState(allPageComposables) }

   single {
      PageStateStore(
         allPageComposables.map { it.pageStateFactory },
         appCoroutineScope = get()
      )
   }

   single<PageDeckRepository> {
      DesktopPageDeckRepository(pageStackRepository = get(), probosqisDataDir)
   }

   single<PageStackRepository> {
      DesktopPageStackRepository(allPageSerializers, probosqisDataDir)
   }

   factory {
      val pageDeckCache = loadPageDeckOrDefault(
         pageDeckRepository = get(),
         pageStackRepository = get()
      )

      MultiColumnPageDeckState(pageDeckCache, pageStackRepository = get())
   }

   factory {
      val pageDeckCache = loadPageDeckOrDefault(
         pageDeckRepository = get(),
         pageStackRepository = get()
      )

      SingleColumnPageDeckState(pageDeckCache, pageStackRepository = get())
   }
}

fun main() {
   application {
      val appCoroutineScope = rememberCoroutineScope()

      KoinApplication(
         application = {
            val appKoinModule = module {
               single { appCoroutineScope }
            }

            modules(koinModule, appKoinModule)
         }
      ) {
         ProbosqisTheme {
            Window(
               title = Strings.App.topAppBar,
               onCloseRequest = { exitApplication() }
            ) {
               val pageSwitcherState: CombinedPageSwitcherState = koinInject()
               val pageStateStore: PageStateStore = koinInject()

               val probosqisState = remember(pageSwitcherState, pageStateStore) {

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
                     allErrorItemComposables, errorListRepository
                  )
               }

               MultiColumnProbosqis(probosqisState)
            }
         }
      }
   }
}
