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

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.wcaokaze.probosqis.capsiqum.page.Page
import com.wcaokaze.probosqis.capsiqum.page.PageStateFactory
import com.wcaokaze.probosqis.capsiqum.transition.PageTransitionSpec
import com.wcaokaze.probosqis.capsiqum.transition.pageTransitionSpec
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import kotlin.reflect.KClass

inline fun <reified P : PPage, reified S : PPageState<P>> PPageComposable(
   pageStateFactory: PageStateFactory<P, S>,
   noinline content: @Composable (P, S, WindowInsets) -> Unit,
   noinline header: @Composable (P, S) -> Unit,
   // 本来nullableである必要はないがデフォルト引数でreified型引数のPとSを使えないため
   // headerActionsが空の状態をnullでも表せるようにする
   noinline headerActions: (@Composable RowScope.(P, S) -> Unit)? = null,
   noinline footer: (@Composable (P, S) -> Unit)?,
   pageTransitions: PageTransitionSet.Builder.() -> Unit
) = PPageComposable(
   P::class,
   S::class,
   pageStateFactory,
   content,
   header,
   headerActions,
   footer,
   pageTransitionSet = PageTransitionSet.Builder().apply(pageTransitions).build()
)

data class PPageComposable<P : PPage, S : PPageState<P>>(
   val pageClass: KClass<P>,
   val pageStateClass: KClass<S>,
   val pageStateFactory: PageStateFactory<P, S>,
   val contentComposable: @Composable (P, S, WindowInsets) -> Unit,
   val headerComposable: @Composable (P, S) -> Unit,
   val headerActionsComposable: (@Composable RowScope.(P, S) -> Unit)?,
   val footerComposable: (@Composable (P, S) -> Unit)?,
   val pageTransitionSet: PageTransitionSet
)

@Immutable
data class PageTransitionSet(
   val outgoingTransitions: ImmutableMap<KClass<out Page>, PageTransitionSpec>,
   val incomingTransitions: ImmutableMap<KClass<out Page>, PageTransitionSpec>
) {
   class Builder {
      private var outgoingTransitions = mutableMapOf<KClass<out Page>, PageTransitionSpec>()
      private var incomingTransitions = mutableMapOf<KClass<out Page>, PageTransitionSpec>()

      fun transitionTo(
         pageClass: KClass<out Page>,
         transitionSpec: PageTransitionSpec
      ) {
         outgoingTransitions[pageClass] = transitionSpec
      }

      inline fun <reified P : Page> transitionTo(
         enter: PageTransitionSpec.Builder.() -> Unit,
         exit:  PageTransitionSpec.Builder.() -> Unit
      ) {
         transitionTo(
            P::class,
            pageTransitionSpec(enter, exit)
         )
      }

      fun transitionFrom(
         pageClass: KClass<out Page>,
         transitionSpec: PageTransitionSpec
      ) {
         incomingTransitions[pageClass] = transitionSpec
      }

      inline fun <reified P : Page> transitionFrom(
         enter: PageTransitionSpec.Builder.() -> Unit,
         exit:  PageTransitionSpec.Builder.() -> Unit
      ) {
         transitionFrom(
            P::class,
            pageTransitionSpec(enter, exit)
         )
      }

      fun build() = PageTransitionSet(
         outgoingTransitions.toImmutableMap(),
         incomingTransitions.toImmutableMap()
      )
   }
}
