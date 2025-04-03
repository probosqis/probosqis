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
use isolang::Language;
use panoptiqon::cache::Cache;
use serde::Deserialize;
use url::Url;
use crate::account::{Account, AccountId};
use crate::application::Application;
use crate::custom_emoji::CustomEmoji;
use crate::filter::FilterResult;
use crate::media_attachment::MediaAttachment;
use crate::poll::{NoCredentialPoll, Poll};
use crate::preview_card::PreviewCard;

#[cfg(feature = "jvm")]
use {
   jni::JNIEnv,
   ext_panoptiqon::convert_jvm_helper,
   panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm},
   panoptiqon::jvm_types::{
      JvmBoolean, JvmCache, JvmList, JvmLong, JvmNullable, JvmString, JvmUnit,
   },
   crate::jvm_types::{
      JvmAccount, JvmApplication, JvmCustomEmoji, JvmFilterResult,
      JvmMediaAttachment, JvmPoll, JvmPollNoCredential, JvmPreviewCard, JvmStatus,
      JvmStatusId, JvmStatusHashtag, JvmStatusMention, JvmStatusNoCredential,
   },
};

#[derive(Debug, PartialEq, Clone, Deserialize)]
pub struct Status {
   pub id: StatusId,
   pub no_credential: Cache<NoCredentialStatus>,
   pub boosted_status: Option<Cache<Status>>,
   pub poll: Option<Poll>,
   pub is_favorited: Option<bool>,
   pub is_boosted: Option<bool>,
   pub is_muted: Option<bool>,
   pub is_bookmarked: Option<bool>,
   pub is_pinned: Option<bool>,
   pub filter_results: Vec<FilterResult>,
}

#[derive(Debug, PartialEq, Clone, Deserialize)]
pub struct NoCredentialStatus {
   pub id: StatusId,
   pub uri: Option<String>,
   pub created_time: Option<DateTime<Utc>>,
   pub account: Option<Cache<Account>>,
   pub content: Option<String>,
   pub visibility: Option<StatusVisibility>,
   pub is_sensitive: Option<bool>,
   pub spoiler_text: Option<String>,
   pub media_attachments: Vec<MediaAttachment>,
   pub application: Option<Application>,
   pub mentions: Vec<StatusMention>,
   pub hashtags: Vec<StatusHashtag>,
   pub emojis: Vec<CustomEmoji>,
   pub boost_count: Option<i64>,
   pub favorite_count: Option<i64>,
   pub reply_count: Option<i64>,
   pub url: Option<Url>,
   pub replied_status_id: Option<StatusId>,
   pub replied_account_id: Option<AccountId>,
   pub boosted_status: Option<Cache<NoCredentialStatus>>,
   pub poll: Option<Cache<NoCredentialPoll>>,
   pub card: Option<PreviewCard>,
   pub language: Option<Language>,
   pub text: Option<String>,
   pub edited_time: Option<DateTime<Utc>>,
}

#[derive(Debug, Eq, PartialEq, Hash, Clone, Deserialize)]
pub struct StatusId {
   pub instance_url: Url,
   pub local: StatusLocalId,
}

#[derive(Debug, Eq, PartialEq, Hash, Clone, Deserialize)]
pub struct StatusLocalId(pub String);

#[derive(Debug, Eq, PartialEq, Clone, Deserialize)]
pub struct StatusVisibility(pub String);

#[derive(Debug, Eq, PartialEq, Clone, Deserialize)]
pub struct StatusMention {
   pub mentioned_account_id: Option<AccountId>,
   pub mentioned_account_username: Option<String>,
   pub mentioned_account_url: Option<Url>,
   pub mentioned_account_acct: Option<String>,
}

