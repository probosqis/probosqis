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

use chrono::{DateTime, Utc};
use serde::Deserialize;
use crate::status::StatusId;

#[cfg(feature = "jvm")]
use {
   ext_panoptiqon::convert_jvm_helper,
   jni::JNIEnv,
   panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm},
   panoptiqon::jvm_types::{
      JvmBoolean, JvmList, JvmLong, JvmNullable, JvmString, JvmUnit,
   },
   crate::jvm_types::{
      JvmFilter, JvmFilterKeyword, JvmFilterResult, JvmFilterStatus, JvmStatusId,
   },
};

#[derive(Debug, Eq, PartialEq, Clone, Deserialize)]
pub struct Filter {
   pub id: FilterId,
   pub title: Option<String>,
   pub context: Vec<FilterContext>,
   pub expire_time: Option<DateTime<Utc>>,
   pub filter_action: Option<FilterAction>,
   pub keywords: Vec<FilterKeyword>,
   pub statuses: Vec<FilterStatus>,
}

#[derive(Debug, Eq, PartialEq, Clone, Deserialize)]
pub struct FilterId(pub String);

#[derive(Debug, Eq, PartialEq, Clone, Deserialize)]
pub struct FilterContext(pub String);

#[derive(Debug, Eq, PartialEq, Clone, Deserialize)]
pub struct FilterAction(pub String);

#[derive(Debug, Eq, PartialEq, Clone, Deserialize)]
pub struct FilterKeyword {
   pub id: FilterKeywordId,
   pub keyword: Option<String>,
   pub whole_word: Option<bool>,
}

#[derive(Debug, Eq, PartialEq, Clone, Deserialize)]
pub struct FilterKeywordId(pub String);

#[derive(Debug, Eq, PartialEq, Clone, Deserialize)]
pub struct FilterStatus {
   pub id: FilterStatusId,
   pub status_id: StatusId,
}

#[derive(Debug, Eq, PartialEq, Clone, Deserialize)]
pub struct FilterStatusId(pub String);

