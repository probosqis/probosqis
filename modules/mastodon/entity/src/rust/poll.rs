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
use url::Url;
use panoptiqon::cache::Cache;
use crate::custom_emoji::CustomEmoji;

#[cfg(feature = "jvm")]
use {
   ext_panoptiqon::convert_jvm_helper,
   jni::JNIEnv,
   panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm},
   panoptiqon::jvm_types::{
      JvmBoolean, JvmCache, JvmList, JvmLong, JvmNullable, JvmString,
   },
   crate::jvm_types::{
      JvmCustomEmoji, JvmPoll, JvmPollNoCredential, JvmPollOption,
   },
};

#[derive(Debug, Eq, PartialEq, Clone, Deserialize)]
pub struct Poll {
   pub id: PollId,
   pub no_credential: Cache<NoCredentialPoll>,
   pub is_voted: Option<bool>,
   pub voted_options: Vec<i64>,
}

#[derive(Debug, Eq, PartialEq, Clone, Deserialize)]
pub struct NoCredentialPoll {
   pub id: PollId,
   pub expire_time: Option<DateTime<Utc>>,
   pub is_expired: Option<bool>,
   pub allows_multiple_choices: Option<bool>,
   pub vote_count: Option<i64>,
   pub voter_count: Option<i64>,
   pub poll_options: Vec<PollOption>,
   pub emojis: Vec<CustomEmoji>,
}

#[derive(Debug, Eq, PartialEq, Hash, Clone, Deserialize)]
pub struct PollId {
   pub instance_url: Url,
   pub local: PollLocalId,
}

#[derive(Debug, Eq, PartialEq, Hash, Clone, Deserialize)]
pub struct PollLocalId(pub String);

