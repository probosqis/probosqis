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

use mastodon_entity::status::Status;
use mastodon_entity::token::Token;

#[cfg(not(feature = "jvm"))]
use std::marker::PhantomData;

#[cfg(feature = "jvm")]
use jni::JNIEnv;

pub struct TimelineRepository<'jni> {
   #[cfg(not(feature = "jvm"))]
   env: PhantomData<&'jni ()>,
   #[cfg(feature = "jvm")]
   env: JNIEnv<'jni>
}

impl TimelineRepository<'_> {
   #[cfg(not(feature = "jvm"))]
   pub fn new() -> TimelineRepository<'static> {
      TimelineRepository {
         env: PhantomData
      }
   }

   #[cfg(feature = "jvm")]
   pub fn new<'jni>(env: &JNIEnv<'jni>) -> TimelineRepository<'jni> {
      TimelineRepository {
         env: unsafe { env.unsafe_clone() }
      }
   }

   pub fn get_home_timeline(
      &mut self,
      token: &Token
   ) -> anyhow::Result<Vec<Status>> {
      use ext_reqwest::CLIENT;
      use mastodon_webapi::api::timelines;
      use crate::cache;
      use crate::conversion;

      let api_timeline = timelines::get_home(
         &CLIENT,
         &token.instance.get().url,
         &token.access_token
      )?;

      let mut account_repo = cache::account::repo()
         .write(#[cfg(feature = "jvm")] &mut self.env)?;

      let mut status_repo = cache::status::status_repo()
         .write(#[cfg(feature = "jvm")] &mut self.env)?;

      let mut no_credential_status_repo = cache::status::no_credential_status_repo()
         .write(#[cfg(feature = "jvm")] &mut self.env)?;

      let mut no_credential_poll_repo = cache::poll::no_credential_poll_repo()
         .write(#[cfg(feature = "jvm")] &mut self.env)?;

      let timeline = api_timeline.into_iter()
         .enumerate()
         .flat_map(|(i, api_status)|
            conversion::status::from_api(
               #[cfg(feature = "jvm")] &mut self.env,
               token.instance.clone(),
               api_status,
               &mut account_repo,
               &mut status_repo,
               &mut no_credential_status_repo,
               &mut no_credential_poll_repo
            )
         )
         .collect();

      Ok(timeline)
   }
}

#[cfg(feature = "jvm")]
mod jvm {
   use jni::JNIEnv;
   use jni::objects::JObject;
   use mastodon_entity::jvm_types::{JvmStatus, JvmToken};
   use panoptiqon::jvm_types::JvmList;

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_repository_AndroidTimelineRepository_getHomeTimeline<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      token: JvmToken<'local>
   ) -> JvmList<'local, JvmStatus<'local>> {
      use ext_panoptiqon::unwrap_or_throw::UnwrapOrThrow;

      get_home_time_line(&mut env, token)
         .unwrap_or_throw_io_exception(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_repository_DesktopTimelineRepository_getHomeTimeline<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      token: JvmToken<'local>
   ) -> JvmList<'local, JvmStatus<'local>> {
      use ext_panoptiqon::unwrap_or_throw::UnwrapOrThrow;

      get_home_time_line(&mut env, token)
         .unwrap_or_throw_io_exception(&mut env)
   }

   fn get_home_time_line<'local>(
      env: &mut JNIEnv<'local>,
      token: JvmToken<'local>
   ) -> anyhow::Result<JvmList<'local, JvmStatus<'local>>> {
      use mastodon_entity::token::Token;
      use panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm};
      use super::TimelineRepository;

      let mut status_repository = TimelineRepository::new(env);

      let token = Token::clone_from_jvm(env, &token);
      let timeline = status_repository.get_home_timeline(&token)?;
      Ok(timeline.clone_into_jvm(env))
   }
}

#[cfg(all(test, not(feature = "jvm")))]
mod test {
   use std::time::Duration;
   use chrono::TimeZone;
   use isolang::Language;
   use super::TimelineRepository;

   #[test]
   fn get_home_timeline() {
      use chrono::{TimeZone, Utc};
      use mastodon_entity::account::{
         Account, AccountId, AccountLocalId, AccountProfileField
      };
      use mastodon_entity::application::Application;
      use mastodon_entity::custom_emoji::CustomEmoji;
      use mastodon_entity::filter::{
         Filter, FilterAction, FilterContext, FilterId, FilterKeyword,
         FilterKeywordId, FilterResult, FilterStatus, FilterStatusId,
      };
      use mastodon_entity::instance::Instance;
      use mastodon_entity::media_attachment::{
         ImageSize, ImageFocus, MediaAttachment, MediaAttachmentId,
         MediaAttachmentMetadata, VideoSize,
      };
      use mastodon_entity::poll::{
         NoCredentialPoll, Poll, PollId, PollLocalId, PollOption,
      };
      use mastodon_entity::preview_card::{PreviewCard, PreviewCardAuthor};
      use mastodon_entity::status::{
         NoCredentialStatus, Status, StatusHashtag, StatusId, StatusLocalId,
         StatusMention, StatusVisibility,
      };
      use mastodon_entity::token::Token;
      use mastodon_webapi::api::timelines;
      use mastodon_webapi::entity::account::{
         Account as ApiAccount,
         AccountField as ApiAccountField,
      };
      use mastodon_webapi::entity::application::{
         Application as ApiApplication,
         ApplicationClientSecretExpiresAt as ApiApplicationClientSecretExpiresAt,
      };
      use mastodon_webapi::entity::custom_emoji::CustomEmoji as ApiCustomEmoji;
      use mastodon_webapi::entity::filter::{
         Filter as ApiFilter,
         FilterKeyword as ApiFilterKeyword,
         FilterResult as ApiFilterResult,
         FilterStatus as ApiFilterStatus,
      };
      use mastodon_webapi::entity::media_attachment::{
         MediaAttachment as ApiMediaAttachment,
         MediaAttachmentFocus as ApiMediaAttachmentFocus,
         MediaAttachmentMetadata as ApiMediaAttachmentMetadata,
         MediaAttachmentSize as ApiMediaAttachmentSize,
      };
      use mastodon_webapi::entity::poll::{
         Poll as ApiPoll,
         PollOption as ApiPollOption,
      };
      use mastodon_webapi::entity::preview_card::{
         PreviewCard as ApiPreviewCard,
         PreviewCardAuthor as ApiPreviewCardAuthor,
      };
      use mastodon_webapi::entity::status::{
         Status as ApiStatus,
         StatusMention as ApiStatusMention,
         StatusTag as ApiStatusTag,
      };
      use crate::cache;

      let mut repository = TimelineRepository::new();

      timelines::inject_get_verify_credentials(|_, _, _|
         Ok(vec![
            ApiStatus {
               id: Some("status id 1".to_string()),
               uri: Some("uri1".to_string()),
               created_at: Some("2000-01-01T00:00:00Z".to_string()),
               account: Some(ApiAccount {
                  id: Some("account id 1".to_string()),
                  username: Some("username1".to_string()),
                  acct: Some("acct1".to_string()),
                  url: Some("https://example.com/account1".to_string()),
                  display_name: Some("display name 1".to_string()),
                  note: Some("note1".to_string()),
                  avatar: Some("https://example.com/avatar1".to_string()),
                  avatar_static: Some("https://example.com/static/avatar1".to_string()),
                  header: Some("https://example.com/header1".to_string()),
                  header_static: Some("https://example.com/static/header1".to_string()),
                  locked: Some(false),
                  fields: Some(vec![
                     ApiAccountField {
                        name: Some("account field name 1".to_string()),
                        value: Some("account field value 1".to_string()),
                        verified_at: Some("2000-01-02T00:00:00Z".to_string()),
                     },
                     ApiAccountField {
                        name: Some("account field name 2".to_string()),
                        value: Some("account field value 2".to_string()),
                        verified_at: Some("2000-01-03T00:00:00Z".to_string()),
                     },
                  ]),
                  emojis: Some(vec![
                     ApiCustomEmoji {
                        shortcode: Some("shortcode1".to_string()),
                        url: Some("https://example.com/emoji1".to_string()),
                        static_url: Some("https://example.com/static/emoji1".to_string()),
                        visible_in_picker: Some(false),
                        category: Some("category".to_string()),
                     },
                     ApiCustomEmoji {
                        shortcode: Some("shortcode2".to_string()),
                        url: Some("https://example.com/emoji2".to_string()),
                        static_url: Some("https://example.com/static/emoji2".to_string()),
                        visible_in_picker: Some(true),
                        category: Some("category".to_string()),
                     },
                  ]),
                  bot: Some(true),
                  group: Some(false),
                  discoverable: Some(true),
                  noindex: Some(false),
                  moved: Some(Box::new(ApiAccount {
                     id: Some("moved account id".to_string()),
                     username: None,
                     acct: None,
                     url: None,
                     display_name: None,
                     note: None,
                     avatar: None,
                     avatar_static: None,
                     header: None,
                     header_static: None,
                     locked: None,
                     fields: None,
                     emojis: None,
                     bot: None,
                     group: None,
                     discoverable: None,
                     noindex: None,
                     moved: None,
                     suspended: None,
                     limited: None,
                     created_at: None,
                     last_status_at: None,
                     statuses_count: None,
                     followers_count: None,
                     following_count: None,
                     source: None,
                     role: None,
                     mute_expires_at: None,
                  })),
                  suspended: Some(false),
                  limited: Some(true),
                  created_at: Some("2000-01-04T00:00:00Z".to_string()),
                  last_status_at: Some("2000-01-05T00:00:00Z".to_string()),
                  statuses_count: Some(123),
                  followers_count: Some(45),
                  following_count: Some(67),
                  source: None,
                  role: None,
                  mute_expires_at: None,
               }),
               content: Some("content".to_string()),
               visibility: Some("public".to_string()),
               sensitive: Some(true),
               spoiler_text: Some("spoiler text".to_string()),
               media_attachments: Some(vec![
                  ApiMediaAttachment {
                     id: Some("media attachment id 1".to_string()),
                     r#type: Some("image".to_string()),
                     url: Some("https://example.com/media_attachment1".to_string()),
                     preview_url: Some("https://example.com/preview/media_attachment1".to_string()),
                     remote_url: Some("https://example.com/remote/media_attachment1".to_string()),
                     meta: Some(ApiMediaAttachmentMetadata {
                        original: Some(ApiMediaAttachmentSize {
                           width: Some(123),
                           height: Some(456),
                           size: None,
                           aspect: None,
                           frame_rate: None,
                           duration: None,
                           bitrate: None,
                        }),
                        small: Some(ApiMediaAttachmentSize {
                           width: Some(78),
                           height: Some(90),
                           size: None,
                           aspect: None,
                           frame_rate: None,
                           duration: None,
                           bitrate: None,
                        }),
                        focus: Some(ApiMediaAttachmentFocus {
                           x: Some(0.1),
                           y: Some(0.2),
                        }),
                        length: None,
                        duration: None,
                        fps: None,
                        size: None,
                        width: None,
                        height: None,
                        aspect: None,
                        audio_encode: None,
                        audio_bitrate: None,
                        audio_channels: None,
                     }),
                     description: Some("description".to_string()),
                     blurhash: Some("blurhash".to_string()),
                     text_url: None,
                  },
                  ApiMediaAttachment {
                     id: Some("media attachment id 2".to_string()),
                     r#type: Some("video".to_string()),
                     url: Some("https://example.com/media_attachment2".to_string()),
                     preview_url: Some("https://example.com/preview/media_attachment2".to_string()),
                     remote_url: Some("https://example.com/remote/media_attachment2".to_string()),
                     meta: Some(ApiMediaAttachmentMetadata {
                        original: Some(ApiMediaAttachmentSize {
                           width: Some(123),
                           height: Some(456),
                           size: None,
                           aspect: None,
                           frame_rate: Some("frame rate".to_string()),
                           duration: Some(12.3),
                           bitrate: Some(78),
                        }),
                        small: Some(ApiMediaAttachmentSize {
                           width: Some(901),
                           height: Some(234),
                           size: None,
                           aspect: None,
                           frame_rate: None,
                           duration: None,
                           bitrate: None,
                        }),
                        focus: None,
                        length: Some("length".to_string()),
                        duration: Some(45.6),
                        fps: Some(56),
                        size: None,
                        width: None,
                        height: None,
                        aspect: None,
                        audio_encode: Some("audio encode".to_string()),
                        audio_bitrate: Some("audio bitrate".to_string()),
                        audio_channels: Some("audio channels".to_string()),
                     }),
                     description: Some("description".to_string()),
                     blurhash: Some("blurhash".to_string()),
                     text_url: None,
                  },
               ]),
               application: Some(ApiApplication {
                  name: "application name".to_string(),
                  website: Some("https://example.com/application".to_string()),
                  scopes: Some(vec!["read".to_string(), "write".to_string()]),
                  redirect_uris: Some(vec!["https://example.com/application/redirect".to_string()]),
                  redirect_uri: Some("https://example.com/application/redirect".to_string()),
                  vapid_key: None,
                  client_id: Some("client id".to_string()),
                  client_secret: Some("client secret".to_string()),
                  client_secret_expires_at: Some(
                     ApiApplicationClientSecretExpiresAt
                        ::ExpiresAt("2000-01-09T00:00:00Z".to_string()),
                  ),
               }),
               mentions: Some(vec![
                  ApiStatusMention {
                     id: Some("mentioned account id 1".to_string()),
                     username: Some("mentioned username 1".to_string()),
                     url: Some("https://example.com/mentioned1".to_string()),
                     acct: Some("mentioned acct 1".to_string()),
                  },
                  ApiStatusMention {
                     id: Some("mentioned account id 2".to_string()),
                     username: Some("mentioned username 2".to_string()),
                     url: Some("https://example.com/mentioned2".to_string()),
                     acct: Some("mentioned acct 2".to_string()),
                  },
               ]),
               tags: Some(vec![
                  ApiStatusTag {
                     name: Some("hash tag 1".to_string()),
                     url: Some("https://example.com/hashtag1".to_string()),
                  },
                  ApiStatusTag {
                     name: Some("hash tag 2".to_string()),
                     url: Some("https://example.com/hashtag2".to_string()),
                  },
               ]),
               emojis: Some(vec![
                  ApiCustomEmoji {
                     shortcode: Some("shortcode3".to_string()),
                     url: Some("https://example.com/emoji3".to_string()),
                     static_url: Some("https://example.com/static/emoji3".to_string()),
                     visible_in_picker: Some(true),
                     category: Some("category".to_string()),
                  },
                  ApiCustomEmoji {
                     shortcode: Some("shortcode4".to_string()),
                     url: Some("https://example.com/emoji4".to_string()),
                     static_url: Some("https://example.com/static/emoji4".to_string()),
                     visible_in_picker: Some(true),
                     category: Some("category".to_string()),
                  },
               ]),
               reblogs_count: Some(123),
               favourites_count: Some(456),
               replies_count: Some(78),
               url: Some("https://example.com/status".to_string()),
               in_reply_to_id: Some("replied status id".to_string()),
               in_reply_to_account_id: Some("replied account id".to_string()),
               reblog: Some(Box::new(
                  ApiStatus {
                     id: Some("boosted status id".to_string()),
                     uri: None,
                     created_at: None,
                     account: None,
                     content: None,
                     visibility: None,
                     sensitive: None,
                     spoiler_text: None,
                     media_attachments: None,
                     application: None,
                     mentions: None,
                     tags: None,
                     emojis: None,
                     reblogs_count: None,
                     favourites_count: None,
                     replies_count: None,
                     url: None,
                     in_reply_to_id: None,
                     in_reply_to_account_id: None,
                     reblog: None,
                     poll: None,
                     card: None,
                     language: None,
                     text: None,
                     edited_at: None,
                     favourited: None,
                     reblogged: None,
                     muted: None,
                     bookmarked: None,
                     pinned: None,
                     filtered: None,
                  }
               )),
               poll: Some(ApiPoll {
                  id: Some("poll id".to_string()),
                  expires_at: Some("2000-01-10T00:00:00Z".to_string()),
                  expired: Some(false),
                  multiple: Some(true),
                  votes_count: Some(12),
                  voters_count: Some(3),
                  options: Some(vec![
                     ApiPollOption {
                        title: Some("poll option 1".to_string()),
                        votes_count: Some(4),
                     },
                     ApiPollOption {
                        title: Some("poll option 2".to_string()),
                        votes_count: Some(5),
                     },
                  ]),
                  emojis: Some(vec![
                     ApiCustomEmoji {
                        shortcode: Some("shortcode5".to_string()),
                        url: Some("https://example.com/emoji5".to_string()),
                        static_url: Some("https://example.com/static/emoji5".to_string()),
                        visible_in_picker: Some(true),
                        category: Some("category".to_string()),
                     },
                     ApiCustomEmoji {
                        shortcode: Some("shortcode6".to_string()),
                        url: Some("https://example.com/emoji6".to_string()),
                        static_url: Some("https://example.com/static/emoji6".to_string()),
                        visible_in_picker: Some(true),
                        category: Some("category".to_string()),
                     },
                  ]),
                  voted: Some(true),
                  own_voted: Some(vec![6, 7]),
               }),
               card: Some(ApiPreviewCard {
                  url: Some("https://example.com/preview/card".to_string()),
                  title: Some("preview card title".to_string()),
                  description: Some("preview card description".to_string()),
                  r#type: Some("link".to_string()),
                  authors: Some(vec![
                     ApiPreviewCardAuthor {
                        name: Some("preview card author name 1".to_string()),
                        url: Some("https://example.com/preview/author1".to_string()),
                        account: Some(ApiAccount {
                           id: Some("preview card author id 1".to_string()),
                           username: None,
                           acct: None,
                           url: None,
                           display_name: None,
                           note: None,
                           avatar: None,
                           avatar_static: None,
                           header: None,
                           header_static: None,
                           locked: None,
                           fields: None,
                           emojis: None,
                           bot: None,
                           group: None,
                           discoverable: None,
                           noindex: None,
                           moved: None,
                           suspended: None,
                           limited: None,
                           created_at: None,
                           last_status_at: None,
                           statuses_count: None,
                           followers_count: None,
                           following_count: None,
                           source: None,
                           role: None,
                           mute_expires_at: None,
                        }),
                     },
                     ApiPreviewCardAuthor {
                        name: Some("preview card author name 2".to_string()),
                        url: Some("https://example.com/preview/author2".to_string()),
                        account: Some(ApiAccount {
                           id: Some("preview card author id 2".to_string()),
                           username: None,
                           acct: None,
                           url: None,
                           display_name: None,
                           note: None,
                           avatar: None,
                           avatar_static: None,
                           header: None,
                           header_static: None,
                           locked: None,
                           fields: None,
                           emojis: None,
                           bot: None,
                           group: None,
                           discoverable: None,
                           noindex: None,
                           moved: None,
                           suspended: None,
                           limited: None,
                           created_at: None,
                           last_status_at: None,
                           statuses_count: None,
                           followers_count: None,
                           following_count: None,
                           source: None,
                           role: None,
                           mute_expires_at: None,
                        }),
                     },
                  ]),
                  author_name: None,
                  author_url: None,
                  provider_name: Some("preview card provider name".to_string()),
                  provider_url: Some("https://example.com/preview/provider".to_string()),
                  html: Some("preview card html".to_string()),
                  width: Some(1234),
                  height: Some(567),
                  image: Some("https://example.com/preview/image".to_string()),
                  embed_url: Some("https://example.com/preview/embed".to_string()),
                  blurhash: Some("blurhash".to_string()),
               }),
               language: Some("ja".to_string()),
               text: Some("text".to_string()),
               edited_at: Some("2000-01-11T00:00:00Z".to_string()),
               favourited: Some(false),
               reblogged: Some(true),
               muted: Some(false),
               bookmarked: Some(true),
               pinned: Some(false),
               filtered: Some(vec![
                  ApiFilterResult {
                     filter: Some(ApiFilter {
                        id: Some("filter id 1".to_string()),
                        title: Some("filter title 1".to_string()),
                        context: Some(vec!["home".to_string()]),
                        expires_at: Some("2000-01-12T00:00:00Z".to_string()),
                        filter_action: Some("hide".to_string()),
                        keywords: Some(vec![
                           ApiFilterKeyword {
                              id: Some("filter keyword id 1".to_string()),
                              keyword: Some("filter keyword 1".to_string()),
                              whole_word: Some(true),
                           },
                           ApiFilterKeyword {
                              id: Some("filter keyword id 2".to_string()),
                              keyword: Some("filter keyword 2".to_string()),
                              whole_word: Some(true),
                           },
                        ]),
                        statuses: Some(vec![
                           ApiFilterStatus {
                              id: Some("filter status id 1".to_string()),
                              status_id: Some("filtered status id 1".to_string()),
                           },
                           ApiFilterStatus {
                              id: Some("filter status id 2".to_string()),
                              status_id: Some("filtered status id 2".to_string()),
                           },
                        ]),
                     }),
                     keyword_matches: Some(vec!["matched keyword 1".to_string()]),
                     status_matches: Some(vec!["matched status id 1".to_string()]),
                  },
                  ApiFilterResult {
                     filter: Some(ApiFilter {
                        id: Some("filter id 2".to_string()),
                        title: Some("filter title 2".to_string()),
                        context: Some(vec!["public".to_string()]),
                        expires_at: Some("2000-01-13T00:00:00Z".to_string()),
                        filter_action: Some("warn".to_string()),
                        keywords: Some(vec![
                           ApiFilterKeyword {
                              id: Some("filter keyword id 3".to_string()),
                              keyword: Some("filter keyword 3".to_string()),
                              whole_word: Some(true),
                           },
                           ApiFilterKeyword {
                              id: Some("filter keyword id 4".to_string()),
                              keyword: Some("filter keyword 4".to_string()),
                              whole_word: Some(true),
                           },
                        ]),
                        statuses: Some(vec![
                           ApiFilterStatus {
                              id: Some("filter status id 3".to_string()),
                              status_id: Some("filtered status id 3".to_string()),
                           },
                           ApiFilterStatus {
                              id: Some("filter status id 4".to_string()),
                              status_id: Some("filtered status id 4".to_string()),
                           },
                        ]),
                     }),
                     keyword_matches: Some(vec!["matched keyword 2".to_string()]),
                     status_matches: Some(vec!["matched status id 2".to_string()]),
                  },
               ]),
            },
            ApiStatus {
               id: Some("status id 2".to_string()),
               uri: None,
               created_at: None,
               account: None,
               content: None,
               visibility: None,
               sensitive: None,
               spoiler_text: None,
               media_attachments: None,
               application: None,
               mentions: None,
               tags: None,
               emojis: None,
               reblogs_count: None,
               favourites_count: None,
               replies_count: None,
               url: None,
               in_reply_to_id: None,
               in_reply_to_account_id: None,
               reblog: None,
               poll: None,
               card: None,
               language: None,
               text: None,
               edited_at: None,
               favourited: None,
               reblogged: None,
               muted: None,
               bookmarked: None,
               pinned: None,
               filtered: None,
            },
         ])
      );

      let instance = Instance {
         url: "https://example.com/".parse().unwrap(),
         version: "0.0.0".to_string(),
         version_checked_time: Utc.with_ymd_and_hms(2000, 1, 1, 0, 0, 0).unwrap(),
      };

      let instance_cache = cache::instance::repo().write().unwrap().save(instance);

      let token = Token {
         instance: instance_cache.clone(),
         access_token: "access token".to_string(),
         token_type: "token type".to_string(),
         scope: "scope".to_string(),
         created_at: Utc.with_ymd_and_hms(2000, 1, 1, 0, 0, 0).unwrap(),
      };

      let statuses = repository.get_home_timeline(&token).unwrap();

      assert_eq!(
         vec![
            Status {
               id: StatusId {
                  instance_url: instance_cache.get().url.clone(),
                  local: StatusLocalId("status id 1".to_string()),
               },
               no_credential: {
                  let id = StatusId {
                     instance_url: instance_cache.get().url.clone(),
                     local: StatusLocalId("status id 1".to_string()),
                  };

                  assert_eq!(
                     NoCredentialStatus {
                        id: id.clone(),
                        uri: Some("uri1".to_string()),
                        created_time: Some(Utc.with_ymd_and_hms(2000, 1, 1, 0, 0, 0).unwrap()),
                        account: {
                           let id = AccountId {
                              instance_url: instance_cache.get().url.clone(),
                              local: AccountLocalId("account id 1".to_string()),
                           };

                           assert_eq!(
                              Account {
                                 instance: instance_cache.clone(),
                                 id: id.clone(),
                                 username: Some("username1".to_string()),
                                 acct: Some("acct1".to_string()),
                                 url: Some("https://example.com/account1".parse().unwrap()),
                                 display_name: Some("display name 1".to_string()),
                                 profile_note: Some("note1".to_string()),
                                 avatar_image_url: Some("https://example.com/avatar1".parse().unwrap()),
                                 avatar_static_image_url: Some("https://example.com/static/avatar1".parse().unwrap()),
                                 header_image_url: Some("https://example.com/header1".parse().unwrap()),
                                 header_static_image_url: Some("https://example.com/static/header1".parse().unwrap()),
                                 is_locked: Some(false),
                                 profile_fields: vec![
                                    AccountProfileField {
                                       name: Some("account field name 1".to_string()),
                                       value: Some("account field value 1".to_string()),
                                       verified_time: Some(Utc.with_ymd_and_hms(2000, 1, 2, 0, 0, 0).unwrap()),
                                    },
                                    AccountProfileField {
                                       name: Some("account field name 2".to_string()),
                                       value: Some("account field value 2".to_string()),
                                       verified_time: Some(Utc.with_ymd_and_hms(2000, 1, 3, 0, 0, 0).unwrap()),
                                    },
                                 ],
                                 emojis_in_profile: vec![
                                    CustomEmoji {
                                       instance: instance_cache.clone(),
                                       shortcode: "shortcode1".to_string(),
                                       image_url: "https://example.com/emoji1".parse().unwrap(),
                                       static_image_url: Some("https://example.com/static/emoji1".parse().unwrap()),
                                       is_visible_in_picker: Some(false),
                                       category: Some("category".to_string()),
                                    },
                                    CustomEmoji {
                                       instance: instance_cache.clone(),
                                       shortcode: "shortcode2".to_string(),
                                       image_url: "https://example.com/emoji2".parse().unwrap(),
                                       static_image_url: Some("https://example.com/static/emoji2".parse().unwrap()),
                                       is_visible_in_picker: Some(true),
                                       category: Some("category".to_string()),
                                    },
                                 ],
                                 is_bot: Some(true),
                                 is_group: Some(false),
                                 is_discoverable: Some(true),
                                 is_noindex: Some(false),
                                 moved_to: {
                                    let id = AccountId {
                                       instance_url: instance_cache.get().url.clone(),
                                       local: AccountLocalId("moved account id".to_string()),
                                    };

                                    assert_eq!(
                                       Account {
                                          instance: instance_cache.clone(),
                                          id: id.clone(),
                                          username: None,
                                          acct: None,
                                          url: None,
                                          display_name: None,
                                          profile_note: None,
                                          avatar_image_url: None,
                                          avatar_static_image_url: None,
                                          header_image_url: None,
                                          header_static_image_url: None,
                                          is_locked: None,
                                          profile_fields: vec![],
                                          emojis_in_profile: vec![],
                                          is_bot: None,
                                          is_group: None,
                                          is_discoverable: None,
                                          is_noindex: None,
                                          moved_to: None,
                                          is_suspended: None,
                                          is_limited: None,
                                          created_time: None,
                                          last_status_post_time: None,
                                          status_count: None,
                                          follower_count: None,
                                          followee_count: None,
                                       },
                                       *statuses[0].no_credential.get()
                                          .account.as_ref().unwrap().get()
                                          .moved_to.as_ref().unwrap().get()
                                    );

                                    let moved_to = cache::account::repo()
                                       .read().unwrap()
                                       .load(id).unwrap();

                                    Some(moved_to)
                                 },
                                 is_suspended: Some(false),
                                 is_limited: Some(true),
                                 created_time: Some(Utc.with_ymd_and_hms(2000, 1, 4, 0, 0, 0).unwrap()),
                                 last_status_post_time: Some(Utc.with_ymd_and_hms(2000, 1, 5, 0, 0, 0).unwrap()),
                                 status_count: Some(123),
                                 follower_count: Some(45),
                                 followee_count: Some(67),
                              },
                              *statuses[0].no_credential.get()
                                 .account.as_ref().unwrap().get()
                           );

                           let account = cache::account::repo()
                              .read().unwrap()
                              .load(id).unwrap();

                           Some(account)
                        },
                        content: Some("content".to_string()),
                        visibility: Some(StatusVisibility("public".to_string())),
                        is_sensitive: Some(true),
                        spoiler_text: Some("spoiler text".to_string()),
                        media_attachments: vec![
                           MediaAttachment {
                              id: MediaAttachmentId("media attachment id 1".to_string()),
                              url: Some("https://example.com/media_attachment1".parse().unwrap()),
                              preview_url: Some("https://example.com/preview/media_attachment1".parse().unwrap()),
                              remote_url: Some("https://example.com/remote/media_attachment1".parse().unwrap()),
                              metadata: Some(MediaAttachmentMetadata::Image {
                                 original_size: Some(ImageSize {
                                    width: 123,
                                    height: 456,
                                 }),
                                 small_size: Some(ImageSize {
                                    width: 78,
                                    height: 90,
                                 }),
                                 focus: Some(ImageFocus {
                                    x: 0.1,
                                    y: 0.2,
                                 }),
                              }),
                              description: Some("description".to_string()),
                              blurhash: Some("blurhash".to_string()),
                           },
                           MediaAttachment {
                              id: MediaAttachmentId("media attachment id 2".to_string()),
                              url: Some("https://example.com/media_attachment2".parse().unwrap()),
                              preview_url: Some("https://example.com/preview/media_attachment2".parse().unwrap()),
                              remote_url: Some("https://example.com/remote/media_attachment2".parse().unwrap()),
                              metadata: Some(MediaAttachmentMetadata::Video {
                                 original_size: Some(VideoSize {
                                    width: Some(123),
                                    height: Some(456),
                                    frame_rate: Some("frame rate".to_string()),
                                    duration: Some(Duration::from_secs_f64(12.3)),
                                    bitrate: Some(78),
                                 }),
                                 small_size: Some(ImageSize {
                                    width: 901,
                                    height: 234,
                                 }),
                                 length: Some("length".to_string()),
                                 fps: Some(56),
                                 audio_encode: Some("audio encode".to_string()),
                                 audio_bitrate: Some("audio bitrate".to_string()),
                                 audio_channels: Some("audio channels".to_string()),
                              }),
                              description: Some("description".to_string()),
                              blurhash: Some("blurhash".to_string()),
                           },
                        ],
                        application: Some(Application {
                           instance: instance_cache.clone(),
                           name: "application name".to_string(),
                           website: Some("https://example.com/application".parse().unwrap()),
                           scopes: vec!["read".to_string(), "write".to_string()],
                           redirect_uris: vec![
                              "https://example.com/application/redirect".to_string()
                           ],
                           client_id: Some("client id".to_string()),
                           client_secret: Some("client secret".to_string()),
                           client_secret_expire_time:
                              Some(Utc.with_ymd_and_hms(2000, 1, 9, 0, 0, 0).unwrap()),
                        }),
                        mentions: vec![
                           StatusMention {
                              mentioned_account_id: Some(AccountId {
                                 instance_url: instance_cache.get().url.clone(),
                                 local: AccountLocalId("mentioned account id 1".to_string()),
                              }),
                              mentioned_account_username: Some("mentioned username 1".to_string()),
                              mentioned_account_url: Some("https://example.com/mentioned1".parse().unwrap()),
                              mentioned_account_acct: Some("mentioned acct 1".to_string()),
                           },
                           StatusMention {
                              mentioned_account_id: Some(AccountId {
                                 instance_url: instance_cache.get().url.clone(),
                                 local: AccountLocalId("mentioned account id 2".to_string()),
                              }),
                              mentioned_account_username: Some("mentioned username 2".to_string()),
                              mentioned_account_url: Some("https://example.com/mentioned2".parse().unwrap()),
                              mentioned_account_acct: Some("mentioned acct 2".to_string()),
                           },
                        ],
                        hashtags: vec![
                           StatusHashtag {
                              name: Some("hash tag 1".to_string()),
                              url: Some("https://example.com/hashtag1".parse().unwrap()),
                           },
                           StatusHashtag {
                              name: Some("hash tag 2".to_string()),
                              url: Some("https://example.com/hashtag2".parse().unwrap()),
                           },
                        ],
                        emojis: vec![
                           CustomEmoji {
                              instance: instance_cache.clone(),
                              shortcode: "shortcode3".to_string(),
                              image_url: "https://example.com/emoji3".parse().unwrap(),
                              static_image_url: Some("https://example.com/static/emoji3".parse().unwrap()),
                              is_visible_in_picker: Some(true),
                              category: Some("category".to_string()),
                           },
                           CustomEmoji {
                              instance: instance_cache.clone(),
                              shortcode: "shortcode4".to_string(),
                              image_url: "https://example.com/emoji4".parse().unwrap(),
                              static_image_url: Some("https://example.com/static/emoji4".parse().unwrap()),
                              is_visible_in_picker: Some(true),
                              category: Some("category".to_string()),
                           },
                        ],
                        boost_count: Some(123),
                        favorite_count: Some(456),
                        reply_count: Some(78),
                        url: Some("https://example.com/status".parse().unwrap()),
                        replied_status_id: Some(StatusId {
                           instance_url: instance_cache.get().url.clone(),
                           local: StatusLocalId("replied status id".to_string()),
                        }),
                        replied_account_id: Some(AccountId {
                           instance_url: instance_cache.get().url.clone(),
                           local: AccountLocalId("replied account id".to_string()),
                        }),
                        boosted_status: {
                           let id = StatusId {
                              instance_url: instance_cache.get().url.clone(),
                              local: StatusLocalId("boosted status id".to_string()),
                           };

                           assert_eq!(
                              NoCredentialStatus {
                                 id: id.clone(),
                                 uri: None,
                                 created_time: None,
                                 account: None,
                                 content: None,
                                 visibility: None,
                                 is_sensitive: None,
                                 spoiler_text: None,
                                 media_attachments: vec![],
                                 application: None,
                                 mentions: vec![],
                                 hashtags: vec![],
                                 emojis: vec![],
                                 boost_count: None,
                                 favorite_count: None,
                                 reply_count: None,
                                 url: None,
                                 replied_status_id: None,
                                 replied_account_id: None,
                                 boosted_status: None,
                                 poll: None,
                                 card: None,
                                 language: None,
                                 text: None,
                                 edited_time: None,
                              },
                              *statuses[0].no_credential.get()
                                 .boosted_status.as_ref().unwrap().get()
                           );

                           let boosted_status = cache::status::no_credential_status_repo()
                              .read().unwrap()
                              .load(id).unwrap();

                           Some(boosted_status)
                        },
                        poll: {
                           let id = PollId {
                              instance_url: instance_cache.get().url.clone(),
                              local: PollLocalId("poll id".to_string()),
                           };

                           assert_eq!(
                              NoCredentialPoll {
                                 id: id.clone(),
                                 expire_time: Some(
                                    Utc.with_ymd_and_hms(2000, 1, 10, 0, 0, 0).unwrap()
                                 ),
                                 is_expired: Some(false),
                                 allows_multiple_choices: Some(true),
                                 vote_count: Some(12),
                                 voter_count: Some(3),
                                 poll_options: vec![
                                    PollOption {
                                       title: Some("poll option 1".to_string()),
                                       vote_count: Some(4),
                                    },
                                    PollOption {
                                       title: Some("poll option 2".to_string()),
                                       vote_count: Some(5),
                                    },
                                 ],
                                 emojis: vec![
                                    CustomEmoji {
                                       instance: instance_cache.clone(),
                                       shortcode: "shortcode5".to_string(),
                                       image_url: "https://example.com/emoji5".parse().unwrap(),
                                       static_image_url: Some("https://example.com/static/emoji5".parse().unwrap()),
                                       is_visible_in_picker: Some(true),
                                       category: Some("category".to_string()),
                                    },
                                    CustomEmoji {
                                       instance: instance_cache.clone(),
                                       shortcode: "shortcode6".to_string(),
                                       image_url: "https://example.com/emoji6".parse().unwrap(),
                                       static_image_url: Some("https://example.com/static/emoji6".parse().unwrap()),
                                       is_visible_in_picker: Some(true),
                                       category: Some("category".to_string()),
                                    },
                                 ],
                              },
                              *statuses[0].no_credential.get()
                                 .poll.as_ref().unwrap().get()
                           );

                           let poll = cache::poll::no_credential_poll_repo()
                              .read().unwrap()
                              .load(id).unwrap();

                           Some(poll)
                        },
                        card: Some(PreviewCard {
                           url: Some("https://example.com/preview/card".parse().unwrap()),
                           title: Some("preview card title".to_string()),
                           description: Some("preview card description".to_string()),
                           card_type: Some("link".to_string()),
                           authors: vec![
                              PreviewCardAuthor {
                                 name: Some("preview card author name 1".to_string()),
                                 url: Some("https://example.com/preview/author1".parse().unwrap()),
                                 account: {
                                    let id = AccountId {
                                       instance_url: instance_cache.get().url.clone(),
                                       local: AccountLocalId("preview card author id 1".to_string()),
                                    };

                                    assert_eq!(
                                       Account {
                                          instance: instance_cache.clone(),
                                          id: id.clone(),
                                          username: None,
                                          acct: None,
                                          url: None,
                                          display_name: None,
                                          profile_note: None,
                                          avatar_image_url: None,
                                          avatar_static_image_url: None,
                                          header_image_url: None,
                                          header_static_image_url: None,
                                          is_locked: None,
                                          profile_fields: vec![],
                                          emojis_in_profile: vec![],
                                          is_bot: None,
                                          is_group: None,
                                          is_discoverable: None,
                                          is_noindex: None,
                                          moved_to: None,
                                          is_suspended: None,
                                          is_limited: None,
                                          created_time: None,
                                          last_status_post_time: None,
                                          status_count: None,
                                          follower_count: None,
                                          followee_count: None,
                                       },
                                       *statuses[0].no_credential.get()
                                          .card.as_ref().unwrap()
                                          .authors[0]
                                          .account.as_ref().unwrap().get()
                                    );

                                    let account = cache::account::repo()
                                       .read().unwrap()
                                       .load(id).unwrap();

                                    Some(account)
                                 },
                              },
                              PreviewCardAuthor {
                                 name: Some("preview card author name 2".to_string()),
                                 url: Some("https://example.com/preview/author2".parse().unwrap()),
                                 account: {
                                    let id = AccountId {
                                       instance_url: instance_cache.get().url.clone(),
                                       local: AccountLocalId("preview card author id 2".to_string()),
                                    };

                                    assert_eq!(
                                       Account {
                                          instance: instance_cache.clone(),
                                          id: id.clone(),
                                          username: None,
                                          acct: None,
                                          url: None,
                                          display_name: None,
                                          profile_note: None,
                                          avatar_image_url: None,
                                          avatar_static_image_url: None,
                                          header_image_url: None,
                                          header_static_image_url: None,
                                          is_locked: None,
                                          profile_fields: vec![],
                                          emojis_in_profile: vec![],
                                          is_bot: None,
                                          is_group: None,
                                          is_discoverable: None,
                                          is_noindex: None,
                                          moved_to: None,
                                          is_suspended: None,
                                          is_limited: None,
                                          created_time: None,
                                          last_status_post_time: None,
                                          status_count: None,
                                          follower_count: None,
                                          followee_count: None,
                                       },
                                       *statuses[0].no_credential.get()
                                          .card.as_ref().unwrap()
                                          .authors[1]
                                          .account.as_ref().unwrap().get()
                                    );

                                    let account = cache::account::repo()
                                       .read().unwrap()
                                       .load(id).unwrap();

                                    Some(account)
                                 },
                              },
                           ],
                           provider_name: Some("preview card provider name".to_string()),
                           provider_url: Some("https://example.com/preview/provider".parse().unwrap()),
                           html: Some("preview card html".to_string()),
                           width: Some(1234),
                           height: Some(567),
                           image_url: Some("https://example.com/preview/image".parse().unwrap()),
                           embed_url: Some("https://example.com/preview/embed".parse().unwrap()),
                           blurhash: Some("blurhash".to_string()),
                        }),
                        language: Some(Language::from_639_1("ja").unwrap()),
                        text: Some("text".to_string()),
                        edited_time: Some(Utc.with_ymd_and_hms(2000, 1, 11, 0, 0, 0).unwrap()),
                     },
                     *statuses[0].no_credential.get()
                  );

                  cache::status::no_credential_status_repo()
                     .read().unwrap()
                     .load(id).unwrap()
               },
               boosted_status: {
                  let id = StatusId {
                     instance_url: instance_cache.get().url.clone(),
                     local: StatusLocalId("boosted status id".to_string()),
                  };

                  assert_eq!(
                     Status {
                        id: id.clone(),
                        no_credential: cache::status::no_credential_status_repo()
                              .read().unwrap()
                              .load(id.clone()).unwrap(),
                        boosted_status: None,
                        poll: None,
                        is_favorited: None,
                        is_boosted: None,
                        is_muted: None,
                        is_bookmarked: None,
                        is_pinned: None,
                        filter_results: vec![],
                     },
                     *statuses[0].boosted_status.as_ref().unwrap().get()
                  );

                  let boosted_status = cache::status::status_repo()
                     .read().unwrap()
                     .load(id).unwrap();

                  Some(boosted_status)
               },
               poll: {
                  let id = PollId {
                     instance_url: instance_cache.get().url.clone(),
                     local: PollLocalId("poll id".to_string()),
                  };

                  Some(Poll {
                     id: id.clone(),
                     no_credential: cache::poll::no_credential_poll_repo()
                        .read().unwrap()
                        .load(id).unwrap(),
                     is_voted: Some(true),
                     voted_options: vec![6, 7],
                  })
               },
               is_favorited: Some(false),
               is_boosted: Some(true),
               is_muted: Some(false),
               is_bookmarked: Some(true),
               is_pinned: Some(false),
               filter_results: vec![
                  FilterResult {
                     filter: Some(Filter {
                        id: FilterId("filter id 1".to_string()),
                        title: Some("filter title 1".to_string()),
                        context: vec![FilterContext("home".to_string())],
                        expire_time: Some(Utc.with_ymd_and_hms(2000, 1, 12, 0, 0, 0).unwrap()),
                        filter_action: Some(FilterAction("hide".to_string())),
                        keywords: vec![
                           FilterKeyword {
                              id: FilterKeywordId("filter keyword id 1".to_string()),
                              keyword: Some("filter keyword 1".to_string()),
                              whole_word: Some(true),
                           },
                           FilterKeyword {
                              id: FilterKeywordId("filter keyword id 2".to_string()),
                              keyword: Some("filter keyword 2".to_string()),
                              whole_word: Some(true),
                           },
                        ],
                        statuses: vec![
                           FilterStatus {
                              id: FilterStatusId("filter status id 1".to_string()),
                              status_id: StatusId {
                                 instance_url: instance_cache.get().url.clone(),
                                 local: StatusLocalId("filtered status id 1".to_string())
                              },
                           },
                           FilterStatus {
                              id: FilterStatusId("filter status id 2".to_string()),
                              status_id: StatusId {
                                 instance_url: instance_cache.get().url.clone(),
                                 local: StatusLocalId("filtered status id 2".to_string())
                              },
                           },
                        ],
                     }),
                     keyword_matches: vec!["matched keyword 1".to_string()],
                     status_matches: vec![
                        StatusId {
                           instance_url: instance_cache.get().url.clone(),
                           local: StatusLocalId("matched status id 1".to_string())
                        }
                     ],
                  },
                  FilterResult {
                     filter: Some(Filter {
                        id: FilterId("filter id 2".to_string()),
                        title: Some("filter title 2".to_string()),
                        context: vec![FilterContext("public".to_string())],
                        expire_time: Some(Utc.with_ymd_and_hms(2000, 1, 13, 0, 0, 0).unwrap()),
                        filter_action: Some(FilterAction("warn".to_string())),
                        keywords: vec![
                           FilterKeyword {
                              id: FilterKeywordId("filter keyword id 3".to_string()),
                              keyword: Some("filter keyword 3".to_string()),
                              whole_word: Some(true),
                           },
                           FilterKeyword {
                              id: FilterKeywordId("filter keyword id 4".to_string()),
                              keyword: Some("filter keyword 4".to_string()),
                              whole_word: Some(true),
                           },
                        ],
                        statuses: vec![
                           FilterStatus {
                              id: FilterStatusId("filter status id 3".to_string()),
                              status_id: StatusId {
                                 instance_url: instance_cache.get().url.clone(),
                                 local: StatusLocalId("filtered status id 3".to_string())
                              },
                           },
                           FilterStatus {
                              id: FilterStatusId("filter status id 4".to_string()),
                              status_id: StatusId {
                                 instance_url: instance_cache.get().url.clone(),
                                 local: StatusLocalId("filtered status id 4".to_string())
                              },
                           },
                        ],
                     }),
                     keyword_matches: vec!["matched keyword 2".to_string()],
                     status_matches: vec![
                        StatusId {
                           instance_url: instance_cache.get().url.clone(),
                           local: StatusLocalId("matched status id 2".to_string())
                        }
                     ],
                  },
               ],
            },
            Status {
               id: StatusId {
                  instance_url: instance_cache.get().url.clone(),
                  local: StatusLocalId("status id 2".to_string()),
               },
               no_credential: {
                  let id = StatusId {
                     instance_url: instance_cache.get().url.clone(),
                     local: StatusLocalId("status id 2".to_string()),
                  };

                  assert_eq!(
                     NoCredentialStatus {
                        id: id.clone(),
                        uri: None,
                        created_time: None,
                        account: None,
                        content: None,
                        visibility: None,
                        is_sensitive: None,
                        spoiler_text: None,
                        media_attachments: vec![],
                        application: None,
                        mentions: vec![],
                        hashtags: vec![],
                        emojis: vec![],
                        boost_count: None,
                        favorite_count: None,
                        reply_count: None,
                        url: None,
                        replied_status_id: None,
                        replied_account_id: None,
                        boosted_status: None,
                        poll: None,
                        card: None,
                        language: None,
                        text: None,
                        edited_time: None,
                     },
                     *statuses[1].no_credential.get()
                  );

                  cache::status::no_credential_status_repo()
                     .read().unwrap()
                     .load(id).unwrap()
               },
               boosted_status: None,
               poll: None,
               is_favorited: None,
               is_boosted: None,
               is_muted: None,
               is_bookmarked: None,
               is_pinned: None,
               filter_results: vec![],
            },
         ],
         statuses
      );
   }
}
