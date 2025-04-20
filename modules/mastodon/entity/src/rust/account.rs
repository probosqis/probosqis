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
use crate::custom_emoji::CustomEmoji;
use crate::instance::Instance;
use crate::role::Role;
use crate::status::StatusVisibility;

#[cfg(feature = "jvm")]
use {
   ext_panoptiqon::convert_jvm_helper,
   jni::JNIEnv,
   panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm},
   panoptiqon::jvm_types::{
      JvmBoolean, JvmCache, JvmList, JvmLong, JvmNullable, JvmString, JvmUnit,
   },
   crate::jvm_types::{
      JvmAccount, JvmAccountProfileField, JvmCredentialAccount, JvmCustomEmoji,
      JvmInstance, JvmRelationalAccount, JvmRole,
   },
};

#[derive(Debug, Eq, PartialEq, Clone, Deserialize)]
pub struct Account {
   pub instance: Cache<Instance>,
   pub id: AccountId,
   pub username: Option<String>,
   pub acct: Option<String>,
   pub url: Option<Url>,
   pub display_name: Option<String>,
   pub profile_note: Option<String>,
   pub avatar_image_url: Option<Url>,
   pub avatar_static_image_url: Option<Url>,
   pub header_image_url: Option<Url>,
   pub header_static_image_url: Option<Url>,
   pub is_locked: Option<bool>,
   pub profile_fields: Vec<AccountProfileField>,
   pub emojis_in_profile: Vec<CustomEmoji>,
   pub is_bot: Option<bool>,
   pub is_group: Option<bool>,
   pub is_discoverable: Option<bool>,
   pub is_noindex: Option<bool>,
   pub moved_to: Option<Cache<Account>>,
   pub is_suspended: Option<bool>,
   pub is_limited: Option<bool>,
   pub created_time: Option<DateTime<Utc>>,
   pub last_status_post_time: Option<DateTime<Utc>>,
   pub status_count: Option<u64>,
   pub follower_count: Option<u64>,
   pub followee_count: Option<u64>,
}

#[derive(Debug, Eq, PartialEq, Clone, Deserialize)]
pub struct CredentialAccount {
   pub id: AccountId,
   pub account: Cache<Account>,
   pub raw_profile_note: Option<String>,
   pub raw_profile_fields: Vec<AccountProfileField>,
   pub default_post_visibility: Option<StatusVisibility>,
   pub default_post_sensitivity: Option<bool>,
   pub default_post_language: Option<Language>,
   pub follow_request_count: Option<u64>,
   pub role: Option<Role>,
}

#[derive(Debug, Eq, PartialEq, Clone, Deserialize)]
pub struct RelationalAccount {
   pub account: Cache<Account>,
   pub mute_expire_time: Option<DateTime<Utc>>,
}

#[derive(Debug, Eq, PartialEq, Hash, Clone, Deserialize)]
pub struct AccountId {
   pub instance_url: Url,
   pub local: AccountLocalId,
}

#[derive(Debug, Eq, PartialEq, Hash, Clone, Deserialize)]
pub struct AccountLocalId(pub String);