#[derive(Debug, Eq, PartialEq, Clone, Deserialize)]
pub struct PollOption {
   pub title: Option<String>,
   pub vote_count: Option<i64>,
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static POLL_HELPER = impl struct PollConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/Poll"
   {
      fn clone_into_jvm<'local>(..) -> JvmPoll<'local>
         where jvm_constructor: "(\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Lcom/wcaokaze/probosqis/panoptiqon/Cache;\
            Ljava/lang/Boolean;\
            Ljava/util/List;\
         )V";

      fn raw_instance_url<'local>(..) -> String
         where jvm_type: JvmString<'local>,
               jvm_getter_method: "getRawInstanceUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn raw_local_id<'local>(..) -> String
         where jvm_type: JvmString<'local>,
               jvm_getter_method: "getRawLocalId",
               jvm_return_type: "Ljava/lang/String;";

      fn no_credential<'local>(..) -> Cache<NoCredentialPoll>
         where jvm_type: JvmCache<'local, JvmPollNoCredential<'local>>,
               jvm_getter_method: "getNoCredential",
               jvm_return_type: "Lcom/wcaokaze/probosqis/panoptiqon/Cache;";

      fn is_voted<'local>(..) -> Option<bool>
         where jvm_type: JvmNullable<'local, JvmBoolean<'local>>,
               jvm_getter_method: "isVoted",
               jvm_return_type: "Ljava/lang/Boolean;";

      fn voted_options<'local>(..) -> Vec<i64>
         where jvm_type: JvmList<'local, JvmLong<'local>>,
               jvm_getter_method: "getVotedOptions",
               jvm_return_type: "Ljava/util/List;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmPoll<'local>> for Poll {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmPoll<'local> {
      POLL_HELPER.clone_into_jvm(
         env,
         self.id.instance_url.as_str(),
         &self.id.local.0,
         &self.no_credential,
         &self.is_voted,
         &self.voted_options,
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmPoll<'local>> for Poll {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmPoll<'local>
   ) -> Poll {
      let raw_instance_url = POLL_HELPER.raw_instance_url(env, jvm_instance);
      let raw_local_id     = POLL_HELPER.raw_local_id    (env, jvm_instance);
      let no_credential    = POLL_HELPER.no_credential   (env, jvm_instance);
      let is_voted         = POLL_HELPER.is_voted        (env, jvm_instance);
      let voted_options    = POLL_HELPER.voted_options   (env, jvm_instance);

      Poll {
         id: PollId {
            instance_url: raw_instance_url.parse().unwrap(),
            local: PollLocalId(raw_local_id)
         },
         no_credential,
         is_voted,
         voted_options,
      }
   }
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static NO_CREDENTIAL_POLL_HELPER = impl struct NoCredentialPollConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/Poll$NoCredential"
   {
      fn clone_into_jvm<'local>(..) -> JvmPollNoCredential<'local>
         where jvm_constructor: "(\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/Long;\
            Ljava/lang/Boolean;\
            Ljava/lang/Boolean;\
            Ljava/lang/Long;\
            Ljava/lang/Long;\
            Ljava/util/List;\
            Ljava/util/List;\
         )V";

      fn raw_instance_url<'local>(..) -> String
         where jvm_type: JvmString<'local>,
               jvm_getter_method: "getRawInstanceUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn raw_local_id<'local>(..) -> String
         where jvm_type: JvmString<'local>,
               jvm_getter_method: "getRawLocalId",
               jvm_return_type: "Ljava/lang/String;";

      fn expire_time_epoch_millis<'local>(..) -> Option<i64>
         where jvm_type: JvmNullable<'local, JvmLong<'local>>,
               jvm_getter_method: "getExpireTimeEpochMillis",
               jvm_return_type: "Ljava/lang/Long;";

      fn is_expired<'local>(..) -> Option<bool>
         where jvm_type: JvmNullable<'local, JvmBoolean<'local>>,
               jvm_getter_method: "isExpired",
               jvm_return_type: "Ljava/lang/Boolean;";

      fn allows_multiple_choices<'local>(..) -> Option<bool>
         where jvm_type: JvmNullable<'local, JvmBoolean<'local>>,
               jvm_getter_method: "getAllowsMultipleChoices",
               jvm_return_type: "Ljava/lang/Boolean;";

      fn vote_count<'local>(..) -> Option<i64>
         where jvm_type: JvmNullable<'local, JvmLong<'local>>,
               jvm_getter_method: "getVoteCount",
               jvm_return_type: "Ljava/lang/Long;";

      fn voter_count<'local>(..) -> Option<i64>
         where jvm_type: JvmNullable<'local, JvmLong<'local>>,
               jvm_getter_method: "getVoterCount",
               jvm_return_type: "Ljava/lang/Long;";

      fn poll_options<'local>(..) -> Vec<PollOption>
         where jvm_type: JvmList<'local, JvmPollOption<'local>>,
               jvm_getter_method: "getPollOptions",
               jvm_return_type: "Ljava/util/List;";

      fn emojis<'local>(..) -> Vec<CustomEmoji>
         where jvm_type: JvmList<'local, JvmCustomEmoji<'local>>,
               jvm_getter_method: "getEmojis",
               jvm_return_type: "Ljava/util/List;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmPollNoCredential<'local>> for NoCredentialPoll {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmPollNoCredential<'local> {
      NO_CREDENTIAL_POLL_HELPER.clone_into_jvm(
         env,
         self.id.instance_url.as_str(),
         &self.id.local.0,
         &self.expire_time.map(|t| t.timestamp_millis()),
         &self.is_expired,
         &self.allows_multiple_choices,
         &self.vote_count,
         &self.voter_count,
         &self.poll_options,
         &self.emojis,
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmPollNoCredential<'local>> for NoCredentialPoll {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmPollNoCredential<'local>
   ) -> NoCredentialPoll {
      let raw_instance_url         = NO_CREDENTIAL_POLL_HELPER.raw_instance_url        (env, jvm_instance);
      let raw_local_id             = NO_CREDENTIAL_POLL_HELPER.raw_local_id            (env, jvm_instance);
      let expire_time_epoch_millis = NO_CREDENTIAL_POLL_HELPER.expire_time_epoch_millis(env, jvm_instance);
      let is_expired               = NO_CREDENTIAL_POLL_HELPER.is_expired              (env, jvm_instance);
      let allows_multiple_choices  = NO_CREDENTIAL_POLL_HELPER.allows_multiple_choices (env, jvm_instance);
      let vote_count               = NO_CREDENTIAL_POLL_HELPER.vote_count              (env, jvm_instance);
      let voter_count              = NO_CREDENTIAL_POLL_HELPER.voter_count             (env, jvm_instance);
      let poll_options             = NO_CREDENTIAL_POLL_HELPER.poll_options            (env, jvm_instance);
      let emojis                   = NO_CREDENTIAL_POLL_HELPER.emojis                  (env, jvm_instance);

      NoCredentialPoll {
         id: PollId {
            instance_url: raw_instance_url.parse().unwrap(),
            local: PollLocalId(raw_local_id)
         },
         expire_time: expire_time_epoch_millis
            .map(|time| DateTime::from_timestamp_millis(time).unwrap()),
         is_expired,
         allows_multiple_choices,
         vote_count,
         voter_count,
         poll_options,
         emojis,
      }
   }
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static POLL_OPTION_HELPER = impl struct PollOptionConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/Poll$Option"
   {
      fn clone_into_jvm<'local>(..) -> JvmPollOption<'local>
         where jvm_constructor: "(\
            Ljava/lang/String;\
            Ljava/lang/Long;\
         )V";

      fn title<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getTitle",
               jvm_return_type: "Ljava/lang/String;";

      fn vote_count<'local>(..) -> Option<i64>
         where jvm_type: JvmNullable<'local, JvmLong<'local>>,
               jvm_getter_method: "getVoteCount",
               jvm_return_type: "Ljava/lang/Long;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmPollOption<'local>> for PollOption {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmPollOption<'local> {
      POLL_OPTION_HELPER.clone_into_jvm(
         env,
         &self.title,
         &self.vote_count,
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmPollOption<'local>> for PollOption {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmPollOption<'local>
   ) -> PollOption {
      let title      = POLL_OPTION_HELPER.title     (env, jvm_instance);
      let vote_count = POLL_OPTION_HELPER.vote_count(env, jvm_instance);

      PollOption {
         title,
         vote_count,
      }
   }
}
