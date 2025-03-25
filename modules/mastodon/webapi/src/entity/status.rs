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

use serde::Deserialize;
use crate::entity::account::Account;
use crate::entity::application::Application;
use crate::entity::custom_emoji::CustomEmoji;
use crate::entity::filter::FilterResult;
use crate::entity::media_attachment::MediaAttachment;
use crate::entity::poll::Poll;
use crate::entity::preview_card::PreviewCard;

#[derive(Deserialize)]
pub struct Status {
   /// since mastodon 0.1.0
   pub id: Option<String>,
   /// since mastodon 0.1.0
   pub uri: Option<String>,
   /// since mastodon 0.1.0
   pub created_at: Option<String>,
   /// since mastodon 0.1.0
   pub account: Option<Account>,
   /// since mastodon 0.1.0
   pub content: Option<String>,
   /// since mastodon 0.9.9
   pub visibility: Option<String>,
   /// since mastodon 0.9.9
   pub sensitive: Option<bool>,
   /// since mastodon 1.0.0
   pub spoiler_text: Option<String>,
   /// since mastodon 0.6.0
   pub media_attachments: Option<Vec<MediaAttachment>>,
   /// since mastodon 0.9.9
   pub application: Option<Application>,
   /// since mastodon 0.6.0
   pub mentions: Option<Vec<StatusMention>>,
   /// since mastodon 0.6.0
   pub tags: Option<Vec<StatusTag>>,
   /// since mastodon 2.0.0
   pub emojis: Option<Vec<CustomEmoji>>,
   /// since mastodon 0.1.0
   pub reblogs_count: Option<i64>,
   /// since mastodon 0.1.0
   pub favourites_count: Option<i64>,
   /// since mastodon 2.5.0
   pub replies_count: Option<i64>,
   /// since mastodon 0.1.0
   pub url: Option<String>,
   /// since mastodon 0.1.0
   pub in_reply_to_id: Option<String>,
   /// since mastodon 0.1.0
   pub in_reply_to_account_id: Option<String>,
   /// since mastodon 0.1.0
   pub reblog: Option<Box<Status>>,
   /// since mastodon 2.8.0
   pub poll: Option<Poll>,
   /// since mastodon 2.6.0
   pub card: Option<PreviewCard>,
   /// since mastodon 1.4.0
   pub language: Option<String>,
   /// since mastodon 2.9.0
   pub text: Option<String>,
   /// since mastodon 3.5.0
   pub edited_at: Option<String>,
   /// since mastodon 0.1.0
   /// ユーザー認可済みのアクセストークンで取得した場合のみ
   pub favourited: Option<bool>,
   /// since mastodon 0.1.0
   /// ユーザー認可済みのアクセストークンで取得した場合のみ
   pub reblogged: Option<bool>,
   /// since mastodon 1.4.0
   /// ユーザー認可済みのアクセストークンで取得した場合のみ
   pub muted: Option<bool>,
   /// since mastodon 3.1.0
   /// ユーザー認可済みのアクセストークンで取得した場合のみ
   pub bookmarked: Option<bool>,
   /// since mastodon 1.6.0
   /// ユーザー認可済みのアクセストークンで取得した場合、
   /// かつStatusがピン留め可能な場合のみ
   pub pinned: Option<bool>,
   /// since mastodon 4.0.0
   /// ユーザー認可済みのアクセストークンで取得した場合のみ
   pub filtered: Option<Vec<FilterResult>>,
}

#[derive(Deserialize)]
pub struct StatusMention {
   /// since mastodon 0.6.0
   pub id: Option<String>,
   /// since mastodon 0.6.0
   pub username: Option<String>,
   /// since mastodon 0.6.0
   pub url: Option<String>,
   /// since mastodon 0.6.0
   pub acct: Option<String>,
}

#[derive(Deserialize)]
pub struct StatusTag {
   /// since mastodon 0.9.0
   pub name: Option<String>,
   /// since mastodon 0.9.0
   pub url: Option<String>,
}
