/*
 * Copyright 2023-2025 wcaokaze
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
import com.wcaokaze.probosqis.app.core.App
import com.wcaokaze.probosqis.app.core.MultiColumnProbosqis
import com.wcaokaze.probosqis.app.core.ProbosqisState
import com.wcaokaze.probosqis.app.core.loadErrorListOrDefault
import com.wcaokaze.probosqis.app.core.loadPageDeckOrDefault
import com.wcaokaze.probosqis.credential.CredentialRepository
import com.wcaokaze.probosqis.credential.DesktopCredentialRepository
import com.wcaokaze.probosqis.credential.credentialSerializer
import com.wcaokaze.probosqis.error.DesktopPErrorListRepository
import com.wcaokaze.probosqis.error.PErrorListRepository
import com.wcaokaze.probosqis.error.PErrorListState
import com.wcaokaze.probosqis.error.errorSerializer
import com.wcaokaze.probosqis.mastodon.repository.AccountRepository
import com.wcaokaze.probosqis.mastodon.repository.AppRepository
import com.wcaokaze.probosqis.mastodon.repository.DesktopAccountRepository
import com.wcaokaze.probosqis.mastodon.repository.DesktopAppRepository
import com.wcaokaze.probosqis.mastodon.repository.DesktopTimelineRepository
import com.wcaokaze.probosqis.mastodon.repository.TimelineRepository
import com.wcaokaze.probosqis.nodeinfo.repository.DesktopNodeInfoRepository
import com.wcaokaze.probosqis.nodeinfo.repository.NodeInfoRepository
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
import java.net.URLEncoder

object Main {
   init {
      loadNativeLib()
   }

   private val allPageComposables = persistentListOf(
      testPageComposable,
      testTimelinePageComposable,
      testNotePageComposable,
      com.wcaokaze.probosqis.mastodon.ui.auth.callbackwaiter.callbackWaiterPageComposable,
      com.wcaokaze.probosqis.mastodon.ui.auth.urlinput.urlInputPageComposable,
      com.wcaokaze.probosqis.mastodon.ui.timeline.home.homeTimelinePageComposable,
   )

   private val allPageSerializers = persistentListOf(
      pageSerializer<TestPage>(),
      pageSerializer<TestTimelinePage>(),
      pageSerializer<TestNotePage>(),
      pageSerializer<com.wcaokaze.probosqis.mastodon.ui.auth.callbackwaiter.CallbackWaiterPage>(),
      pageSerializer<com.wcaokaze.probosqis.mastodon.ui.auth.urlinput.UrlInputPage>(),
      pageSerializer<com.wcaokaze.probosqis.mastodon.ui.timeline.home.HomeTimelinePage>(),
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

      single<CredentialRepository> {
         DesktopCredentialRepository(
            allCredentialSerializers = listOf(
               credentialSerializer<com.wcaokaze.probosqis.mastodon.entity.Token> { token ->
                  val encodedUrl = URLEncoder.encode(token.accountId.instanceUrl.raw, "UTF-8")
                  val localId = token.accountId.local.value
                  "mastodon_${encodedUrl}_$localId"
               },
            ),
            probosqisDataDir
         )
      }

      single<AppRepository> { DesktopAppRepository(probosqisDataDir) }
      single<AccountRepository> { DesktopAccountRepository() }
      single<NodeInfoRepository> { DesktopNodeInfoRepository() }
      single<TimelineRepository> { DesktopTimelineRepository() }
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
                  MultiColumnProbosqis(
                     probosqisState,
                     onRequestCloseWindow = { exitApplication() }
                  )
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
               "target/debug/libapp_core.so"
            )
            System.load(lib.absolutePath)
         }
         else -> throw IllegalStateException()
      }
   }
}
