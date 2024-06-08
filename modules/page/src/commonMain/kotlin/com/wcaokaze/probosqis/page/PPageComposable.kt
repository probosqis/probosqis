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

package com.wcaokaze.probosqis.page

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import com.wcaokaze.probosqis.capsiqum.page.Page
import com.wcaokaze.probosqis.capsiqum.page.PageStateFactory
import com.wcaokaze.probosqis.pagedeck.PageTransitionSet
import kotlin.reflect.KClass

inline fun <reified P : Page, reified S : PPageState> PPageComposable(
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

data class PPageComposable<P : Page, S : PPageState>(
   val pageClass: KClass<P>,
   val pageStateClass: KClass<S>,
   val pageStateFactory: PageStateFactory<P, S>,
   val contentComposable: @Composable (P, S, WindowInsets) -> Unit,
   val headerComposable: @Composable (P, S) -> Unit,
   val headerActionsComposable: (@Composable RowScope.(P, S) -> Unit)?,
   val footerComposable: (@Composable (P, S) -> Unit)?,
   val pageTransitionSet: PageTransitionSet
)
