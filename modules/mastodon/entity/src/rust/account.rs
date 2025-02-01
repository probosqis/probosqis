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
      JvmBoolean, JvmCache, JvmList, JvmLong, JvmNullable, JvmString,
   },
   crate::jvm_types::{
      JvmAccount, JvmAccountProfileField, JvmCredentialAccount, JvmCustomEmoji,
      JvmInstance, JvmRelationalAccount, JvmRole, JvmStatusVisibility,
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
pub struct AccountId(pub String);

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

      fn instance<'local>(..) -> JvmCache<'local, JvmInstance<'local>>
         where jvm_getter_method: "getInstance",
               jvm_return_type: "Lcom/wcaokaze/probosqis/panoptiqon/Cache;";

      fn raw_id<'local>(..) -> JvmString<'local>
         where jvm_getter_method: "getRawId",
               jvm_return_type: "Ljava/lang/String;";

      fn username<'local>(..) -> JvmNullable<'local, JvmString<'local>>
         where jvm_getter_method: "getUsername",
               jvm_return_type: "Ljava/lang/String;";

      fn acct<'local>(..) -> JvmNullable<'local, JvmString<'local>>
         where jvm_getter_method: "getAcct",
               jvm_return_type: "Ljava/lang/String;";

      fn url<'local>(..) -> JvmNullable<'local, JvmString<'local>>
         where jvm_getter_method: "getUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn display_name<'local>(..) -> JvmNullable<'local, JvmString<'local>>
         where jvm_getter_method: "getDisplayName",
               jvm_return_type: "Ljava/lang/String;";

      fn profile_note<'local>(..) -> JvmNullable<'local, JvmString<'local>>
         where jvm_getter_method: "getProfileNote",
               jvm_return_type: "Ljava/lang/String;";

      fn avatar_image_url<'local>(..) -> JvmNullable<'local, JvmString<'local>>
         where jvm_getter_method: "getAvatarImageUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn avatar_static_image_url<'local>(..) -> JvmNullable<'local, JvmString<'local>>
         where jvm_getter_method: "getAvatarStaticImageUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn header_image_url<'local>(..) -> JvmNullable<'local, JvmString<'local>>
         where jvm_getter_method: "getHeaderImageUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn header_static_image_url<'local>(..) -> JvmNullable<'local, JvmString<'local>>
         where jvm_getter_method: "getHeaderStaticImageUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn is_locked<'local>(..) -> JvmNullable<'local, JvmBoolean<'local>>
         where jvm_getter_method: "isLocked",
               jvm_return_type: "Ljava/lang/Boolean;";

      fn profile_fields<'local>(..) -> JvmList<'local, JvmAccountProfileField<'local>>
         where jvm_getter_method: "getProfileFields",
               jvm_return_type: "Ljava/util/List;";

      fn emojis_in_profile<'local>(..) -> JvmList<'local, JvmCustomEmoji<'local>>
         where jvm_getter_method: "getEmojisInProfile",
               jvm_return_type: "Ljava/util/List;";

      fn is_bot<'local>(..) -> JvmNullable<'local, JvmBoolean<'local>>
         where jvm_getter_method: "isBot",
               jvm_return_type: "Ljava/lang/Boolean;";

      fn is_group<'local>(..) -> JvmNullable<'local, JvmBoolean<'local>>
         where jvm_getter_method: "isGroup",
               jvm_return_type: "Ljava/lang/Boolean;";

      fn is_discoverable<'local>(..) -> JvmNullable<'local, JvmBoolean<'local>>
         where jvm_getter_method: "isDiscoverable",
               jvm_return_type: "Ljava/lang/Boolean;";

      fn is_noindex<'local>(..) -> JvmNullable<'local, JvmBoolean<'local>>
         where jvm_getter_method: "isNoindex",
               jvm_return_type: "Ljava/lang/Boolean;";

      fn moved_to<'local>(..) -> JvmNullable<'local, JvmCache<'local, JvmAccount<'local>>>
         where jvm_getter_method: "getMovedTo",
               jvm_return_type: "Lcom/wcaokaze/probosqis/panoptiqon/Cache;";

      fn is_suspended<'local>(..) -> JvmNullable<'local, JvmBoolean<'local>>
         where jvm_getter_method: "isSuspended",
               jvm_return_type: "Ljava/lang/Boolean;";

      fn is_limited<'local>(..) -> JvmNullable<'local, JvmBoolean<'local>>
         where jvm_getter_method: "isLimited",
               jvm_return_type: "Ljava/lang/Boolean;";

      fn created_time_epoch_millis<'local>(..) -> JvmNullable<'local, JvmLong<'local>>
         where jvm_getter_method: "getCreatedTimeEpochMillis",
               jvm_return_type: "Ljava/lang/Long;";

      fn last_status_post_time_epoch_millis<'local>(..) -> JvmNullable<'local, JvmLong<'local>>
         where jvm_getter_method: "getLastStatusPostTimeEpochMillis",
               jvm_return_type: "Ljava/lang/Long;";

      fn status_count<'local>(..) -> JvmNullable<'local, JvmLong<'local>>
         where jvm_getter_method: "getStatusCount",
               jvm_return_type: "Ljava/lang/Long;";

      fn follower_count<'local>(..) -> JvmNullable<'local, JvmLong<'local>>
         where jvm_getter_method: "getFollowerCount",
               jvm_return_type: "Ljava/lang/Long;";

      fn followee_count<'local>(..) -> JvmNullable<'local, JvmLong<'local>>
         where jvm_getter_method: "getFolloweeCount",
               jvm_return_type: "Ljava/lang/Long;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmAccount<'local>> for Account {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmAccount<'local> {
      use panoptiqon::jvm_types::{JvmCache, JvmNullable};
      use crate::jvm_types::JvmInstance;

      let instance: JvmCache<JvmInstance> = self.instance.clone_into_jvm(env);
      let raw_id = self.id.0.clone_into_jvm(env);
      let username = self.username.clone_into_jvm(env);
      let acct = self.acct.clone_into_jvm(env);
      let url = self.url.as_ref().map(|url| url.to_string()).clone_into_jvm(env);
      let display_name = self.display_name.clone_into_jvm(env);
      let profile_note = self.profile_note.clone_into_jvm(env);
      let avatar_image_url = self.avatar_image_url.as_ref().map(|url| url.to_string()).clone_into_jvm(env);
      let avatar_static_image_url = self.avatar_static_image_url.as_ref().map(|url| url.to_string()).clone_into_jvm(env);
      let header_image_url = self.header_image_url.as_ref().map(|url| url.to_string()).clone_into_jvm(env);
      let header_static_image_url = self.header_static_image_url.as_ref().map(|url| url.to_string()).clone_into_jvm(env);
      let is_locked = self.is_locked.clone_into_jvm(env);
      let profile_fields = self.profile_fields.clone_into_jvm(env);
      let emoji_in_profile = self.emojis_in_profile.clone_into_jvm(env);
      let is_bot = self.is_bot.clone_into_jvm(env);
      let is_group = self.is_group.clone_into_jvm(env);
      let is_discoverable = self.is_discoverable.clone_into_jvm(env);
      let is_noindex = self.is_noindex.clone_into_jvm(env);
      let moved_to: JvmNullable<'local, JvmCache<JvmAccount>> = self.moved_to.clone_into_jvm(env);
      let is_suspended = self.is_suspended.clone_into_jvm(env);
      let is_limited = self.is_limited.clone_into_jvm(env);
      let created_time = self.created_time.map(|t| t.timestamp_millis()).clone_into_jvm(env);
      let last_status_post_time = self.last_status_post_time.map(|t| t.timestamp_millis()).clone_into_jvm(env);
      let status_count = self.status_count  .map(|u| u as i64).clone_into_jvm(env);
      let follower_count = self.follower_count.map(|u| u as i64).clone_into_jvm(env);
      let followee_count = self.followee_count.map(|u| u as i64).clone_into_jvm(env);

      ACCOUNT_HELPER.clone_into_jvm(
         env,
         instance,
         raw_id,
         username,
         acct,
         url,
         display_name,
         profile_note,
         avatar_image_url,
         avatar_static_image_url,
         header_image_url,
         header_static_image_url,
         is_locked,
         profile_fields,
         emoji_in_profile,
         is_bot,
         is_group,
         is_discoverable,
         is_noindex,
         moved_to,
         is_suspended,
         is_limited,
         created_time,
         last_status_post_time,
         status_count,
         follower_count,
         followee_count,
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmAccount<'local>> for Account {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmAccount<'local>
   ) -> Account {
      let instance                = ACCOUNT_HELPER.instance                          (env, jvm_instance);
      let id                      = ACCOUNT_HELPER.raw_id                            (env, jvm_instance);
      let username                = ACCOUNT_HELPER.username                          (env, jvm_instance);
      let acct                    = ACCOUNT_HELPER.acct                              (env, jvm_instance);
      let url                     = ACCOUNT_HELPER.url                               (env, jvm_instance);
      let display_name            = ACCOUNT_HELPER.display_name                      (env, jvm_instance);
      let profile_note            = ACCOUNT_HELPER.profile_note                      (env, jvm_instance);
      let avatar_image_url        = ACCOUNT_HELPER.avatar_image_url                  (env, jvm_instance);
      let avatar_static_image_url = ACCOUNT_HELPER.avatar_static_image_url           (env, jvm_instance);
      let header_image_url        = ACCOUNT_HELPER.header_image_url                  (env, jvm_instance);
      let header_static_image_url = ACCOUNT_HELPER.header_static_image_url           (env, jvm_instance);
      let is_locked               = ACCOUNT_HELPER.is_locked                         (env, jvm_instance);
      let profile_fields          = ACCOUNT_HELPER.profile_fields                    (env, jvm_instance);
      let emojis_in_profile       = ACCOUNT_HELPER.emojis_in_profile                 (env, jvm_instance);
      let is_bot                  = ACCOUNT_HELPER.is_bot                            (env, jvm_instance);
      let is_group                = ACCOUNT_HELPER.is_group                          (env, jvm_instance);
      let is_discoverable         = ACCOUNT_HELPER.is_discoverable                   (env, jvm_instance);
      let is_noindex              = ACCOUNT_HELPER.is_noindex                        (env, jvm_instance);
      let moved_to                = ACCOUNT_HELPER.moved_to                          (env, jvm_instance);
      let is_suspended            = ACCOUNT_HELPER.is_suspended                      (env, jvm_instance);
      let is_limited              = ACCOUNT_HELPER.is_limited                        (env, jvm_instance);
      let created_time            = ACCOUNT_HELPER.created_time_epoch_millis         (env, jvm_instance);
      let last_status_post_time   = ACCOUNT_HELPER.last_status_post_time_epoch_millis(env, jvm_instance);
      let status_count            = ACCOUNT_HELPER.status_count                      (env, jvm_instance);
      let follower_count          = ACCOUNT_HELPER.follower_count                    (env, jvm_instance);
      let followee_count          = ACCOUNT_HELPER.followee_count                    (env, jvm_instance);

      let instance                = Cache::<Instance>         ::clone_from_jvm(env, &instance);
      let id                      = String                    ::clone_from_jvm(env, &id);
      let username                = Option::<String>          ::clone_from_jvm(env, &username);
      let acct                    = Option::<String>          ::clone_from_jvm(env, &acct);
      let url                     = Option::<String>          ::clone_from_jvm(env, &url);
      let display_name            = Option::<String>          ::clone_from_jvm(env, &display_name);
      let profile_note            = Option::<String>          ::clone_from_jvm(env, &profile_note);
      let avatar_image_url        = Option::<String>          ::clone_from_jvm(env, &avatar_image_url);
      let avatar_static_image_url = Option::<String>          ::clone_from_jvm(env, &avatar_static_image_url);
      let header_image_url        = Option::<String>          ::clone_from_jvm(env, &header_image_url);
      let header_static_image_url = Option::<String>          ::clone_from_jvm(env, &header_static_image_url);
      let is_locked               = Option::<bool>            ::clone_from_jvm(env, &is_locked);
      let profile_fields          = Vec::<AccountProfileField>::clone_from_jvm(env, &profile_fields);
      let emojis_in_profile       = Vec::<CustomEmoji>        ::clone_from_jvm(env, &emojis_in_profile);
      let is_bot                  = Option::<bool>            ::clone_from_jvm(env, &is_bot);
      let is_group                = Option::<bool>            ::clone_from_jvm(env, &is_group);
      let is_discoverable         = Option::<bool>            ::clone_from_jvm(env, &is_discoverable);
      let is_noindex              = Option::<bool>            ::clone_from_jvm(env, &is_noindex);
      let moved_to                = Option::<Cache<Account>>  ::clone_from_jvm(env, &moved_to);
      let is_suspended            = Option::<bool>            ::clone_from_jvm(env, &is_suspended);
      let is_limited              = Option::<bool>            ::clone_from_jvm(env, &is_limited);
      let created_time            = Option::<i64>             ::clone_from_jvm(env, &created_time);
      let last_status_post_time   = Option::<i64>             ::clone_from_jvm(env, &last_status_post_time);
      let status_count            = Option::<i64>             ::clone_from_jvm(env, &status_count);
      let follower_count          = Option::<i64>             ::clone_from_jvm(env, &follower_count);
      let followee_count          = Option::<i64>             ::clone_from_jvm(env, &followee_count);

      Account {
         instance,
         id: AccountId(id),
         username,
         acct,
         url: url.map(|url| url.parse().unwrap()),
         display_name,
         profile_note,
         avatar_image_url:        avatar_image_url       .map(|url| url.parse().unwrap()),
         avatar_static_image_url: avatar_static_image_url.map(|url| url.parse().unwrap()),
         header_image_url:        header_image_url       .map(|url| url.parse().unwrap()),
         header_static_image_url: header_static_image_url.map(|url| url.parse().unwrap()),
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
            Lcom/wcaokaze/probosqis/panoptiqon/Cache;\
            Ljava/lang/String;\
            Ljava/util/List;\
            Lcom/wcaokaze/probosqis/mastodon/entity/Status$Visibility;\
            Ljava/lang/Boolean;\
            Ljava/lang/String;\
            Ljava/lang/Long;\
            Lcom/wcaokaze/probosqis/mastodon/entity/Role;\
         )V";

      fn account<'local>(..) -> JvmCache<'local, JvmAccount<'local>>
         where jvm_getter_method: "getAccount",
               jvm_return_type: "Lcom/wcaokaze/probosqis/panoptiqon/Cache;";

      fn raw_profile_note<'local>(..) -> JvmNullable<'local, JvmString<'local>>
         where jvm_getter_method: "getRawProfileNote",
               jvm_return_type: "Ljava/lang/String;";

      fn raw_profile_fields<'local>(..) -> JvmList<'local, JvmAccountProfileField<'local>>
         where jvm_getter_method: "getRawProfileFields",
               jvm_return_type: "Ljava/util/List;";

      fn default_post_visibility<'local>(..) -> JvmNullable<'local, JvmStatusVisibility<'local>>
         where jvm_getter_method: "getDefaultPostVisibility",
               jvm_return_type: "Lcom/wcaokaze/probosqis/mastodon/entity/Status$Visibility;";

      fn default_post_sensitivity<'local>(..) -> JvmNullable<'local, JvmBoolean<'local>>
         where jvm_getter_method: "getDefaultPostSensitivity",
               jvm_return_type: "Ljava/util/Boolean;";

      fn default_post_language<'local>(..) -> JvmNullable<'local, JvmString<'local>>
         where jvm_getter_method: "getDefaultPostLanguage",
               jvm_return_type: "Ljava/lang/String;";

      fn follow_request_count<'local>(..) -> JvmNullable<'local, JvmLong<'local>>
         where jvm_getter_method: "getFollowRequestCount",
               jvm_return_type: "Ljava/lang/Long;";

      fn role<'local>(..) -> JvmNullable<'local, JvmRole<'local>>
         where jvm_getter_method: "getRole",
               jvm_return_type: "Lcom/wcaokaze/probosqis/mastodon/entity/Role;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmCredentialAccount<'local>> for CredentialAccount {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmCredentialAccount<'local> {
      use panoptiqon::jvm_types::JvmCache;

      let account: JvmCache<JvmAccount> = self.account                                                         .clone_into_jvm(env);
      let raw_profile_note              = self.raw_profile_note                                                .clone_into_jvm(env);
      let raw_profile_fields            = self.raw_profile_fields                                              .clone_into_jvm(env);
      let default_post_visibility       = self.default_post_visibility                                         .clone_into_jvm(env);
      let default_post_sensitivity      = self.default_post_sensitivity                                        .clone_into_jvm(env);
      let default_post_language         = self.default_post_language.map(|l| l.to_639_1().unwrap().to_string()).clone_into_jvm(env);
      let follow_request_count          = self.follow_request_count.map(|u| u as i64)                          .clone_into_jvm(env);
      let role                          = self.role                                                            .clone_into_jvm(env);

      CREDENTIAL_ACCOUNT_HELPER.clone_into_jvm(
         env,
         account,
         raw_profile_note,
         raw_profile_fields,
         default_post_visibility,
         default_post_sensitivity,
         default_post_language,
         follow_request_count,
         role,
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmCredentialAccount<'local>> for CredentialAccount {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmCredentialAccount<'local>
   ) -> CredentialAccount {
      let account                  = CREDENTIAL_ACCOUNT_HELPER.account                 (env, jvm_instance);
      let raw_profile_note         = CREDENTIAL_ACCOUNT_HELPER.raw_profile_note        (env, jvm_instance);
      let raw_profile_fields       = CREDENTIAL_ACCOUNT_HELPER.raw_profile_fields      (env, jvm_instance);
      let default_post_visibility  = CREDENTIAL_ACCOUNT_HELPER.default_post_visibility (env, jvm_instance);
      let default_post_sensitivity = CREDENTIAL_ACCOUNT_HELPER.default_post_sensitivity(env, jvm_instance);
      let default_post_language    = CREDENTIAL_ACCOUNT_HELPER.default_post_language   (env, jvm_instance);
      let follow_request_count     = CREDENTIAL_ACCOUNT_HELPER.follow_request_count    (env, jvm_instance);
      let role                     = CREDENTIAL_ACCOUNT_HELPER.role                    (env, jvm_instance);

      CredentialAccount {
         account:                  Cache ::<Account>            ::clone_from_jvm(env, &account),
         raw_profile_note:         Option::<String>             ::clone_from_jvm(env, &raw_profile_note),
         raw_profile_fields:       Vec   ::<AccountProfileField>::clone_from_jvm(env, &raw_profile_fields),
         default_post_visibility:  Option::<StatusVisibility>   ::clone_from_jvm(env, &default_post_visibility),
         default_post_sensitivity: Option::<bool>               ::clone_from_jvm(env, &default_post_sensitivity),
         default_post_language:    Option::<String>             ::clone_from_jvm(env, &default_post_language).map(|code| Language::from_639_1(&code).unwrap()),
         follow_request_count:     Option::<i64>                ::clone_from_jvm(env, &follow_request_count).map(|i| i as u64),
         role:                     Option::<Role>               ::clone_from_jvm(env, &role),
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

      fn account<'local>(..) -> JvmCache<'local, JvmAccount<'local>>
         where jvm_getter_method: "getAccount",
               jvm_return_type: "Lcom/wcaokaze/probosqis/panoptiqon/Cache;";

      fn mute_expire_time_epoch_millis<'local>(..) -> JvmNullable<'local, JvmLong<'local>>
         where jvm_getter_method: "getMuteExpireTimeEpochMillis",
               jvm_return_type: "Ljava/lang/Long;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmRelationalAccount<'local>> for RelationalAccount {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmRelationalAccount<'local> {
      use panoptiqon::jvm_types::JvmCache;

      let account: JvmCache<JvmAccount> = self.account.clone_into_jvm(env);
      let mute_expire_time = self.mute_expire_time.map(|t| t.timestamp_millis()).clone_into_jvm(env);

      RELATIONAL_ACCOUNT_HELPER.clone_into_jvm(
         env,
         account,
         mute_expire_time,
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
         account:          Cache::<Account>::clone_from_jvm(env, &account),
         mute_expire_time: Option::<i64>   ::clone_from_jvm(env, &mute_expire_time).map(|time| DateTime::from_timestamp_millis(time).unwrap()),
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

      fn name<'local>(..) -> JvmNullable<'local, JvmString<'local>>
         where jvm_getter_method: "getName",
               jvm_return_type: "Ljava/lang/String;";

      fn value<'local>(..) -> JvmNullable<'local, JvmString<'local>>
         where jvm_getter_method: "getValue",
               jvm_return_type: "Ljava/lang/String;";

      fn verified_time_epoch_millis<'local>(..) -> JvmNullable<'local, JvmLong<'local>>
         where jvm_getter_method: "getVerifiedTimeEpochMillis",
               jvm_return_type: "Ljava/lang/Long;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmAccountProfileField<'local>> for AccountProfileField {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmAccountProfileField<'local> {
      let name = self.name.clone_into_jvm(env);
      let value = self.value.clone_into_jvm(env);
      let verified_time = self.verified_time.map(|t| t.timestamp_millis()).clone_into_jvm(env);

      ACCOUNT_PROFILE_FIELD_HELPER.clone_into_jvm(
         env,
         name,
         value,
         verified_time,
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
         name: Option::<String>::clone_from_jvm(env, &name),
         value: Option::<String>::clone_from_jvm(env, &value),
         verified_time: Option::<i64>::clone_from_jvm(env, &verified_time_epoch_millis)
            .map(|time_millis| DateTime::from_timestamp_millis(time_millis).unwrap()),
      }
   }
}