#[derive(Debug, Eq, PartialEq, Clone, Deserialize)]
pub struct StatusHashtag {
   pub name: Option<String>,
   pub url: Option<Url>,
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static STATUS_HELPER = impl struct StatusConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/Status"
   {
      fn clone_into_jvm<'local>(..) -> JvmStatus<'local>
         where jvm_constructor: "(\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Lcom/wcaokaze/probosqis/panoptiqon/Cache;\
            Lcom/wcaokaze/probosqis/panoptiqon/Cache;\
            Lcom/wcaokaze/probosqis/mastodon/entity/Poll;\
            Ljava/lang/Boolean;\
            Ljava/lang/Boolean;\
            Ljava/lang/Boolean;\
            Ljava/lang/Boolean;\
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

      fn no_credential<'local>(..) -> Cache<NoCredentialStatus>
         where jvm_type: JvmCache<'local, JvmStatusNoCredential<'local>>,
               jvm_getter_method: "getNoCredential",
               jvm_return_type: "Lcom/wcaokaze/probosqis/panoptiqon/Cache;";

      fn boosted_status<'local>(..) -> Option<Cache<Status>>
         where jvm_type: JvmNullable<'local, JvmCache<'local, JvmStatus<'local>>>,
               jvm_getter_method: "getBoostedStatus",
               jvm_return_type: "Lcom/wcaokaze/probosqis/panoptiqon/Cache;";

      fn poll<'local>(..) -> Option<Poll>
         where jvm_type: JvmNullable<'local, JvmPoll<'local>>,
               jvm_getter_method: "getPoll",
               jvm_return_type: "Lcom/wcaokaze/probosqis/mastodon/entity/Poll;";

      fn is_favorited<'local>(..) -> Option<bool>
         where jvm_type: JvmNullable<'local, JvmBoolean<'local>>,
               jvm_getter_method: "isFavorited",
               jvm_return_type: "Ljava/lang/Boolean;";

      fn is_boosted<'local>(..) -> Option<bool>
         where jvm_type: JvmNullable<'local, JvmBoolean<'local>>,
               jvm_getter_method: "isBoosted",
               jvm_return_type: "Ljava/lang/Boolean;";

      fn is_muted<'local>(..) -> Option<bool>
         where jvm_type: JvmNullable<'local, JvmBoolean<'local>>,
               jvm_getter_method: "isMuted",
               jvm_return_type: "Ljava/lang/Boolean;";

      fn is_bookmarked<'local>(..) -> Option<bool>
         where jvm_type: JvmNullable<'local, JvmBoolean<'local>>,
               jvm_getter_method: "isBookmarked",
               jvm_return_type: "Ljava/lang/Boolean;";

      fn is_pinned<'local>(..) -> Option<bool>
         where jvm_type: JvmNullable<'local, JvmBoolean<'local>>,
               jvm_getter_method: "isPinned",
               jvm_return_type: "Ljava/lang/Boolean;";

      fn filter_results<'local>(..) -> Vec<FilterResult>
         where jvm_type: JvmList<'local, JvmFilterResult<'local>>,
               jvm_getter_method: "getFilterResults",
               jvm_return_type: "Ljava/util/List;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmStatus<'local>> for Status {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmStatus<'local> {
      STATUS_HELPER.clone_into_jvm(
         env,
         &self.id.instance_url.as_str(),
         &self.id.local.0,
         &self.no_credential,
         &self.boosted_status,
         &self.poll,
         &self.is_favorited,
         &self.is_boosted,
         &self.is_muted,
         &self.is_bookmarked,
         &self.is_pinned,
         &self.filter_results,
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmStatus<'local>> for Status {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmStatus<'local>
   ) -> Status {
      let raw_instance_url = STATUS_HELPER.raw_instance_url(env, jvm_instance);
      let raw_local_id     = STATUS_HELPER.raw_local_id    (env, jvm_instance);
      let no_credential    = STATUS_HELPER.no_credential   (env, jvm_instance);
      let boosted_status   = STATUS_HELPER.boosted_status  (env, jvm_instance);
      let poll             = STATUS_HELPER.poll            (env, jvm_instance);
      let is_favorited     = STATUS_HELPER.is_favorited    (env, jvm_instance);
      let is_boosted       = STATUS_HELPER.is_boosted      (env, jvm_instance);
      let is_muted         = STATUS_HELPER.is_muted        (env, jvm_instance);
      let is_bookmarked    = STATUS_HELPER.is_bookmarked   (env, jvm_instance);
      let is_pinned        = STATUS_HELPER.is_pinned       (env, jvm_instance);
      let filter_results   = STATUS_HELPER.filter_results  (env, jvm_instance);

      Status {
         id: StatusId {
            instance_url: raw_instance_url.parse().unwrap(),
            local: StatusLocalId(raw_local_id)
         },
         no_credential,
         boosted_status,
         poll,
         is_favorited,
         is_boosted,
         is_muted,
         is_bookmarked,
         is_pinned,
         filter_results,
      }
   }
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static NO_CREDENTIAL_STATUS_HELPER = impl struct NoCredentialStatusConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/Status$NoCredential"
   {
      fn clone_into_jvm<'local>(..) -> JvmStatusNoCredential<'local>
         where jvm_constructor: "(\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/Long;\
            Lcom/wcaokaze/probosqis/panoptiqon/Cache;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/Boolean;\
            Ljava/lang/String;\
            Ljava/util/List;\
            Lcom/wcaokaze/probosqis/mastodon/entity/Application;\
            Ljava/util/List;\
            Ljava/util/List;\
            Ljava/util/List;\
            Ljava/lang/Long;\
            Ljava/lang/Long;\
            Ljava/lang/Long;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Lcom/wcaokaze/probosqis/panoptiqon/Cache;\
            Lcom/wcaokaze/probosqis/panoptiqon/Cache;\
            Lcom/wcaokaze/probosqis/mastodon/entity/PreviewCard;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/Long;\
         )V";

      fn raw_instance_url<'local>(..) -> String
         where jvm_type: JvmString<'local>,
               jvm_getter_method: "getRawInstanceUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn raw_local_id<'local>(..) -> String
         where jvm_type: JvmString<'local>,
               jvm_getter_method: "getRawLocalId",
               jvm_return_type: "Ljava/lang/String;";

      fn uri<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getUri",
               jvm_return_type: "Ljava/lang/String;";

      fn created_time_epoch_millis<'local>(..) -> Option<i64>
         where jvm_type: JvmNullable<'local, JvmLong<'local>>,
               jvm_getter_method: "getCreatedTimeEpochMillis",
               jvm_return_type: "Ljava/lang/Long;";

      fn account<'local>(..) -> Option<Cache<Account>>
         where jvm_type: JvmNullable<'local, JvmCache<'local, JvmAccount<'local>>>,
               jvm_getter_method: "getAccount",
               jvm_return_type: "Lcom/wcaokaze/probosqis/panoptiqon/Cache;";

      fn content<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getContent",
               jvm_return_type: "Ljava/lang/String;";

      fn raw_visibility<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getRawVisibility",
               jvm_return_type: "Ljava/lang/String;";

      fn is_sensitive<'local>(..) -> Option<bool>
         where jvm_type: JvmNullable<'local, JvmBoolean<'local>>,
               jvm_getter_method: "isSensitive",
               jvm_return_type: "Ljava/lang/Boolean;";

      fn spoiler_text<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getSpoilerText",
               jvm_return_type: "Ljava/lang/String;";

      fn media_attachments<'local>(..) -> Vec<MediaAttachment>
         where jvm_type: JvmList<'local, JvmMediaAttachment<'local>>,
               jvm_getter_method: "getMediaAttachments",
               jvm_return_type: "Ljava/util/List;";

      fn application<'local>(..) -> Option<Application>
         where jvm_type: JvmNullable<'local, JvmApplication<'local>>,
               jvm_getter_method: "getApplication",
               jvm_return_type: "Lcom/wcaokaze/probosqis/mastodon/entity/Application;";

      fn mentions<'local>(..) -> Vec<StatusMention>
         where jvm_type: JvmList<'local, JvmStatusMention<'local>>,
               jvm_getter_method: "getMentions",
               jvm_return_type: "Ljava/util/List;";

      fn hashtags<'local>(..) -> Vec<StatusHashtag>
         where jvm_type: JvmList<'local, JvmStatusHashtag<'local>>,
               jvm_getter_method: "getHashtags",
               jvm_return_type: "Ljava/util/List;";

      fn emojis<'local>(..) -> Vec<CustomEmoji>
         where jvm_type: JvmList<'local, JvmCustomEmoji<'local>>,
               jvm_getter_method: "getEmojis",
               jvm_return_type: "Ljava/util/List;";

      fn boost_count<'local>(..) -> Option<i64>
         where jvm_type: JvmNullable<'local, JvmLong<'local>>,
               jvm_getter_method: "getBoostCount",
               jvm_return_type: "Ljava/lang/Long;";

      fn favorite_count<'local>(..) -> Option<i64>
         where jvm_type: JvmNullable<'local, JvmLong<'local>>,
               jvm_getter_method: "getFavoriteCount",
               jvm_return_type: "Ljava/lang/Long;";

      fn reply_count<'local>(..) -> Option<i64>
         where jvm_type: JvmNullable<'local, JvmLong<'local>>,
               jvm_getter_method: "getReplyCount",
               jvm_return_type: "Ljava/lang/Long;";

      fn raw_url<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getRawUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn raw_replied_status_local_id<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getRawRepliedStatusLocalId",
               jvm_return_type: "Ljava/lang/String;";

      fn raw_replied_account_local_id<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getRawRepliedAccountLocalId",
               jvm_return_type: "Ljava/lang/String;";

      fn boosted_status<'local>(..) -> Option<Cache<NoCredentialStatus>>
         where jvm_type: JvmNullable<'local, JvmCache<'local, JvmStatusNoCredential<'local>>>,
               jvm_getter_method: "getBoostedStatus",
               jvm_return_type: "Lcom/wcaokaze/probosqis/panoptiqon/Cache;";

      fn poll<'local>(..) -> Option<Cache<NoCredentialPoll>>
         where jvm_type: JvmNullable<'local, JvmCache<'local, JvmPollNoCredential<'local>>>,
               jvm_getter_method: "getPoll",
               jvm_return_type: "Lcom/wcaokaze/probosqis/panoptiqon/Cache;";

      fn card<'local>(..) -> Option<PreviewCard>
         where jvm_type: JvmNullable<'local, JvmPreviewCard<'local>>,
               jvm_getter_method: "getCard",
               jvm_return_type: "Lcom/wcaokaze/probosqis/mastodon/entity/PreviewCard;";

      fn language<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getLanguage",
               jvm_return_type: "Ljava/lang/String;";

      fn text<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getText",
               jvm_return_type: "Ljava/lang/String;";

      fn edited_time_epoch_millis<'local>(..) -> Option<i64>
         where jvm_type: JvmNullable<'local, JvmLong<'local>>,
               jvm_getter_method: "getEditedTimeEpochMillis",
               jvm_return_type: "Ljava/lang/Long;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmStatusNoCredential<'local>> for NoCredentialStatus {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmStatusNoCredential<'local> {
      NO_CREDENTIAL_STATUS_HELPER.clone_into_jvm(
         env,
         &self.id.instance_url.as_str(),
         &self.id.local.0,
         &self.uri,
         &self.created_time.map(|t| t.timestamp_millis()),
         &self.account,
         &self.content,
         &self.visibility.as_ref().map(|v| &v.0),
         &self.is_sensitive,
         &self.spoiler_text,
         &self.media_attachments,
         &self.application,
         &self.mentions,
         &self.hashtags,
         &self.emojis,
         &self.boost_count,
         &self.favorite_count,
         &self.reply_count,
         &self.url.as_ref().map(Url::as_str),
         &self.replied_status_id.as_ref().map(|id| &id.local.0),
         &self.replied_account_id.as_ref().map(|id| &id.local.0),
         &self.boosted_status,
         &self.poll,
         &self.card,
         &self.language.map(|l| l.to_639_1().unwrap()),
         &self.text,
         &self.edited_time.map(|t| t.timestamp_millis()),
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmStatusNoCredential<'local>> for NoCredentialStatus {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmStatusNoCredential<'local>
   ) -> NoCredentialStatus {
      use crate::account::AccountLocalId;

      let raw_instance_url             = NO_CREDENTIAL_STATUS_HELPER.raw_instance_url            (env, jvm_instance);
      let raw_local_id                 = NO_CREDENTIAL_STATUS_HELPER.raw_local_id                (env, jvm_instance);
      let uri                          = NO_CREDENTIAL_STATUS_HELPER.uri                         (env, jvm_instance);
      let created_time_epoch_millis    = NO_CREDENTIAL_STATUS_HELPER.created_time_epoch_millis   (env, jvm_instance);
      let account                      = NO_CREDENTIAL_STATUS_HELPER.account                     (env, jvm_instance);
      let content                      = NO_CREDENTIAL_STATUS_HELPER.content                     (env, jvm_instance);
      let raw_visibility               = NO_CREDENTIAL_STATUS_HELPER.raw_visibility              (env, jvm_instance);
      let is_sensitive                 = NO_CREDENTIAL_STATUS_HELPER.is_sensitive                (env, jvm_instance);
      let spoiler_text                 = NO_CREDENTIAL_STATUS_HELPER.spoiler_text                (env, jvm_instance);
      let media_attachments            = NO_CREDENTIAL_STATUS_HELPER.media_attachments           (env, jvm_instance);
      let application                  = NO_CREDENTIAL_STATUS_HELPER.application                 (env, jvm_instance);
      let mentions                     = NO_CREDENTIAL_STATUS_HELPER.mentions                    (env, jvm_instance);
      let hashtags                     = NO_CREDENTIAL_STATUS_HELPER.hashtags                    (env, jvm_instance);
      let emojis                       = NO_CREDENTIAL_STATUS_HELPER.emojis                      (env, jvm_instance);
      let boost_count                  = NO_CREDENTIAL_STATUS_HELPER.boost_count                 (env, jvm_instance);
      let favorite_count               = NO_CREDENTIAL_STATUS_HELPER.favorite_count              (env, jvm_instance);
      let reply_count                  = NO_CREDENTIAL_STATUS_HELPER.reply_count                 (env, jvm_instance);
      let raw_url                      = NO_CREDENTIAL_STATUS_HELPER.raw_url                     (env, jvm_instance);
      let raw_replied_status_local_id  = NO_CREDENTIAL_STATUS_HELPER.raw_replied_status_local_id (env, jvm_instance);
      let raw_replied_account_local_id = NO_CREDENTIAL_STATUS_HELPER.raw_replied_account_local_id(env, jvm_instance);
      let boosted_status               = NO_CREDENTIAL_STATUS_HELPER.boosted_status              (env, jvm_instance);
      let poll                         = NO_CREDENTIAL_STATUS_HELPER.poll                        (env, jvm_instance);
      let card                         = NO_CREDENTIAL_STATUS_HELPER.card                        (env, jvm_instance);
      let language                     = NO_CREDENTIAL_STATUS_HELPER.language                    (env, jvm_instance);
      let text                         = NO_CREDENTIAL_STATUS_HELPER.text                        (env, jvm_instance);
      let edited_time_epoch_millis     = NO_CREDENTIAL_STATUS_HELPER.edited_time_epoch_millis    (env, jvm_instance);

      let instance_url = raw_instance_url.parse::<Url>().unwrap();

      NoCredentialStatus {
         id: StatusId {
            instance_url: instance_url.clone(),
            local: StatusLocalId(raw_local_id)
         },
         uri,
         created_time: created_time_epoch_millis
            .map(|t| DateTime::from_timestamp_millis(t).unwrap()),
         account,
         content,
         visibility: raw_visibility.map(StatusVisibility),
         is_sensitive,
         spoiler_text,
         media_attachments,
         application,
         mentions,
         hashtags,
         emojis,
         boost_count,
         favorite_count,
         reply_count,
         url: raw_url.map(|u| u.parse().unwrap()),
         replied_status_id: raw_replied_status_local_id.map(|id|
            StatusId {
               instance_url: instance_url.clone(),
               local: StatusLocalId(id)
            }
         ),
         replied_account_id: raw_replied_account_local_id.map(|id|
            AccountId {
               instance_url: instance_url.clone(),
               local: AccountLocalId(id)
            }
         ),
         boosted_status,
         poll,
         card,
         language: language.map(|code| Language::from_639_1(&code).unwrap()),
         text,
         edited_time: edited_time_epoch_millis
            .map(|time| DateTime::from_timestamp_millis(time).unwrap()),
      }
   }
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static STATUS_ID_HELPER = impl struct StatusIdConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/Status$Id"
   {
      fn clone_into_jvm<'local>(..) -> JvmStatusId<'local>
         where jvm_constructor: "(\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Lkotlin/Unit;\
         )V";

      fn raw_instance_url<'local>(..) -> String
         where jvm_type: JvmString<'local>,
               jvm_getter_method: "getRawInstanceUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn raw_local_id<'local>(..) -> String
         where jvm_type: JvmString<'local>,
               jvm_getter_method: "getRawLocalId",
               jvm_return_type: "Ljava/lang/String;";

      fn dummy<'local>(..) -> Option<()>
         where jvm_type: JvmNullable<'local, JvmUnit<'local>>,
               jvm_getter_method: "getDummy",
               jvm_return_type: "Lkotlin/Unit;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmStatusId<'local>> for StatusId {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmStatusId<'local> {
      STATUS_ID_HELPER.clone_into_jvm(
         env,
         &self.instance_url.as_str(),
         &self.local.0,
         &None::<()>
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmStatusId<'local>> for StatusId {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmStatusId<'local>
   ) -> StatusId {
      let raw_instance_url = STATUS_ID_HELPER.raw_instance_url(env, jvm_instance);
      let raw_local_id     = STATUS_ID_HELPER.raw_local_id    (env, jvm_instance);

      StatusId {
         instance_url: raw_instance_url.parse().unwrap(),
         local: StatusLocalId(raw_local_id),
      }
   }
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static STATUS_MENTION_HELPER = impl struct StatusMentionConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/Status$Mention"
   {
      fn clone_into_jvm<'local>(..) -> JvmStatusMention<'local>
         where jvm_constructor: "(\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/String;\
         )V";

      fn raw_instance_url<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getRawInstanceUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn raw_mentioned_account_local_id<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getRawMentionedAccountLocalId",
               jvm_return_type: "Ljava/lang/String;";

      fn mentioned_account_username<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getMentionedAccountUsername",
               jvm_return_type: "Ljava/lang/String;";

      fn raw_mentioned_account_url<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getRawMentionedAccountUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn mentioned_account_acct<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getMentionedAccountAcct",
               jvm_return_type: "Ljava/lang/String;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmStatusMention<'local>> for StatusMention {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmStatusMention<'local> {
      STATUS_MENTION_HELPER.clone_into_jvm(
         env,
         &self.mentioned_account_id.as_ref().map(|id| id.instance_url.as_str()),
         &self.mentioned_account_id.as_ref().map(|id| &id.local.0),
         &self.mentioned_account_username,
         &self.mentioned_account_url.as_ref().map(|u| u.as_str()),
         &self.mentioned_account_acct,
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmStatusMention<'local>> for StatusMention {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmStatusMention<'local>
   ) -> StatusMention {
      use crate::account::AccountLocalId;

      let raw_instance_url               = STATUS_MENTION_HELPER.raw_instance_url              (env, jvm_instance);
      let raw_mentioned_account_local_id = STATUS_MENTION_HELPER.raw_mentioned_account_local_id(env, jvm_instance);
      let mentioned_account_username     = STATUS_MENTION_HELPER.mentioned_account_username    (env, jvm_instance);
      let raw_mentioned_account_url      = STATUS_MENTION_HELPER.raw_mentioned_account_url     (env, jvm_instance);
      let mentioned_account_acct         = STATUS_MENTION_HELPER.mentioned_account_acct        (env, jvm_instance);

      StatusMention {
         mentioned_account_id: raw_instance_url.zip(raw_mentioned_account_local_id)
            .map(|(inst, local)| AccountId {
               instance_url: inst.parse().unwrap(),
               local: AccountLocalId(local),
            }),
         mentioned_account_username,
         mentioned_account_url: raw_mentioned_account_url.map(|u| u.parse().unwrap()),
         mentioned_account_acct,
      }
   }
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static STATUS_HASHTAG_HELPER = impl struct StatusHashtagConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/Status$Hashtag"
   {
      fn clone_into_jvm<'local>(..) -> JvmStatusHashtag<'local>
         where jvm_constructor: "(\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Lkotlin/Unit;\
         )V";

      fn name<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getName",
               jvm_return_type: "Ljava/lang/String;";

      fn raw_url<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getRawUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn dummy<'local>(..) -> Option<()>
         where jvm_type: JvmNullable<'local, JvmUnit<'local>>,
               jvm_getter_method: "getDummy",
               jvm_return_type: "Lkotlin/Unit;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmStatusHashtag<'local>> for StatusHashtag {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmStatusHashtag<'local> {
      STATUS_HASHTAG_HELPER.clone_into_jvm(
         env,
         &self.name,
         &self.url.as_ref().map(|u| u.as_str()),
         &None::<()>,
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmStatusHashtag<'local>> for StatusHashtag {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmStatusHashtag<'local>
   ) -> StatusHashtag {
      let name    = STATUS_HASHTAG_HELPER.name   (env, jvm_instance);
      let raw_url = STATUS_HASHTAG_HELPER.raw_url(env, jvm_instance);

      StatusHashtag {
         name,
         url: raw_url.map(|u| u.parse().unwrap()),
      }
   }
}
