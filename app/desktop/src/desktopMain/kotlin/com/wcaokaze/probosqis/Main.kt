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
import com.wcaokaze.probosqis.app.ProbosqisState
import com.wcaokaze.probosqis.app.loadErrorListOrDefault
import com.wcaokaze.probosqis.app.loadPageDeckOrDefault
import com.wcaokaze.probosqis.error.DesktopPErrorListRepository
import com.wcaokaze.probosqis.error.PErrorListRepository
import com.wcaokaze.probosqis.error.PErrorListState
import com.wcaokaze.probosqis.error.errorSerializer
import com.wcaokaze.probosqis.page.PPageStateStore
import com.wcaokaze.probosqis.page.PPageSwitcherState
import com.wcaokaze.probosqis.pagedeck.DesktopPageDeckRepository
import com.wcaokaze.probosqis.pagedeck.DesktopPageStackRepository
import com.wcaokaze.probosqis.pagedeck.MultiColumnPageDeckState
import com.wcaokaze.probosqis.pagedeck.PageDeckRepository
import com.wcaokaze.probosqis.pagedeck.PageStackRepository
import com.wcaokaze.probosqis.pagedeck.SingleColumnPageDeckState
import com.wcaokaze.probosqis.pagedeck.pageSerializer
import com.wcaokaze.probosqis.resources.ProbosqisTheme
import com.wcaokaze.probosqis.resources.Strings
import com.wcaokaze.probosqis.testpages.TestError
import com.wcaokaze.probosqis.testpages.TestNotePage
import com.wcaokaze.probosqis.testpages.TestPage
import com.wcaokaze.probosqis.testpages.TestTimelinePage
import com.wcaokaze.probosqis.testpages.testErrorComposable
import com.wcaokaze.probosqis.testpages.testNotePageComposable
import com.wcaokaze.probosqis.testpages.testPageComposable
import com.wcaokaze.probosqis.testpages.testTimelinePageComposable
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.KoinApplication
import org.koin.dsl.module
import java.io.File

object Main {
   init {
      loadNativeLib()
   }

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

   private val allErrorItemComposables = persistentListOf(
      testErrorComposable,
   )

   private val allErrorSerializers = listOf(
      errorSerializer<TestError>(),
   )

   private val probosqisDataDir = File(System.getProperty("user.home"), ".probosqisData")

   private val koinModule = module {
      single { PPageSwitcherState(allPageComposables) }

      single {
         PPageStateStore(
            allPageComposables,
            appCoroutineScope = get()
         )
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

      single {
         PErrorListState(
            loadErrorListOrDefault(errorListRepository = get()),
            allErrorItemComposables
         )
      }
   }

   private val repositoriesKoinModule = module {
      single<PageDeckRepository> {
         DesktopPageDeckRepository(pageStackRepository = get(), probosqisDataDir)
      }

      single<PageStackRepository> {
         DesktopPageStackRepository(allPageSerializers, probosqisDataDir)
      }

      single<PErrorListRepository> {
         DesktopPErrorListRepository(
            allErrorSerializers,
            allPageSerializers,
            probosqisDataDir
         )
      }
   }

   @JvmStatic
   fun main(vararg args: String) {
      application {
         val appCoroutineScope = rememberCoroutineScope()

         KoinApplication(
            application = {
               val appKoinModule = module {
                  single { appCoroutineScope }
               }

               modules(koinModule, repositoriesKoinModule, appKoinModule)
            }
         ) {
            ProbosqisTheme {
               Window(
                  title = Strings.App.topAppBar,
                  onCloseRequest = { exitApplication() }
               ) {
                  val probosqisState = remember { ProbosqisState() }
                  MultiColumnProbosqis(probosqisState)
               }
            }
         }
      }
   }

   private fun loadNativeLib() {
      val osName = System.getProperty("os.name").lowercase()
      when {
         osName.startsWith("linux") -> {
            val lib = File(
               System.getProperty("user.dir").split('/').dropLast(2).joinToString("/"),
               "target/debug/libapp.so"
            )
            System.load(lib.absolutePath)
         }
         else -> throw IllegalStateException()
      }
   }
}
