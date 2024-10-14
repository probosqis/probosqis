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

package com.wcaokaze.probosqis.pagedeck

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import com.wcaokaze.probosqis.capsiqum.page.Page
import com.wcaokaze.probosqis.capsiqum.page.PageState
import com.wcaokaze.probosqis.capsiqum.page.PageStateFactory
import com.wcaokaze.probosqis.capsiqum.transition.PageTransitionSpec
import kotlinx.collections.immutable.ImmutableMap
import kotlin.reflect.KClass

inline fun <reified P : Page, reified S : PageState> CombinedPageComposable(
   pageStateFactory: PageStateFactory<P, S>,
   noinline content: @Composable (P, S, PPageStackState, WindowInsets) -> Unit,
   noinline header: @Composable (P, S, PPageStackState) -> Unit,
   // 本来nullableである必要はないがデフォルト引数でreified型引数のPとSを使えないため
   // headerActionsが空の状態をnullでも表せるようにする
   noinline headerActions: (@Composable RowScope.(P, S, PPageStackState) -> Unit)? = null,
   noinline footer: (@Composable (P, S, PPageStackState) -> Unit)?,
   outgoingTransitions: ImmutableMap<KClass<out Page>, PageTransitionSpec>,
   incomingTransitions: ImmutableMap<KClass<out Page>, PageTransitionSpec>
) = CombinedPageComposable(
   P::class,
   S::class,
   pageStateFactory,
   content,
   header,
   headerActions,
   footer,
   outgoingTransitions,
   incomingTransitions
)

@Stable
data class CombinedPageComposable<P : Page, S : PageState>(
   val pageClass: KClass<P>,
   val pageStateClass: KClass<S>,
   val pageStateFactory: PageStateFactory<P, S>,
   val contentComposable: @Composable (P, S, PPageStackState, WindowInsets) -> Unit,
   val headerComposable: @Composable (P, S, PPageStackState) -> Unit,
   val headerActionsComposable: (@Composable RowScope.(P, S, PPageStackState) -> Unit)?,
   val footerComposable: (@Composable (P, S, PPageStackState) -> Unit)?,
   val outgoingTransitions: ImmutableMap<KClass<out Page>, PageTransitionSpec>,
   val incomingTransitions: ImmutableMap<KClass<out Page>, PageTransitionSpec>
)
