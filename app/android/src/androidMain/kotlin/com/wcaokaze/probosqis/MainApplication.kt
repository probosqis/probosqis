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

package com.wcaokaze.probosqis

import android.app.Application
import com.wcaokaze.probosqis.app.core.loadErrorListOrDefault
import com.wcaokaze.probosqis.app.core.loadPageDeckOrDefault
import com.wcaokaze.probosqis.app.pagedeck.AndroidPageDeckRepository
import com.wcaokaze.probosqis.app.pagedeck.AndroidPageStackRepository
import com.wcaokaze.probosqis.app.pagedeck.MultiColumnPageDeckState
import com.wcaokaze.probosqis.app.pagedeck.PageDeckRepository
import com.wcaokaze.probosqis.app.pagedeck.PageStackRepository
import com.wcaokaze.probosqis.app.pagedeck.SingleColumnPageDeckState
import com.wcaokaze.probosqis.app.pagedeck.pageSerializer
import com.wcaokaze.probosqis.foundation.credential.AndroidCredentialRepository
import com.wcaokaze.probosqis.foundation.credential.CredentialRepository
import com.wcaokaze.probosqis.foundation.credential.credentialSerializer
import com.wcaokaze.probosqis.foundation.error.AndroidPErrorListRepository
import com.wcaokaze.probosqis.foundation.error.PErrorListRepository
import com.wcaokaze.probosqis.foundation.error.PErrorListState
import com.wcaokaze.probosqis.foundation.error.errorSerializer
import com.wcaokaze.probosqis.foundation.page.PPageSwitcherState
import com.wcaokaze.probosqis.mastodon.repository.AccountRepository
import com.wcaokaze.probosqis.mastodon.repository.AndroidAccountRepository
import com.wcaokaze.probosqis.mastodon.repository.AndroidAppRepository
import com.wcaokaze.probosqis.mastodon.repository.AndroidTimelineRepository
import com.wcaokaze.probosqis.mastodon.repository.AppRepository
import com.wcaokaze.probosqis.mastodon.repository.TimelineRepository
import com.wcaokaze.probosqis.nodeinfo.repository.AndroidNodeInfoRepository
import com.wcaokaze.probosqis.nodeinfo.repository.NodeInfoRepository
import com.wcaokaze.probosqis.testpages.TestError
import com.wcaokaze.probosqis.testpages.TestNotePage
import com.wcaokaze.probosqis.testpages.TestPage
import com.wcaokaze.probosqis.testpages.TestTimelinePage
import com.wcaokaze.probosqis.testpages.testErrorComposable
import com.wcaokaze.probosqis.testpages.testNotePageComposable
import com.wcaokaze.probosqis.testpages.testPageComposable
import com.wcaokaze.probosqis.testpages.testTimelinePageComposable
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.MainScope
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.net.URLEncoder

class MainApplication : Application() {
   init {
      System.loadLibrary("app_core")
   }

   private val allPageComposables = persistentListOf(
      testPageComposable,
      testTimelinePageComposable,
      testNotePageComposable,
      com.wcaokaze.probosqis.app.setting.account.list.accountListPageComposable,
      com.wcaokaze.probosqis.mastodon.ui.auth.callbackwaiter.callbackWaiterPageComposable,
      com.wcaokaze.probosqis.mastodon.ui.auth.urlinput.urlInputPageComposable,
      com.wcaokaze.probosqis.mastodon.ui.timeline.home.homeTimelinePageComposable,
   )

   private val allPageSerializers = persistentListOf(
      pageSerializer<TestPage>(),
      pageSerializer<TestTimelinePage>(),
      pageSerializer<TestNotePage>(),
      pageSerializer<com.wcaokaze.probosqis.app.setting.account.list.AccountListPage>(),
      pageSerializer<com.wcaokaze.probosqis.mastodon.ui.auth.callbackwaiter.CallbackWaiterPage>(),
      pageSerializer<com.wcaokaze.probosqis.mastodon.ui.auth.urlinput.UrlInputPage>(),
      pageSerializer<com.wcaokaze.probosqis.mastodon.ui.timeline.home.HomeTimelinePage>(),
   )

   private val allErrorItemComposables = persistentListOf(
      testErrorComposable,
   )

   private val allErrorSerializers = persistentListOf(
      errorSerializer<TestError>(),
   )

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
         AndroidPageDeckRepository(context = get(), pageStackRepository = get())
      }

      single<PageStackRepository> {
         AndroidPageStackRepository(context = get(), allPageSerializers)
      }

      single<PErrorListRepository> {
         AndroidPErrorListRepository(
            context = get(),
            allErrorSerializers,
            allPageSerializers
         )
      }

      single<CredentialRepository> {
         AndroidCredentialRepository(
            context = get(),
            allCredentialSerializers = listOf(
               credentialSerializer<com.wcaokaze.probosqis.mastodon.entity.Token> { token ->
                  val encodedUrl = URLEncoder.encode(token.accountId.instanceUrl.raw, "UTF-8")
                  val localId = token.accountId.local.value
                  "mastodon_${encodedUrl}_$localId"
               },
            ),
         )
      }

      single<AppRepository> { AndroidAppRepository(context = get()) }
      single<AccountRepository> { AndroidAccountRepository() }
      single<NodeInfoRepository> { AndroidNodeInfoRepository() }
      single<TimelineRepository> { AndroidTimelineRepository() }
   }

   private val appKoinModule = module {
      single { MainScope() }
   }

   override fun onCreate() {
      super.onCreate()

      startKoin {
         androidContext(this@MainApplication)
         modules(koinModule, repositoriesKoinModule, appKoinModule)
      }
   }
}
