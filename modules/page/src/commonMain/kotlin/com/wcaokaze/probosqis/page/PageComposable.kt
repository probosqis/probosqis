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
import kotlin.reflect.KClass

data class PageComposable<P : Page>(
   val pageClass: KClass<P>,
   val contentComposable: @Composable (P, PageStackState) -> Unit,
   val headerComposable: @Composable (P, PageStackState) -> Unit,
   val footerComposable: (@Composable (P, PageStackState) -> Unit)?
)

inline fun <reified P : Page> pageComposable(
   noinline content: @Composable (P, PageStackState) -> Unit,
   noinline header: @Composable (P, PageStackState) -> Unit,
   noinline footer: (@Composable (P, PageStackState) -> Unit)?
) = PageComposable(
   P::class,
   content,
   header,
   footer
)
