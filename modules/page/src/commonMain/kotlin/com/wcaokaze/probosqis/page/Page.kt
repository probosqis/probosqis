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

package com.wcaokaze.probosqis.page

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import com.wcaokaze.probosqis.cache.core.WritableCache
import com.wcaokaze.probosqis.cache.core.update
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

@Serializable
abstract class Page

@Stable
abstract class PageState {
   @Stable
   class StateSaver(private val cache: WritableCache<JsonObject>) {
      fun <T> save(
         key: String,
         serializer: KSerializer<T>,
         init: () -> T
      ): MutableState<T> {
         return ElementState(cache, key, serializer, init)
      }

      private class ElementState<T>(
         private val source: WritableCache<JsonObject>,
         private val key: String,
         private val serializer: KSerializer<T>,
         private val init: () -> T
      ) : MutableState<T> {
         init {
            if (!source.value.containsKey(key)) {
               initialize()
            }
         }

         override var value: T
            get() {
               val element = source.value[key]
               return if (element != null) {
                  try {
                     Json.decodeFromJsonElement(serializer, element)
                  } catch (_: Exception) {
                     initialize()
                  }
               } else {
                  initialize()
               }
            }
            set(value) {
               updateSource(value)
            }

         override fun component1(): T = value
         override fun component2(): (T) -> Unit = { value = it }

         private fun updateSource(value: T) {
            val jsonValue = Json.encodeToJsonElement(serializer, value)
            source.update { JsonObject(it + (key to jsonValue)) }
         }

         private fun initialize(): T {
            val initValue = init()
            updateSource(initValue)
            return initValue
         }
      }
   }
}

@Composable
internal fun PageContent(
   savedPageState: PageStack.SavedPageState,
   pageComposableSwitcher: PageComposableSwitcher,
   pageStateStore: PageStateStore,
   pageStackState: PageStackState
) {
   val page = savedPageState.page
   val pageContentComposable = pageComposableSwitcher[page]
   val pageState = pageStateStore.get(savedPageState)

   if (pageContentComposable == null) {
      TODO()
   } else {
      PageContent(
         pageContentComposable.contentComposable,
         page,
         pageState,
         pageStackState
      )
   }
}

@Composable
inline fun <P : Page, S : PageState> PageContent(
   pageContentComposable: @Composable (P, S, PageStackState) -> Unit,
   page: P,
   pageState: PageState,
   pageStackState: PageStackState
) {
   @Suppress("UNCHECKED_CAST")
   pageContentComposable(
      page,
      pageState as S,
      pageStackState
   )
}