#[derive(Debug, Eq, PartialEq, Clone, Deserialize)]
pub struct AccountProfileField {
   pub name: Option<String>,
   pub value: Option<String>,
   pub verified_time: Option<DateTime<Utc>>,
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static ACCOUNT_HELPER = impl struct AccountConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/Account"
   {
      fn clone_into_jvm<'local>(..) -> JvmAccount<'local>
         where jvm_constructor: "(\
            Lcom/wcaokaze/probosqis/panoptiqon/Cache;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/Boolean;\
            Ljava/util/List;\
            Ljava/util/List;\
            Ljava/lang/Boolean;\
            Ljava/lang/Boolean;\
            Ljava/lang/Boolean;\
            Ljava/lang/Boolean;\
            Lcom/wcaokaze/probosqis/panoptiqon/Cache;\
            Ljava/lang/Boolean;\
            Ljava/lang/Boolean;\
            Ljava/lang/Long;\
            Ljava/lang/Long;\
            Ljava/lang/Long;\
            Ljava/lang/Long;\
            Ljava/lang/Long;\
         )V";

      fn instance<'local>(..) -> Cache<Instance>
         where jvm_type: JvmCache<'local, JvmInstance<'local>>,
               jvm_getter_method: "getInstance",
               jvm_return_type: "Lcom/wcaokaze/probosqis/panoptiqon/Cache;";

      fn raw_instance_url<'local>(..) -> String
         where jvm_type: JvmString<'local>,
               jvm_getter_method: "getRawInstanceUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn raw_local_id<'local>(..) -> String
         where jvm_type: JvmString<'local>,
               jvm_getter_method: "getRawLocalId",
               jvm_return_type: "Ljava/lang/String;";

      fn username<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getUsername",
               jvm_return_type: "Ljava/lang/String;";

      fn acct<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getAcct",
               jvm_return_type: "Ljava/lang/String;";

      fn raw_url<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getRawUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn display_name<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getDisplayName",
               jvm_return_type: "Ljava/lang/String;";

      fn profile_note<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getProfileNote",
               jvm_return_type: "Ljava/lang/String;";

      fn raw_avatar_image_url<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getRawAvatarImageUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn raw_avatar_static_image_url<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getRawAvatarStaticImageUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn raw_header_image_url<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getRawHeaderImageUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn raw_header_static_image_url<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getRawHeaderStaticImageUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn is_locked<'local>(..) -> Option<bool>
         where jvm_type: JvmNullable<'local, JvmBoolean<'local>>,
               jvm_getter_method: "isLocked",
               jvm_return_type: "Ljava/lang/Boolean;";

      fn profile_fields<'local>(..) -> Vec<AccountProfileField>
         where jvm_type: JvmList<'local, JvmAccountProfileField<'local>>,
               jvm_getter_method: "getProfileFields",
               jvm_return_type: "Ljava/util/List;";

      fn emojis_in_profile<'local>(..) -> Vec<CustomEmoji>
         where jvm_type: JvmList<'local, JvmCustomEmoji<'local>>,
               jvm_getter_method: "getEmojisInProfile",
               jvm_return_type: "Ljava/util/List;";

      fn is_bot<'local>(..) -> Option<bool>
         where jvm_type: JvmNullable<'local, JvmBoolean<'local>>,
               jvm_getter_method: "isBot",
               jvm_return_type: "Ljava/lang/Boolean;";

      fn is_group<'local>(..) -> Option<bool>
         where jvm_type: JvmNullable<'local, JvmBoolean<'local>>,
               jvm_getter_method: "isGroup",
               jvm_return_type: "Ljava/lang/Boolean;";

      fn is_discoverable<'local>(..) -> Option<bool>
         where jvm_type: JvmNullable<'local, JvmBoolean<'local>>,
               jvm_getter_method: "isDiscoverable",
               jvm_return_type: "Ljava/lang/Boolean;";

      fn is_noindex<'local>(..) -> Option<bool>
         where jvm_type: JvmNullable<'local, JvmBoolean<'local>>,
               jvm_getter_method: "isNoindex",
               jvm_return_type: "Ljava/lang/Boolean;";

      fn moved_to<'local>(..) -> Option<Cache<Account>>
         where jvm_type: JvmNullable<'local, JvmCache<'local, JvmAccount<'local>>>,
               jvm_getter_method: "getMovedTo",
               jvm_return_type: "Lcom/wcaokaze/probosqis/panoptiqon/Cache;";

      fn is_suspended<'local>(..) -> Option<bool>
         where jvm_type: JvmNullable<'local, JvmBoolean<'local>>,
               jvm_getter_method: "isSuspended",
               jvm_return_type: "Ljava/lang/Boolean;";

      fn is_limited<'local>(..) -> Option<bool>
         where jvm_type: JvmNullable<'local, JvmBoolean<'local>>,
               jvm_getter_method: "isLimited",
               jvm_return_type: "Ljava/lang/Boolean;";

      fn created_time_epoch_millis<'local>(..) -> Option<i64>
         where jvm_type: JvmNullable<'local, JvmLong<'local>>,
               jvm_getter_method: "getCreatedTimeEpochMillis",
               jvm_return_type: "Ljava/lang/Long;";

      fn last_status_post_time_epoch_millis<'local>(..) -> Option<i64>
         where jvm_type: JvmNullable<'local, JvmLong<'local>>,
               jvm_getter_method: "getLastStatusPostTimeEpochMillis",
               jvm_return_type: "Ljava/lang/Long;";

      fn status_count<'local>(..) -> Option<i64>
         where jvm_type: JvmNullable<'local, JvmLong<'local>>,
               jvm_getter_method: "getStatusCount",
               jvm_return_type: "Ljava/lang/Long;";

      fn follower_count<'local>(..) -> Option<i64>
         where jvm_type: JvmNullable<'local, JvmLong<'local>>,
               jvm_getter_method: "getFollowerCount",
               jvm_return_type: "Ljava/lang/Long;";

      fn followee_count<'local>(..) -> Option<i64>
         where jvm_type: JvmNullable<'local, JvmLong<'local>>,
               jvm_getter_method: "getFolloweeCount",
               jvm_return_type: "Ljava/lang/Long;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmAccount<'local>> for Account {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmAccount<'local> {
      ACCOUNT_HELPER.clone_into_jvm(
         env,
         &self.instance,
         self.id.instance_url.as_str(),
         &self.id.local.0,
         &self.username,
         &self.acct,
         &self.url.as_ref().map(Url::as_str),
         &self.display_name,
         &self.profile_note,
         &self.avatar_image_url       .as_ref().map(Url::as_str),
         &self.avatar_static_image_url.as_ref().map(Url::as_str),
         &self.header_image_url       .as_ref().map(Url::as_str),
         &self.header_static_image_url.as_ref().map(Url::as_str),
         &self.is_locked,
         &self.profile_fields,
         &self.emojis_in_profile,
         &self.is_bot,
         &self.is_group,
         &self.is_discoverable,
         &self.is_noindex,
         &self.moved_to,
         &self.is_suspended,
         &self.is_limited,
         &self.created_time         .map(|t| t.timestamp_millis()),
         &self.last_status_post_time.map(|t| t.timestamp_millis()),
         &self.status_count  .map(|u| u as i64),
         &self.follower_count.map(|u| u as i64),
         &self.followee_count.map(|u| u as i64),
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmAccount<'local>> for Account {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmAccount<'local>
   ) -> Account {
      let instance                    = ACCOUNT_HELPER.instance                          (env, jvm_instance);
      let raw_instance_url            = ACCOUNT_HELPER.raw_instance_url                  (env, jvm_instance);
      let raw_local_id                = ACCOUNT_HELPER.raw_local_id                      (env, jvm_instance);
      let username                    = ACCOUNT_HELPER.username                          (env, jvm_instance);
      let acct                        = ACCOUNT_HELPER.acct                              (env, jvm_instance);
      let raw_url                     = ACCOUNT_HELPER.raw_url                           (env, jvm_instance);
      let display_name                = ACCOUNT_HELPER.display_name                      (env, jvm_instance);
      let profile_note                = ACCOUNT_HELPER.profile_note                      (env, jvm_instance);
      let raw_avatar_image_url        = ACCOUNT_HELPER.raw_avatar_image_url              (env, jvm_instance);
      let raw_avatar_static_image_url = ACCOUNT_HELPER.raw_avatar_static_image_url       (env, jvm_instance);
      let raw_header_image_url        = ACCOUNT_HELPER.raw_header_image_url              (env, jvm_instance);
      let raw_header_static_image_url = ACCOUNT_HELPER.raw_header_static_image_url       (env, jvm_instance);
      let is_locked                   = ACCOUNT_HELPER.is_locked                         (env, jvm_instance);
      let profile_fields              = ACCOUNT_HELPER.profile_fields                    (env, jvm_instance);
      let emojis_in_profile           = ACCOUNT_HELPER.emojis_in_profile                 (env, jvm_instance);
      let is_bot                      = ACCOUNT_HELPER.is_bot                            (env, jvm_instance);
      let is_group                    = ACCOUNT_HELPER.is_group                          (env, jvm_instance);
      let is_discoverable             = ACCOUNT_HELPER.is_discoverable                   (env, jvm_instance);
      let is_noindex                  = ACCOUNT_HELPER.is_noindex                        (env, jvm_instance);
      let moved_to                    = ACCOUNT_HELPER.moved_to                          (env, jvm_instance);
      let is_suspended                = ACCOUNT_HELPER.is_suspended                      (env, jvm_instance);
      let is_limited                  = ACCOUNT_HELPER.is_limited                        (env, jvm_instance);
      let created_time                = ACCOUNT_HELPER.created_time_epoch_millis         (env, jvm_instance);
      let last_status_post_time       = ACCOUNT_HELPER.last_status_post_time_epoch_millis(env, jvm_instance);
      let status_count                = ACCOUNT_HELPER.status_count                      (env, jvm_instance);
      let follower_count              = ACCOUNT_HELPER.follower_count                    (env, jvm_instance);
      let followee_count              = ACCOUNT_HELPER.followee_count                    (env, jvm_instance);

      Account {
         instance,
         id: AccountId {
            instance_url: raw_instance_url.parse().unwrap(),
            local: AccountLocalId(raw_local_id),
         },
         username,
         acct,
         url: raw_url.map(|url| url.parse().unwrap()),
         display_name,
         profile_note,
         avatar_image_url:        raw_avatar_image_url       .map(|url| url.parse().unwrap()),
         avatar_static_image_url: raw_avatar_static_image_url.map(|url| url.parse().unwrap()),
         header_image_url:        raw_header_image_url       .map(|url| url.parse().unwrap()),
         header_static_image_url: raw_header_static_image_url.map(|url| url.parse().unwrap()),
         is_locked,
         profile_fields,
         emojis_in_profile,
         is_bot,
         is_group,
         is_discoverable,
         is_noindex,
         moved_to,
         is_suspended,
         is_limited,
         created_time:          created_time         .map(|time| DateTime::from_timestamp_millis(time).unwrap()),
         last_status_post_time: last_status_post_time.map(|time| DateTime::from_timestamp_millis(time).unwrap()),
         status_count:   status_count  .map(|i| i as u64),
         follower_count: follower_count.map(|i| i as u64),
         followee_count: followee_count.map(|i| i as u64),
      }
   }
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static CREDENTIAL_ACCOUNT_HELPER = impl struct CredentialAccountConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/CredentialAccount"
   {
      fn clone_into_jvm<'local>(..) -> JvmCredentialAccount<'local>
         where jvm_constructor: "(\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Lcom/wcaokaze/probosqis/panoptiqon/Cache;\
            Ljava/lang/String;\
            Ljava/util/List;\
            Ljava/lang/String;\
            Ljava/lang/Boolean;\
            Ljava/lang/String;\
            Ljava/lang/Long;\
            Lcom/wcaokaze/probosqis/mastodon/entity/Role;\
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

      fn account<'local>(..) -> Cache<Account>
         where jvm_type: JvmCache<'local, JvmAccount<'local>>,
               jvm_getter_method: "getAccount",
               jvm_return_type: "Lcom/wcaokaze/probosqis/panoptiqon/Cache;";

      fn raw_profile_note<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getRawProfileNote",
               jvm_return_type: "Ljava/lang/String;";

      fn raw_profile_fields<'local>(..) -> Vec<AccountProfileField>
         where jvm_type: JvmList<'local, JvmAccountProfileField<'local>>,
               jvm_getter_method: "getRawProfileFields",
               jvm_return_type: "Ljava/util/List;";

      fn raw_default_post_visibility<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getRawDefaultPostVisibility",
               jvm_return_type: "Ljava/lang/String;";

      fn default_post_sensitivity<'local>(..) -> Option<bool>
         where jvm_type: JvmNullable<'local, JvmBoolean<'local>>,
               jvm_getter_method: "getDefaultPostSensitivity",
               jvm_return_type: "Ljava/lang/Boolean;";

      fn default_post_language<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getDefaultPostLanguage",
               jvm_return_type: "Ljava/lang/String;";

      fn follow_request_count<'local>(..) -> Option<i64>
         where jvm_type: JvmNullable<'local, JvmLong<'local>>,
               jvm_getter_method: "getFollowRequestCount",
               jvm_return_type: "Ljava/lang/Long;";

      fn role<'local>(..) -> Option<Role>
         where jvm_type: JvmNullable<'local, JvmRole<'local>>,
               jvm_getter_method: "getRole",
               jvm_return_type: "Lcom/wcaokaze/probosqis/mastodon/entity/Role;";

      fn dummy<'local>(..) -> Option<()>
         where jvm_type: JvmNullable<'local, JvmUnit<'local>>,
               jvm_getter_method: "getDummy",
               jvm_return_type: "Lkotlin/Unit;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmCredentialAccount<'local>> for CredentialAccount {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmCredentialAccount<'local> {
      CREDENTIAL_ACCOUNT_HELPER.clone_into_jvm(
         env,
         self.id.instance_url.as_str(),
         &self.id.local.0,
         &self.account,
         &self.raw_profile_note,
         &self.raw_profile_fields,
         &self.default_post_visibility.as_ref().map(|v| &v.0),
         &self.default_post_sensitivity,
         &self.default_post_language.map(|l| l.to_639_1().unwrap().to_string()),
         &self.follow_request_count.map(|u| u as i64),
         &self.role,
         &None::<()>,
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmCredentialAccount<'local>> for CredentialAccount {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmCredentialAccount<'local>
   ) -> CredentialAccount {
      let raw_instance_url            = CREDENTIAL_ACCOUNT_HELPER.raw_instance_url           (env, jvm_instance);
      let raw_local_id                = CREDENTIAL_ACCOUNT_HELPER.raw_local_id               (env, jvm_instance);
      let account                     = CREDENTIAL_ACCOUNT_HELPER.account                    (env, jvm_instance);
      let raw_profile_note            = CREDENTIAL_ACCOUNT_HELPER.raw_profile_note           (env, jvm_instance);
      let raw_profile_fields          = CREDENTIAL_ACCOUNT_HELPER.raw_profile_fields         (env, jvm_instance);
      let raw_default_post_visibility = CREDENTIAL_ACCOUNT_HELPER.raw_default_post_visibility(env, jvm_instance);
      let default_post_sensitivity    = CREDENTIAL_ACCOUNT_HELPER.default_post_sensitivity   (env, jvm_instance);
      let default_post_language       = CREDENTIAL_ACCOUNT_HELPER.default_post_language      (env, jvm_instance);
      let follow_request_count        = CREDENTIAL_ACCOUNT_HELPER.follow_request_count       (env, jvm_instance);
      let role                        = CREDENTIAL_ACCOUNT_HELPER.role                       (env, jvm_instance);

      CredentialAccount {
         id: AccountId {
            instance_url: raw_instance_url.parse().unwrap(),
            local: AccountLocalId(raw_local_id),
         },
         account,
         raw_profile_note,
         raw_profile_fields,
         default_post_visibility: raw_default_post_visibility.map(StatusVisibility),
         default_post_sensitivity,
         default_post_language: default_post_language
            .map(|code| Language::from_639_1(&code).unwrap()),
         follow_request_count: follow_request_count.map(|i| i as u64),
         role,
      }
   }
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static RELATIONAL_ACCOUNT_HELPER = impl struct RelationalAccountConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/RelationalAccount"
   {
      fn clone_into_jvm<'local>(..) -> JvmRelationalAccount<'local>
         where jvm_constructor: "(Lcom/wcaokaze/probosqis/panoptiqon/Cache;Ljava/lang/Long;)V";

      fn account<'local>(..) -> Cache<Account>
         where jvm_type: JvmCache<'local, JvmAccount<'local>>,
               jvm_getter_method: "getAccount",
               jvm_return_type: "Lcom/wcaokaze/probosqis/panoptiqon/Cache;";

      fn mute_expire_time_epoch_millis<'local>(..) -> Option<i64>
         where jvm_type: JvmNullable<'local, JvmLong<'local>>,
               jvm_getter_method: "getMuteExpireTimeEpochMillis",
               jvm_return_type: "Ljava/lang/Long;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmRelationalAccount<'local>> for RelationalAccount {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmRelationalAccount<'local> {
      RELATIONAL_ACCOUNT_HELPER.clone_into_jvm(
         env,
         &self.account,
         &self.mute_expire_time.map(|t| t.timestamp_millis()),
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmRelationalAccount<'local>> for RelationalAccount {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmRelationalAccount<'local>
   ) -> RelationalAccount {
      let account          = RELATIONAL_ACCOUNT_HELPER.account(env, jvm_instance);
      let mute_expire_time = RELATIONAL_ACCOUNT_HELPER.mute_expire_time_epoch_millis(env, jvm_instance);

      RelationalAccount {
         account,
         mute_expire_time: mute_expire_time
            .map(|time| DateTime::from_timestamp_millis(time).unwrap()),
      }
   }
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static ACCOUNT_PROFILE_FIELD_HELPER = impl struct AccountProfileFieldConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/Account$ProfileField"
   {
      fn clone_into_jvm<'local>(..) -> JvmAccountProfileField<'local>
         where jvm_constructor: "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Long;)V";

      fn name<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getName",
               jvm_return_type: "Ljava/lang/String;";

      fn value<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getValue",
               jvm_return_type: "Ljava/lang/String;";

      fn verified_time_epoch_millis<'local>(..) -> Option<i64>
         where jvm_type: JvmNullable<'local, JvmLong<'local>>,
               jvm_getter_method: "getVerifiedTimeEpochMillis",
               jvm_return_type: "Ljava/lang/Long;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmAccountProfileField<'local>> for AccountProfileField {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmAccountProfileField<'local> {
      ACCOUNT_PROFILE_FIELD_HELPER.clone_into_jvm(
         env,
         &self.name,
         &self.value,
         &self.verified_time.map(|t| t.timestamp_millis()),
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmAccountProfileField<'local>> for AccountProfileField {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmAccountProfileField<'local>
   ) -> AccountProfileField {
      let name                       = ACCOUNT_PROFILE_FIELD_HELPER.name(env, jvm_instance);
      let value                      = ACCOUNT_PROFILE_FIELD_HELPER.value(env, jvm_instance);
      let verified_time_epoch_millis = ACCOUNT_PROFILE_FIELD_HELPER.verified_time_epoch_millis(env, jvm_instance);

      AccountProfileField {
         name,
         value,
         verified_time: verified_time_epoch_millis
            .map(|time_millis| DateTime::from_timestamp_millis(time_millis).unwrap()),
      }
   }
}
