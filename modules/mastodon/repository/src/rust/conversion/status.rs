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

use mastodon_entity::instance::Instance;
use mastodon_entity::status::{Status, StatusHashtag, StatusMention};
use panoptiqon::cache::Cache;
use crate::cache;

use mastodon_webapi::entity::status::{
   Status as ApiStatus,
   StatusMention as ApiStatusMention,
   StatusTag as ApiStatusTag,
};

#[cfg(feature = "jvm")]
use jni::JNIEnv;

pub fn from_api(
   #[cfg(feature = "jvm")] env: &mut JNIEnv,
   instance: Cache<Instance>,
   entity: ApiStatus,
   account_cache_repository: &mut cache::account::Repository,
   status_cache_repository: &mut cache::status::StatusRepository,
   no_credential_status_cache_repository:
      &mut cache::status::NoCredentialStatusRepository,
   no_credential_poll_repository: &mut cache::poll::NoCredentialPollRepository,
) -> anyhow::Result<Status> {
   use anyhow::Context;
   use chrono::DateTime;
   use isolang::Language;
   use mastodon_entity::status::{
      NoCredentialStatus, StatusId, StatusLocalId, StatusVisibility,
   };
   use mastodon_entity::account::{AccountId, AccountLocalId};
   use crate::conversion;

   let ApiStatus {
      id, uri, created_at, account, content, visibility, sensitive, spoiler_text,
      media_attachments, application, mentions, tags, emojis, reblogs_count,
      favourites_count, replies_count, url, in_reply_to_id, in_reply_to_account_id,
      reblog, poll, card, language, text, edited_at, favourited, reblogged, muted,
      bookmarked, pinned, filtered,
   } = entity;

   let id = StatusId {
      instance_url: instance.get().url.clone(),
      local: StatusLocalId(id.context("No status id")?)
   };

   let boosted_status = reblog
      .and_then(|reblog|
         from_api(
            #[cfg(feature = "jvm")] env,
            instance.clone(),
            *reblog,
            account_cache_repository,
            status_cache_repository,
            no_credential_status_cache_repository,
            no_credential_poll_repository
         ).ok()
      )
      .map(|boosted| status_cache_repository.save(boosted));

   let poll = poll.and_then(|poll|
      conversion::poll::from_api(
         instance.clone(), poll, no_credential_poll_repository
      ).ok()
   );

   let no_credential = NoCredentialStatus {
      id: id.clone(),
      uri,
      created_time: created_at
         .and_then(|time| DateTime::parse_from_rfc3339(&time).ok())
         .map(|time| time.to_utc()),
      account: account
         .and_then(|acc|
            conversion::account::from_api(
               #[cfg(feature = "jvm")] env,
               instance.clone(),
               acc,
               account_cache_repository
            ).ok()
         )
         .map(|acc| account_cache_repository.save(acc)),
      content,
      visibility: visibility.map(StatusVisibility),
      is_sensitive: sensitive,
      spoiler_text,
      media_attachments: media_attachments.unwrap_or(vec![]).into_iter()
         .flat_map(|media|
            conversion::media_attachment::from_api(media)
         )
         .collect(),
      application: application.and_then(|app|
         conversion::application::from_api(app, instance.clone()).ok()
      ),
      mentions: mentions.unwrap_or(vec![]).into_iter()
         .flat_map(|mnt| mention_from_api(instance.clone(), mnt))
         .collect(),
      hashtags: tags.unwrap_or(vec![]).into_iter()
         .flat_map(|tag| tag_from_api(tag))
         .collect(),
      emojis: emojis.unwrap_or(vec![]).into_iter()
         .flat_map(|emj| conversion::custom_emoji::from_api(instance.clone(), emj))
         .collect(),
      boost_count: reblogs_count,
      favorite_count: favourites_count,
      reply_count: replies_count,
      url: url.and_then(|url| url.parse().ok()),
      replied_status_id: in_reply_to_id.map(|id|
         StatusId {
            instance_url: instance.get().url.clone(),
            local: StatusLocalId(id)
         }
      ),
      replied_account_id: in_reply_to_account_id.map(|id|
         AccountId {
            instance_url: instance.get().url.clone(),
            local: AccountLocalId(id)
         }
      ),
      boosted_status: boosted_status.as_ref().map(|s| s.get().no_credential.clone()),
      poll: poll.as_ref().map(|p| p.no_credential.clone()),
      card: card.and_then(|card|
         conversion::preview_card::from_api(
            #[cfg(feature = "jvm")] env,
            instance.clone(),
            card,
            account_cache_repository
         ).ok()
      ),
      language: language.and_then(|code| Language::from_639_1(&code)),
      text,
      edited_time: edited_at
         .and_then(|time| DateTime::parse_from_rfc3339(&time).ok())
         .map(|time| time.to_utc()),
   };

   let no_credential = no_credential_status_cache_repository.save(no_credential);

   let status = Status {
      id: id.clone(),
      no_credential,
      boosted_status,
      poll,
      is_favorited: favourited,
      is_boosted: reblogged,
      is_muted: muted,
      is_bookmarked: bookmarked,
      is_pinned: pinned,
      filter_results: filtered.unwrap_or(vec![]).into_iter()
         .flat_map(|fr|
            conversion::filter::filter_result_from_api(
               instance.get().url.clone(), fr
            )
         )
         .collect(),
   };

   Ok(status)
}

pub fn mention_from_api(
   instance: Cache<Instance>,
   entity: ApiStatusMention
) -> anyhow::Result<StatusMention> {
   use mastodon_entity::account::{AccountId, AccountLocalId};

   let ApiStatusMention { id, username, url, acct } = entity;

   let status_mention = StatusMention {
      mentioned_account_id: id.map(|id| AccountId {
         instance_url: instance.get().url.clone(),
         local: AccountLocalId(id)
      }),
      mentioned_account_username: username,
      mentioned_account_url: url.and_then(|url| url.parse().ok()),
      mentioned_account_acct: acct,
   };

   Ok(status_mention)
}

pub fn tag_from_api(
   entity: ApiStatusTag
) -> anyhow::Result<StatusHashtag> {
   let ApiStatusTag { name, url } = entity;

   let status_tag = StatusHashtag {
      name,
      url: url.and_then(|url| url.parse().ok()),
   };

   Ok(status_tag)
}