#[derive(Debug, Eq, PartialEq, Clone, Deserialize)]
pub struct FilterResult {
   pub filter: Option<Filter>,
   pub keyword_matches: Vec<String>,
   pub status_matches: Vec<StatusId>,
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static FILTER_HELPER = impl struct FilterConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/Filter"
   {
      fn clone_into_jvm<'local>(..) -> JvmFilter<'local>
         where jvm_constructor: "(\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/util/List;\
            Ljava/lang/Long;\
            Ljava/lang/String;\
            Ljava/util/List;\
            Ljava/util/List;\
         )V";

      fn raw_id<'local>(..) -> String
         where jvm_type: JvmString<'local>,
               jvm_getter_method: "getRawId",
               jvm_return_type: "Ljava/lang/String;";

      fn title<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getTitle",
               jvm_return_type: "Ljava/lang/String;";

      fn raw_context<'local>(..) -> Vec<String>
         where jvm_type: JvmList<'local, JvmString<'local>>,
               jvm_getter_method: "getRawContext",
               jvm_return_type: "Ljava/util/List;";

      fn expire_time_epoch_millis<'local>(..) -> Option<i64>
         where jvm_type: JvmNullable<'local, JvmLong<'local>>,
               jvm_getter_method: "getExpireTimeEpochMillis",
               jvm_return_type: "Ljava/lang/Long;";

      fn raw_filter_action<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getRawFilterAction",
               jvm_return_type: "Ljava/lang/String;";

      fn keywords<'local>(..) -> Vec<FilterKeyword>
         where jvm_type: JvmList<'local, JvmFilterKeyword<'local>>,
               jvm_getter_method: "getKeywords",
               jvm_return_type: "Ljava/util/List;";

      fn statuses<'local>(..) -> Vec<FilterStatus>
         where jvm_type: JvmList<'local, JvmFilterStatus<'local>>,
               jvm_getter_method: "getStatuses",
               jvm_return_type: "Ljava/util/List;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmFilter<'local>> for Filter {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmFilter<'local> {
      FILTER_HELPER.clone_into_jvm(
         env,
         &self.id.0,
         &self.title,
         &self.context.iter().map(|c| &c.0).collect::<Vec<_>>(),
         &self.expire_time.map(|t| t.timestamp_millis()),
         &self.filter_action.as_ref().map(|a| &a.0),
         &self.keywords,
         &self.statuses,
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmFilter<'local>> for Filter {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmFilter<'local>
   ) -> Filter {
      let raw_id                   = FILTER_HELPER.raw_id                  (env, jvm_instance);
      let title                    = FILTER_HELPER.title                   (env, jvm_instance);
      let raw_context              = FILTER_HELPER.raw_context             (env, jvm_instance);
      let expire_time_epoch_millis = FILTER_HELPER.expire_time_epoch_millis(env, jvm_instance);
      let raw_filter_action        = FILTER_HELPER.raw_filter_action       (env, jvm_instance);
      let keywords                 = FILTER_HELPER.keywords                (env, jvm_instance);
      let statuses                 = FILTER_HELPER.statuses                (env, jvm_instance);

      Filter {
         id: FilterId(raw_id),
         title,
         context: raw_context.into_iter().map(FilterContext).collect(),
         expire_time: expire_time_epoch_millis
            .map(|time| DateTime::from_timestamp_millis(time).unwrap()),
         filter_action: raw_filter_action.map(FilterAction),
         keywords,
         statuses,
      }
   }
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static FILTER_KEYWORD_HELPER = impl struct FilterKeywordConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/Filter$Keyword"
   {
      fn clone_into_jvm<'local>(..) -> JvmFilterKeyword<'local>
         where jvm_constructor: "(\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/Boolean;\
            Lkotlin/Unit;\
         )V";

      fn raw_id<'local>(..) -> String
         where jvm_type: JvmString<'local>,
               jvm_getter_method: "getRawId",
               jvm_return_type: "Ljava/lang/String;";

      fn keyword<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getKeyword",
               jvm_return_type: "Ljava/lang/String;";

      fn whole_word<'local>(..) -> Option<bool>
         where jvm_type: JvmNullable<'local, JvmBoolean<'local>>,
               jvm_getter_method: "getWholeWord",
               jvm_return_type: "Ljava/lang/Boolean;";

      fn dummy<'local>(..) -> Option<()>
         where jvm_type: JvmNullable<'local, JvmUnit<'local>>,
               jvm_getter_method: "getDummy",
               jvm_return_type: "Lkotlin/Unit;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmFilterKeyword<'local>> for FilterKeyword {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmFilterKeyword<'local> {
      FILTER_KEYWORD_HELPER.clone_into_jvm(
         env,
         &self.id.0,
         &self.keyword,
         &self.whole_word,
         &None::<()>,
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmFilterKeyword<'local>> for FilterKeyword {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmFilterKeyword<'local>
   ) -> FilterKeyword {
      let raw_id     = FILTER_KEYWORD_HELPER.raw_id    (env, jvm_instance);
      let keyword    = FILTER_KEYWORD_HELPER.keyword   (env, jvm_instance);
      let whole_word = FILTER_KEYWORD_HELPER.whole_word(env, jvm_instance);

      FilterKeyword {
         id: FilterKeywordId(raw_id),
         keyword,
         whole_word,
      }
   }
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static FILTER_STATUS_HELPER = impl struct FilterStatusConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/Filter$FilterStatus"
   {
      fn clone_into_jvm<'local>(..) -> JvmFilterStatus<'local>
         where jvm_constructor: "(\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/String;\
         )V";

      fn raw_instance_url<'local>(..) -> String
         where jvm_type: JvmString<'local>,
               jvm_getter_method: "getRawInstanceUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn raw_id<'local>(..) -> String
         where jvm_type: JvmString<'local>,
               jvm_getter_method: "getRawId",
               jvm_return_type: "Ljava/lang/String;";

      fn raw_local_status_id<'local>(..) -> String
         where jvm_type: JvmString<'local>,
               jvm_getter_method: "getRawLocalStatusId",
               jvm_return_type: "Ljava/lang/String;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmFilterStatus<'local>> for FilterStatus {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmFilterStatus<'local> {
      FILTER_STATUS_HELPER.clone_into_jvm(
         env,
         &self.status_id.instance_url.as_str(),
         &self.id.0,
         &self.status_id.local.0,
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmFilterStatus<'local>> for FilterStatus {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmFilterStatus<'local>
   ) -> FilterStatus {
      use crate::status::StatusLocalId;

      let raw_instance_url    = FILTER_STATUS_HELPER.raw_instance_url   (env, jvm_instance);
      let raw_id              = FILTER_STATUS_HELPER.raw_id             (env, jvm_instance);
      let raw_status_local_id = FILTER_STATUS_HELPER.raw_local_status_id(env, jvm_instance);

      FilterStatus {
         id: FilterStatusId(raw_id),
         status_id: StatusId {
            instance_url: raw_instance_url.parse().unwrap(),
            local: StatusLocalId(raw_status_local_id)
         },
      }
   }
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static FILTER_RESULT_HELPER = impl struct FilterResultConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/FilterResult"
   {
      fn clone_into_jvm<'local>(..) -> JvmFilterResult<'local>
         where jvm_constructor: "(\
            Lcom/wcaokaze/probosqis/mastodon/entity/Filter;\
            Ljava/util/List;\
            Ljava/util/List;\
         )V";

      fn filter<'local>(..) -> Option<Filter>
         where jvm_type: JvmNullable<'local, JvmFilter<'local>>,
               jvm_getter_method: "getFilter",
               jvm_return_type: "Lcom/wcaokaze/probosqis/mastodon/entity/Filter;";

      fn keyword_matches<'local>(..) -> Vec<String>
         where jvm_type: JvmList<'local, JvmString<'local>>,
               jvm_getter_method: "getKeywordMatches",
               jvm_return_type: "Ljava/util/List;";

      fn status_matches<'local>(..) -> Vec<StatusId>
         where jvm_type: JvmList<'local, JvmStatusId<'local>>,
               jvm_getter_method: "getStatusMatches",
               jvm_return_type: "Ljava/util/List;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmFilterResult<'local>> for FilterResult {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmFilterResult<'local> {
      FILTER_RESULT_HELPER.clone_into_jvm(
         env,
         &self.filter,
         &self.keyword_matches,
         &self.status_matches,
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmFilterResult<'local>> for FilterResult {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmFilterResult<'local>
   ) -> FilterResult {
      let filter          = FILTER_RESULT_HELPER.filter         (env, jvm_instance);
      let keyword_matches = FILTER_RESULT_HELPER.keyword_matches(env, jvm_instance);
      let status_matches  = FILTER_RESULT_HELPER.status_matches (env, jvm_instance);

      FilterResult {
         filter,
         keyword_matches,
         status_matches,
      }
   }
}
