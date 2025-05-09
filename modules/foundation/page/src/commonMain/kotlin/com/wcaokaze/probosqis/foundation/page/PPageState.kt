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

package com.wcaokaze.probosqis.foundation.page

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wcaokaze.probosqis.app.pagedeck.PPageStackState
import com.wcaokaze.probosqis.capsiqum.page.PageStack
import com.wcaokaze.probosqis.capsiqum.page.PageState
import com.wcaokaze.probosqis.error.PError
import com.wcaokaze.probosqis.error.PErrorListState
import com.wcaokaze.probosqis.ext.kotlin.annotation.UsingAppScope
import kotlinx.coroutines.CoroutineScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private fun throwUninitializedException(): Nothing
      = throw IllegalStateException(
         "This PPageState has not been initialized. " +
         "Probably a constructor of some PPageState attempt to invoke " +
         "a PPageState API."
      )

@Stable
abstract class PPageState<out P : PPage> : PageState<P>(), KoinComponent {
   private val errorListState: PErrorListState by inject()
   private var pageStackStateRc = RC<PPageStackState>()

   private var impl: Interface = object : Interface {
      @UsingAppScope
      override val appScope: CoroutineScope by inject()

      override var pageStack: PageStack
         get() = pageStackStateRc.get().pageStack
         set(value) { pageStackStateRc.get().pageStack = value }

      override fun startPage(page: PPage) {
         pageStackStateRc.get().startPage(page)
      }

      override fun finishPage() {
         pageStackStateRc.get().finishPage()
      }

      override fun addColumn(page: PPage) {
         pageStackStateRc.get().addColumn(page)
      }

      override fun addColumn(pageStack: PageStack) {
         pageStackStateRc.get().addColumn(pageStack)
      }

      override fun removeFromDeck() {
         pageStackStateRc.get().removeFromDeck()
      }

      override fun raiseError(error: PError) {
         errorListState.raise(error, raiserPageId = pageId)
      }
   }

   @Composable
   internal fun inject(pageStackState: PPageStackState) {
      DisposableEffect(pageStackState) {
         pageStackStateRc.set(pageStackState)

         onDispose {
            pageStackStateRc.release()
         }
      }
   }

   @VisibleForTesting
   fun injectTestable(implementation: Interface) {
      impl = implementation
   }

   var pageStack: PageStack
      get() = impl.pageStack
      set(value) { impl.pageStack = value }

   fun startPage(page: PPage) {
      impl.startPage(page)
   }

   fun finishPage() {
      impl.finishPage()
   }

   fun addColumn(page: PPage) {
      impl.addColumn(page)
   }

   fun addColumn(pageStack: PageStack) {
      impl.addColumn(pageStack)
   }

   fun removeFromDeck() {
      impl.removeFromDeck()
   }

   fun raiseError(error: PError) {
      impl.raiseError(error)
   }

   interface Interface {
      @UsingAppScope
      val appScope: CoroutineScope

      var pageStack: PageStack

      fun startPage(page: PPage)
      fun finishPage()
      fun addColumn(page: PPage)
      fun addColumn(pageStack: PageStack)
      fun removeFromDeck()

      fun raiseError(error: PError)
   }

   @VisibleForTesting
   @Stable
   internal class RC<T> {
      private var referenceCount by mutableIntStateOf(0)
      private var ref: T? by mutableStateOf(null)

      fun get(): T {
         if (referenceCount <= 0) { throwUninitializedException() }

         @Suppress("UNCHECKED_CAST")
         return ref as T
      }

      fun set(value: T) {
         if (referenceCount <= 0) {
            referenceCount = 1
            ref = value
         } else {
            check(value == ref)
            referenceCount++
         }
      }

      fun release() {
         referenceCount--
      }
   }
}
