/*
 * Copyright 2025 wcaokaze
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

package com.wcaokaze.probosqis.ext.panoptiqon

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import com.wcaokaze.probosqis.panoptiqon.Cache
import com.wcaokaze.probosqis.panoptiqon.CacheId
import com.wcaokaze.probosqis.panoptiqon.InternalCacheApi
import com.wcaokaze.probosqis.panoptiqon.WritableCache

inline fun <T, R> Cache<T>.map(crossinline op: (T) -> R): Cache<R> {
   return object : MappedCache<T, R>(this) {
      override fun map(value: T): R = op(value)
   }
}

/**
 * `Cache<T>` の[中身][Cache.value]に変換関数を適用し、 `Cache<R>` として
 * 振る舞うインスタンス。
 *
 * `Cache<R>` のサブタイプであるものの[CacheSerializer]でのシリアライズが
 * 不可能となる点に注意
 */
@Stable
abstract class MappedCache<in T, out R>(
   private val origin: Cache<T>
) : Cache<R> {
   override val id: CacheId
      get() = origin.id

   override val value: R
      get() {
         @OptIn(InternalCacheApi::class)
         return state.value
      }

   protected abstract fun map(value: T): R

   @InternalCacheApi
   override val state = object : State<R> {
      override val value: R
         get() = map(origin.value)
   }
}

@Stable
abstract class MappedWritableCache<T, R>(
   private val origin: WritableCache<T>
) : WritableCache<R> {
   override val id: CacheId
      get() = origin.id

   override var value: R
      get() {
         @OptIn(InternalCacheApi::class)
         return mutableState.value
      }
      set(value) {
         @OptIn(InternalCacheApi::class)
         mutableState.value = value
      }

   protected abstract fun map(value: T): R
   protected abstract fun reverseMap(value: R): T

   override fun asCache(): Cache<R> = object : MappedCache<T, R>(origin.asCache()) {
      override fun map(value: T): R = this@MappedWritableCache.map(value)
   }

   @InternalCacheApi
   override val mutableState = object : MutableState<R> {
      override var value: R
         get() = map(origin.value)
         set(value) {
            origin.value = reverseMap(value)
         }

      override fun component1() = value
      override fun component2(): (R) -> Unit {
         return { value = it }
      }
   }
}
