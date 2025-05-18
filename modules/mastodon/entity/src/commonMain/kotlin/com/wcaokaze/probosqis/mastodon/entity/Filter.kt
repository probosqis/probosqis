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

package com.wcaokaze.probosqis.mastodon.entity

import com.wcaokaze.probosqis.ext.kotlin.Url
import kotlinx.datetime.Instant

data class Filter(
   val id: Id,
   val title: String?,
   val context: List<Context>,
   val expireTime: Instant?,
   val filterAction: Action?,
   val keywords: List<Keyword>,
   val statuses: List<FilterStatus>,
) {
   constructor(
      rawId: String,
      title: String?,
      rawContext: List<String>,
      expireTimeEpochMillis: Long?,
      rawFilterAction: String?,
      keywords: List<Keyword>,
      statuses: List<FilterStatus>,
   ) : this(
      Id(rawId),
      title,
      rawContext.map(::Context),
      expireTimeEpochMillis?.let(Instant::fromEpochMilliseconds),
      rawFilterAction?.let(::Action),
      keywords,
      statuses,
   )

   val rawId: String
      get() = id.raw

   val rawContext: List<String>
      get() = context.map(Context::raw)

   val expireTimeEpochMillis: Long?
      get() = expireTime?.toEpochMilliseconds()

   val rawFilterAction: String?
      get() = filterAction?.raw

   @JvmInline
   value class Id(val raw: String)

   @JvmInline
   value class Context(val raw: String) {
      companion object {
         val HOME          = Context("home")
         val NOTIFICATIONS = Context("notifications")
         val PUBLIC        = Context("public")
         val THREAD        = Context("thread")
         val ACCOUNT       = Context("account")
      }
   }

   @JvmInline
   value class Action(val raw: String) {
      companion object {
         val WARN = Action("warn")
         val HIDE = Action("hide")
      }
   }

   data class Keyword(
      val id: Id,
      val keyword: String?,
      val wholeWord: Boolean?,
   ) {
      constructor(
         rawId: String,
         keyword: String?,
         wholeWord: Boolean?,
         @Suppress("UNUSED_PARAMETER")
         dummy: Unit?
      ) : this(
         Id(rawId),
         keyword,
         wholeWord,
      )

      val rawId: String
         get() = id.raw

      val dummy: Unit?
         get() = null

      @JvmInline
      value class Id(val raw: String)
   }

   data class FilterStatus(
      val id: Id,
      val statusId: Status.Id,
   ) {
      constructor(
         rawInstanceUrl: String,
         rawId: String,
         rawLocalStatusId: String,
      ) : this(
         Id(rawId),
         Status.Id(Url(rawInstanceUrl), Status.LocalId(rawLocalStatusId)),
      )

      val rawInstanceUrl: String
         get() = statusId.instanceUrl.raw

      val rawId: String
         get() = id.raw

      val rawLocalStatusId: String
         get() = statusId.local.raw

      @JvmInline
      value class Id(val raw: String)
   }
}

data class FilterResult(
   val filter: Filter?,
   val keywordMatches: List<String>,
   val statusMatches: List<Status.Id>,
)
