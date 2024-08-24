/*
 * Copyright 2024 wcaokaze
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
import com.wcaokaze.probosqis.app.loadErrorListOrDefault
import com.wcaokaze.probosqis.app.loadPageDeckOrDefault
import com.wcaokaze.probosqis.error.AndroidPErrorListRepository
import com.wcaokaze.probosqis.error.PErrorListRepository
import com.wcaokaze.probosqis.error.PErrorListState
import com.wcaokaze.probosqis.error.errorSerializer
import com.wcaokaze.probosqis.mastodon.repository.AppRepository
import com.wcaokaze.probosqis.mastodon.repository.AppRepositoryImpl
import com.wcaokaze.probosqis.mastodon.ui.MastodonTestPage
import com.wcaokaze.probosqis.mastodon.ui.mastodonTestPageComposable
import com.wcaokaze.probosqis.page.PPageStateStore
import com.wcaokaze.probosqis.page.PPageSwitcherState
import com.wcaokaze.probosqis.pagedeck.AndroidPageDeckRepository
import com.wcaokaze.probosqis.pagedeck.AndroidPageStackRepository
import com.wcaokaze.probosqis.pagedeck.MultiColumnPageDeckState
import com.wcaokaze.probosqis.pagedeck.PageDeckRepository
import com.wcaokaze.probosqis.pagedeck.PageStackRepository
import com.wcaokaze.probosqis.pagedeck.SingleColumnPageDeckState
import com.wcaokaze.probosqis.pagedeck.pageSerializer
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

class MainApplication : Application() {
   init {
      System.loadLibrary("app")
   }

   private val allPageComposables = persistentListOf(
      testPageComposable,
      testTimelinePageComposable,
      testNotePageComposable,
      mastodonTestPageComposable,
   )

   private val allPageSerializers = persistentListOf(
      pageSerializer<TestPage>(),
      pageSerializer<TestTimelinePage>(),
      pageSerializer<TestNotePage>(),
      pageSerializer<MastodonTestPage>(),
   )

   private val allErrorItemComposables = persistentListOf(
      testErrorComposable,
   )

   private val allErrorSerializers = persistentListOf(
      errorSerializer<TestError>(),
   )

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

      single<AppRepository> {
         AppRepositoryImpl()
      }
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
