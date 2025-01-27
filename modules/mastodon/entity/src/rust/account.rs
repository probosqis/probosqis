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
   ext_panoptiqon::convert_jvm_helper::{ConvertJniHelper, JvmInstantiationStrategy},
   jni::JNIEnv,
   panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm},
   crate::jvm_types::{
      JvmAccount, JvmAccountProfileField, JvmCredentialAccount, JvmRelationalAccount
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
static ACCOUNT_HELPER: ConvertJniHelper<26> = ConvertJniHelper::new(
   "com/wcaokaze/probosqis/mastodon/entity/Account",
   JvmInstantiationStrategy::ViaConstructor(
      "(Lcom/wcaokaze/probosqis/panoptiqon/Cache;\
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
      Ljava/lang/Long;)V"
   ),
   [
      ("getInstance", "Lcom/wcaokaze/probosqis/panoptiqon/Cache;"),
      ("getRawId", "Ljava/lang/String;"),
      ("getUsername", "Ljava/lang/String;"),
      ("getAcct", "Ljava/lang/String;"),
      ("getUrl", "Ljava/lang/String;"),
      ("getDisplayName", "Ljava/lang/String;"),
      ("getProfileNote", "Ljava/lang/String;"),
      ("getAvatarImageUrl", "Ljava/lang/String;"),
      ("getAvatarStaticImageUrl", "Ljava/lang/String;"),
      ("getHeaderImageUrl", "Ljava/lang/String;"),
      ("getHeaderStaticImageUrl", "Ljava/lang/String;"),
      ("isLocked", "Ljava/lang/Boolean;"),
      ("getProfileFields", "Ljava/util/List;"),
      ("getEmojisInProfile", "Ljava/util/List;"),
      ("isBot", "Ljava/lang/Boolean;"),
      ("isGroup", "Ljava/lang/Boolean;"),
      ("isDiscoverable", "Ljava/lang/Boolean;"),
      ("isNoindex", "Ljava/lang/Boolean;"),
      ("getMovedTo", "Lcom/wcaokaze/probosqis/panoptiqon/Cache;"),
      ("isSuspended", "Ljava/lang/Boolean;"),
      ("isLimited", "Ljava/lang/Boolean;"),
      ("getCreatedTimeEpochMillis", "Ljava/lang/Long;"),
      ("getLastStatusPostTimeEpochMillis", "Ljava/lang/Long;"),
      ("getStatusCount", "Ljava/lang/Long;"),
      ("getFollowerCount", "Ljava/lang/Long;"),
      ("getFolloweeCount", "Ljava/lang/Long;"),
   ]
);

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmAccount<'local>> for Account {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmAccount<'local> {
      use jni::sys::jvalue;
      use panoptiqon::jvm_type::JvmType;
      use panoptiqon::jvm_types::{JvmCache, JvmNullable};
      use crate::jvm_types::JvmInstance;

      let instance: JvmCache<JvmInstance> = self.instance.clone_into_jvm(env);
      let id = self.id.0.clone_into_jvm(env);
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
      let moved_to: JvmNullable<JvmCache<JvmAccount>> = self.moved_to.clone_into_jvm(env);
      let is_suspended = self.is_suspended.clone_into_jvm(env);
      let is_limited = self.is_limited.clone_into_jvm(env);
      let created_time = self.created_time.map(|t| t.timestamp_millis()).clone_into_jvm(env);
      let last_status_post_time = self.last_status_post_time.map(|t| t.timestamp_millis()).clone_into_jvm(env);
      let status_count = self.status_count  .map(|u| u as i64).clone_into_jvm(env);
      let follower_count = self.follower_count.map(|u| u as i64).clone_into_jvm(env);
      let followee_count = self.followee_count.map(|u| u as i64).clone_into_jvm(env);

      let args = [
         jvalue { l: instance               .j_object().as_raw() },
         jvalue { l: id                     .j_string().as_raw() },
         jvalue { l: username               .j_object().as_raw() },
         jvalue { l: acct                   .j_object().as_raw() },
         jvalue { l: url                    .j_object().as_raw() },
         jvalue { l: display_name           .j_object().as_raw() },
         jvalue { l: profile_note           .j_object().as_raw() },
         jvalue { l: avatar_image_url       .j_object().as_raw() },
         jvalue { l: avatar_static_image_url.j_object().as_raw() },
         jvalue { l: header_image_url       .j_object().as_raw() },
         jvalue { l: header_static_image_url.j_object().as_raw() },
         jvalue { l: is_locked              .j_object().as_raw() },
         jvalue { l: profile_fields         .j_object().as_raw() },
         jvalue { l: emoji_in_profile       .j_object().as_raw() },
         jvalue { l: is_bot                 .j_object().as_raw() },
         jvalue { l: is_group               .j_object().as_raw() },
         jvalue { l: is_discoverable        .j_object().as_raw() },
         jvalue { l: is_noindex             .j_object().as_raw() },
         jvalue { l: moved_to               .j_object().as_raw() },
         jvalue { l: is_suspended           .j_object().as_raw() },
         jvalue { l: is_limited             .j_object().as_raw() },
         jvalue { l: created_time           .j_object().as_raw() },
         jvalue { l: last_status_post_time  .j_object().as_raw() },
         jvalue { l: status_count           .j_object().as_raw() },
         jvalue { l: follower_count         .j_object().as_raw() },
         jvalue { l: followee_count         .j_object().as_raw() },
      ];

      let j_object = ACCOUNT_HELPER.clone_into_jvm(env, &args);
      unsafe { JvmAccount::from_j_object(j_object) }
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmAccount<'local>> for Account {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmAccount<'local>
   ) -> Account {
      use panoptiqon::jvm_type::JvmType;
      use panoptiqon::jvm_types::{
         JvmBoolean, JvmCache, JvmList, JvmLong, JvmNullable, JvmString,
      };
      use crate::jvm_types::{JvmCustomEmoji, JvmInstance};

      let instance                = ACCOUNT_HELPER.get(env, jvm_instance.j_object(),  0).l().unwrap();
      let id                      = ACCOUNT_HELPER.get(env, jvm_instance.j_object(),  1).l().unwrap();
      let username                = ACCOUNT_HELPER.get(env, jvm_instance.j_object(),  2).l().unwrap();
      let acct                    = ACCOUNT_HELPER.get(env, jvm_instance.j_object(),  3).l().unwrap();
      let url                     = ACCOUNT_HELPER.get(env, jvm_instance.j_object(),  4).l().unwrap();
      let display_name            = ACCOUNT_HELPER.get(env, jvm_instance.j_object(),  5).l().unwrap();
      let profile_note            = ACCOUNT_HELPER.get(env, jvm_instance.j_object(),  6).l().unwrap();
      let avatar_image_url        = ACCOUNT_HELPER.get(env, jvm_instance.j_object(),  7).l().unwrap();
      let avatar_static_image_url = ACCOUNT_HELPER.get(env, jvm_instance.j_object(),  8).l().unwrap();
      let header_image_url        = ACCOUNT_HELPER.get(env, jvm_instance.j_object(),  9).l().unwrap();
      let header_static_image_url = ACCOUNT_HELPER.get(env, jvm_instance.j_object(), 10).l().unwrap();
      let is_locked               = ACCOUNT_HELPER.get(env, jvm_instance.j_object(), 11).l().unwrap();
      let profile_fields          = ACCOUNT_HELPER.get(env, jvm_instance.j_object(), 12).l().unwrap();
      let emojis_in_profile       = ACCOUNT_HELPER.get(env, jvm_instance.j_object(), 13).l().unwrap();
      let is_bot                  = ACCOUNT_HELPER.get(env, jvm_instance.j_object(), 14).l().unwrap();
      let is_group                = ACCOUNT_HELPER.get(env, jvm_instance.j_object(), 15).l().unwrap();
      let is_discoverable         = ACCOUNT_HELPER.get(env, jvm_instance.j_object(), 16).l().unwrap();
      let is_noindex              = ACCOUNT_HELPER.get(env, jvm_instance.j_object(), 17).l().unwrap();
      let moved_to                = ACCOUNT_HELPER.get(env, jvm_instance.j_object(), 18).l().unwrap();
      let is_suspended            = ACCOUNT_HELPER.get(env, jvm_instance.j_object(), 19).l().unwrap();
      let is_limited              = ACCOUNT_HELPER.get(env, jvm_instance.j_object(), 20).l().unwrap();
      let created_time            = ACCOUNT_HELPER.get(env, jvm_instance.j_object(), 21).l().unwrap();
      let last_status_post_time   = ACCOUNT_HELPER.get(env, jvm_instance.j_object(), 22).l().unwrap();
      let status_count            = ACCOUNT_HELPER.get(env, jvm_instance.j_object(), 23).l().unwrap();
      let follower_count          = ACCOUNT_HELPER.get(env, jvm_instance.j_object(), 24).l().unwrap();
      let followee_count          = ACCOUNT_HELPER.get(env, jvm_instance.j_object(), 25).l().unwrap();

      let instance                = unsafe { JvmCache::<JvmInstance>            ::from_j_object(instance)                };
      let id                      = unsafe { JvmString                          ::from_j_object(id)                      };
      let username                = unsafe { JvmNullable::<JvmString>           ::from_j_object(username)                };
      let acct                    = unsafe { JvmNullable::<JvmString>           ::from_j_object(acct)                    };
      let url                     = unsafe { JvmNullable::<JvmString>           ::from_j_object(url)                     };
      let display_name            = unsafe { JvmNullable::<JvmString>           ::from_j_object(display_name)            };
      let profile_note            = unsafe { JvmNullable::<JvmString>           ::from_j_object(profile_note)            };
      let avatar_image_url        = unsafe { JvmNullable::<JvmString>           ::from_j_object(avatar_image_url)        };
      let avatar_static_image_url = unsafe { JvmNullable::<JvmString>           ::from_j_object(avatar_static_image_url) };
      let header_image_url        = unsafe { JvmNullable::<JvmString>           ::from_j_object(header_image_url)        };
      let header_static_image_url = unsafe { JvmNullable::<JvmString>           ::from_j_object(header_static_image_url) };
      let is_locked               = unsafe { JvmNullable::<JvmBoolean>          ::from_j_object(is_locked)               };
      let profile_fields          = unsafe { JvmList::<JvmAccountProfileField>  ::from_j_object(profile_fields)          };
      let emojis_in_profile       = unsafe { JvmList::<JvmCustomEmoji>          ::from_j_object(emojis_in_profile)       };
      let is_bot                  = unsafe { JvmNullable::<JvmBoolean>          ::from_j_object(is_bot)                  };
      let is_group                = unsafe { JvmNullable::<JvmBoolean>          ::from_j_object(is_group)                };
      let is_discoverable         = unsafe { JvmNullable::<JvmBoolean>          ::from_j_object(is_discoverable)         };
      let is_noindex              = unsafe { JvmNullable::<JvmBoolean>          ::from_j_object(is_noindex)              };
      let moved_to                = unsafe { JvmNullable::<JvmCache<JvmAccount>>::from_j_object(moved_to  )              };
      let is_suspended            = unsafe { JvmNullable::<JvmBoolean>          ::from_j_object(is_suspended)            };
      let is_limited              = unsafe { JvmNullable::<JvmBoolean>          ::from_j_object(is_limited)              };
      let created_time            = unsafe { JvmNullable::<JvmLong>             ::from_j_object(created_time)            };
      let last_status_post_time   = unsafe { JvmNullable::<JvmLong>             ::from_j_object(last_status_post_time)   };
      let status_count            = unsafe { JvmNullable::<JvmLong>             ::from_j_object(status_count)            };
      let follower_count          = unsafe { JvmNullable::<JvmLong>             ::from_j_object(follower_count)          };
      let followee_count          = unsafe { JvmNullable::<JvmLong>             ::from_j_object(followee_count)          };

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
static CREDENTIAL_ACCOUNT_HELPER: ConvertJniHelper<8> = ConvertJniHelper::new(
   "com/wcaokaze/probosqis/mastodon/entity/CredentialAccount",
   JvmInstantiationStrategy::ViaConstructor(
      "(Lcom/wcaokaze/probosqis/panoptiqon/Cache;\
      Ljava/lang/String;\
      Ljava/util/List;\
      Lcom/wcaokaze/probosqis/mastodon/entity/Status$Visibility;\
      Ljava/lang/Boolean;\
      Ljava/lang/String;\
      Ljava/lang/Long;\
      Lcom/wcaokaze/probosqis/mastodon/entity/Role;)V"
   ),
   [
      ("getAccount",                "Lcom/wcaokaze/probosqis/panoptiqon/Cache;"),
      ("getRawProfileNote",         "Ljava/lang/String;"),
      ("getRawProfileFields",       "Ljava/util/List;"),
      ("getDefaultPostVisibility",  "Lcom/wcaokaze/probosqis/mastodon/entity/Status$Visibility;"),
      ("getDefaultPostSensitivity", "Ljava/util/Boolean;"),
      ("getDefaultPostLanguage",    "Ljava/lang/String;"),
      ("getFollowRequestCount",     "Ljava/lang/Long;"),
      ("getRole",                   "Lcom/wcaokaze/probosqis/mastodon/entity/Role;"),
   ]
);

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmCredentialAccount<'local>> for CredentialAccount {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmCredentialAccount<'local> {
      use jni::sys::jvalue;
      use panoptiqon::jvm_type::JvmType;
      use panoptiqon::jvm_types::JvmCache;

      let account: JvmCache<JvmAccount> = self.account                                                         .clone_into_jvm(env);
      let raw_profile_note              = self.raw_profile_note                                                .clone_into_jvm(env);
      let raw_profile_fields            = self.raw_profile_fields                                              .clone_into_jvm(env);
      let default_post_visibility       = self.default_post_visibility                                         .clone_into_jvm(env);
      let default_post_sensitivity      = self.default_post_sensitivity                                        .clone_into_jvm(env);
      let default_post_language         = self.default_post_language.map(|l| l.to_639_1().unwrap().to_string()).clone_into_jvm(env);
      let follow_request_count          = self.follow_request_count.map(|u| u as i64)                          .clone_into_jvm(env);
      let role                          = self.role                                                            .clone_into_jvm(env);

      let args = [
         jvalue { l: account.j_object().as_raw() },
         jvalue { l: raw_profile_note.j_object().as_raw() },
         jvalue { l: raw_profile_fields.j_object().as_raw() },
         jvalue { l: default_post_visibility.j_object().as_raw() },
         jvalue { l: default_post_sensitivity.j_object().as_raw() },
         jvalue { l: default_post_language.j_object().as_raw() },
         jvalue { l: follow_request_count.j_object().as_raw() },
         jvalue { l: role.j_object().as_raw() },
      ];

      let j_object = CREDENTIAL_ACCOUNT_HELPER.clone_into_jvm(env, &args);
      unsafe { JvmCredentialAccount::from_j_object(j_object) }
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmCredentialAccount<'local>> for CredentialAccount {
   fn clone_from_jvm(
      env: &mut JNIEnv,
      jvm_instance: &JvmCredentialAccount<'local>
   ) -> CredentialAccount {
      use panoptiqon::jvm_type::JvmType;
      use panoptiqon::jvm_types::{
         JvmBoolean, JvmCache, JvmList, JvmLong, JvmNullable, JvmString,
      };
      use crate::jvm_types::{JvmRole, JvmStatusVisibility};

      let account                  = CREDENTIAL_ACCOUNT_HELPER.get(env, jvm_instance.j_object(), 0).l().unwrap();
      let raw_profile_note         = CREDENTIAL_ACCOUNT_HELPER.get(env, jvm_instance.j_object(), 1).l().unwrap();
      let raw_profile_fields       = CREDENTIAL_ACCOUNT_HELPER.get(env, jvm_instance.j_object(), 2).l().unwrap();
      let default_post_visibility  = CREDENTIAL_ACCOUNT_HELPER.get(env, jvm_instance.j_object(), 3).l().unwrap();
      let default_post_sensitivity = CREDENTIAL_ACCOUNT_HELPER.get(env, jvm_instance.j_object(), 4).l().unwrap();
      let default_post_language    = CREDENTIAL_ACCOUNT_HELPER.get(env, jvm_instance.j_object(), 5).l().unwrap();
      let follow_request_count     = CREDENTIAL_ACCOUNT_HELPER.get(env, jvm_instance.j_object(), 6).l().unwrap();
      let role                     = CREDENTIAL_ACCOUNT_HELPER.get(env, jvm_instance.j_object(), 7).l().unwrap();

      let account                  = unsafe { JvmCache   ::<JvmAccount>            ::from_j_object(account)                  };
      let raw_profile_note         = unsafe { JvmNullable::<JvmString>             ::from_j_object(raw_profile_note)         };
      let raw_profile_fields       = unsafe { JvmList    ::<JvmAccountProfileField>::from_j_object(raw_profile_fields)       };
      let default_post_visibility  = unsafe { JvmNullable::<JvmStatusVisibility>   ::from_j_object(default_post_visibility)  };
      let default_post_sensitivity = unsafe { JvmNullable::<JvmBoolean>            ::from_j_object(default_post_sensitivity) };
      let default_post_language    = unsafe { JvmNullable::<JvmString>             ::from_j_object(default_post_language)    };
      let follow_request_count     = unsafe { JvmNullable::<JvmLong>               ::from_j_object(follow_request_count)     };
      let role                     = unsafe { JvmNullable::<JvmRole>               ::from_j_object(role)                     };

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
static RELATIONAL_ACCOUNT_HELPER: ConvertJniHelper<2> = ConvertJniHelper::new(
   "com/wcaokaze/probosqis/mastodon/entity/RelationalAccount",
   JvmInstantiationStrategy::ViaConstructor(
      "(Lcom/wcaokaze/probosqis/panoptiqon/Cache;Ljava/lang/Long;)V"
   ),
   [
      ("getAccount",                   "Lcom/wcaokaze/probosqis/panoptiqon/Cache;"),
      ("getMuteExpireTimeEpochMillis", "Ljava/lang/Long;"),
   ]
);

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmRelationalAccount<'local>> for RelationalAccount {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmRelationalAccount<'local> {
      use jni::sys::jvalue;
      use panoptiqon::jvm_type::JvmType;
      use panoptiqon::jvm_types::JvmCache;

      let account: JvmCache<JvmAccount> = self.account.clone_into_jvm(env);
      let mute_expire_time = self.mute_expire_time.map(|t| t.timestamp_millis()).clone_into_jvm(env);

      let args = [
         jvalue { l: account.j_object().as_raw() },
         jvalue { l: mute_expire_time.j_object().as_raw() },
      ];

      let j_object = RELATIONAL_ACCOUNT_HELPER.clone_into_jvm(env, &args);
      unsafe { JvmRelationalAccount::from_j_object(j_object) }
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmRelationalAccount<'local>> for RelationalAccount {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmRelationalAccount<'local>
   ) -> RelationalAccount {
      use panoptiqon::jvm_type::JvmType;
      use panoptiqon::jvm_types::{JvmCache, JvmLong, JvmNullable};

      let account          = RELATIONAL_ACCOUNT_HELPER.get(env, jvm_instance.j_object(), 0).l().unwrap();
      let mute_expire_time = RELATIONAL_ACCOUNT_HELPER.get(env, jvm_instance.j_object(), 1).l().unwrap();

      let account          = unsafe { JvmCache::<JvmAccount>::from_j_object(account)          };
      let mute_expire_time = unsafe { JvmNullable::<JvmLong>::from_j_object(mute_expire_time) };

      RelationalAccount {
         account:          Cache::<Account>::clone_from_jvm(env, &account),
         mute_expire_time: Option::<i64>   ::clone_from_jvm(env, &mute_expire_time).map(|time| DateTime::from_timestamp_millis(time).unwrap()),
      }
   }
}

#[cfg(feature = "jvm")]
static ACCOUNT_PROFILE_FIELD_HELPER: ConvertJniHelper<3> = ConvertJniHelper::new(
   "com/wcaokaze/probosqis/mastodon/entity/Account$ProfileField",
   JvmInstantiationStrategy::ViaConstructor(
      "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Long;)V"
   ),
   [
      ("getName",                    "Ljava/lang/String;"),
      ("getValue",                   "Ljava/lang/String;"),
      ("getVerifiedTimeEpochMillis", "Ljava/lang/Long;"),
   ]
);

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmAccountProfileField<'local>> for AccountProfileField {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmAccountProfileField<'local> {
      use jni::sys::jvalue;
      use panoptiqon::jvm_type::JvmType;

      let name = self.name.clone_into_jvm(env);
      let value = self.value.clone_into_jvm(env);
      let verified_time = self.verified_time.map(|t| t.timestamp_millis()).clone_into_jvm(env);

      let args = [
         jvalue { l: name.j_object().as_raw() },
         jvalue { l: value.j_object().as_raw() },
         jvalue { l: verified_time.j_object().as_raw() },
      ];

      let j_object = ACCOUNT_PROFILE_FIELD_HELPER.clone_into_jvm(env, &args);
      unsafe { JvmAccountProfileField::from_j_object(j_object) }
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmAccountProfileField<'local>> for AccountProfileField {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmAccountProfileField<'local>
   ) -> AccountProfileField {
      use panoptiqon::jvm_type::JvmType;
      use panoptiqon::jvm_types::{JvmLong, JvmNullable, JvmString};

      let name                       = ACCOUNT_PROFILE_FIELD_HELPER.get(env, jvm_instance.j_object(), 0).l().unwrap();
      let value                      = ACCOUNT_PROFILE_FIELD_HELPER.get(env, jvm_instance.j_object(), 1).l().unwrap();
      let verified_time_epoch_millis = ACCOUNT_PROFILE_FIELD_HELPER.get(env, jvm_instance.j_object(), 2).l().unwrap();

      let name = unsafe { JvmNullable::<JvmString>::from_j_object(name) };
      let value = unsafe { JvmNullable::<JvmString>::from_j_object(value) };
      let verified_time_epoch_millis = unsafe { JvmNullable::<JvmLong>::from_j_object(verified_time_epoch_millis) };

      AccountProfileField {
         name: Option::<String>::clone_from_jvm(env, &name),
         value: Option::<String>::clone_from_jvm(env, &value),
         verified_time: Option::<i64>::clone_from_jvm(env, &verified_time_epoch_millis)
            .map(|time_millis| DateTime::from_timestamp_millis(time_millis).unwrap()),
      }
   }
}
