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

package com.wcaokaze.probosqis.cache.core

import androidx.compose.runtime.*

interface Cache<out T> {
   val value: T
}

@Suppress("FunctionName")
fun <T> Cache(initialValue: T): Cache<T>
      = CacheImpl(initialValue)

interface WritableCache<T> {
   var value: T

   fun asCache(): Cache<T>
}

@Suppress("FunctionName")
fun <T> WritableCache(initialValue: T): WritableCache<T>
      = CacheImpl(initialValue)

private class CacheImpl<T>(initialValue: T) : Cache<T>, WritableCache<T> {
   private val state = mutableStateOf(initialValue)

   override var value: T by state

   override fun asCache(): Cache<T> = this
}
