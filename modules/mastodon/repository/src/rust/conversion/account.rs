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
use mastodon_entity::account::{Account, AccountProfileField, CredentialAccount};
use mastodon_entity::instance::Instance;
use mastodon_webapi::entity::account::{Account as ApiAccount, AccountField};
use panoptiqon::cache::Cache;
use crate::cache;

#[cfg(feature="jvm")]
use jni::JNIEnv;

pub fn from_api(
   #[cfg(feature="jvm")] env: &mut JNIEnv,
   instance: Cache<Instance>,
   entity: ApiAccount,
   account_cache_repository: &mut cache::account::Repository
) -> anyhow::Result<Account> {
   use anyhow::Context;
   use chrono::DateTime;
   use mastodon_entity::account::{AccountId, AccountLocalId};
   use crate::conversion::custom_emoji;

   let ApiAccount {
      id, username, acct, url, display_name, note, avatar, avatar_static, header,
      header_static, locked, fields, emojis, bot, group, discoverable, noindex,
      moved, suspended, limited, created_at, last_status_at, statuses_count,
      followers_count, following_count, ..
   } = entity;

   let account = Account {
      instance: instance.clone(),
      id: AccountId {
         instance_url: instance.get().url.clone(),
         local: AccountLocalId(id.context("No account id")?)
      },
      username,
      acct,
      url: url.and_then(|url| url.parse().ok()),
      display_name,
      profile_note: note,
      avatar_image_url: avatar.and_then(|url| url.parse().ok()),
      avatar_static_image_url: avatar_static.and_then(|url| url.parse().ok()),
      header_image_url: header.and_then(|url| url.parse().ok()),
      header_static_image_url: header_static.and_then(|url| url.parse().ok()),
      is_locked: locked,
      profile_fields: fields.into_iter()
         .flatten()
         .flat_map(|f| profile_field_from_api(f))
         .collect(),
      emojis_in_profile: emojis.into_iter()
         .flatten()
         .flat_map(|e| custom_emoji::from_api(instance.clone(), e))
         .collect(),
      is_bot: bot,
      is_group: group,
      is_discoverable: discoverable,
      is_noindex: noindex,
      moved_to: moved
         .and_then(|moved: Box<ApiAccount>| -> Option<_> {
            let moved = from_api(
               #[cfg(feature="jvm")] env,
               instance.clone(),
               *moved,
               account_cache_repository
            ).ok()?;

            let moved = account_cache_repository.save(moved);
            Some(moved)
         }),
      is_suspended: suspended,
      is_limited: limited,
      created_time: created_at
         .and_then(|time| DateTime::parse_from_rfc3339(&time).ok())
         .map(|time| time.to_utc()),
      last_status_post_time: last_status_at
         .and_then(|time| DateTime::parse_from_rfc3339(&time).ok())
         .map(|time| time.to_utc()),
      status_count: statuses_count,
      follower_count: followers_count,
      followee_count: following_count,
   };

   Ok(account)
}

pub fn credential_account_from_api(
   #[cfg(feature="jvm")] env: &mut JNIEnv,
   instance: Cache<Instance>,
   mut entity: ApiAccount
) -> anyhow::Result<CredentialAccount> {
   use isolang::Language;
   use mastodon_entity::status::StatusVisibility;
   use crate::conversion::role;

   let source = entity.source.take();

   let raw_profile_note;
   let raw_profile_fields;
   let default_post_visibility;
   let default_post_sensitivity;
   let default_post_language;
   let follow_request_count;

   if let Some(source) = source {
      raw_profile_note = source.note;
      raw_profile_fields = source.fields.into_iter()
         .flatten()
         .flat_map(|f| profile_field_from_api(f))
         .collect();
      default_post_visibility = source.privacy.map(StatusVisibility);
      default_post_sensitivity = source.sensitive;
      default_post_language
         = source.language.and_then(|code| Language::from_639_1(&code));
      follow_request_count = source.follow_requests_count;
   } else {
      raw_profile_note = None;
      raw_profile_fields = vec![];
      default_post_visibility = None;
      default_post_sensitivity = None;
      default_post_language = None;
      follow_request_count = None;
   }

   let role = entity.role.take();
   let role = role.and_then(|role| role::from_api(instance.clone(), role).ok());

   let mut account_cache_repo = cache::account::repo()
      .write(#[cfg(feature="jvm")] env)?;

   let account = from_api(#[cfg(feature="jvm")] env, instance, entity, &mut account_cache_repo)?;
   let account = account_cache_repo.save(account);

   let credential_account = CredentialAccount {
      account,
      raw_profile_note,
      raw_profile_fields,
      default_post_visibility,
      default_post_sensitivity,
      default_post_language,
      follow_request_count,
      role,
   };

   Ok(credential_account)
}

pub fn profile_field_from_api(
   entity: AccountField
) -> anyhow::Result<AccountProfileField> {
   use chrono::DateTime;

   let AccountField { name, value, verified_at } = entity;

   let profile_field = AccountProfileField {
      name,
      value,
      verified_time: verified_at
         .and_then(|time| DateTime::parse_from_rfc3339(&time).ok())
         .map(|time| time.to_utc())
   };

   Ok(profile_field)
}
