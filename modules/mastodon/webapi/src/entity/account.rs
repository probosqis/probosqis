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
use crate::entity::custom_emoji::CustomEmoji;
use crate::entity::role::Role;

/// since mastodon 0.0.0
#[derive(Deserialize)]
pub struct Account {
   /// since mastodon 0.1.0
   pub id: Option<String>,
   /// since mastodon 0.1.0
   pub username: Option<String>,
   /// since mastodon 0.1.0
   pub acct: Option<String>,
   /// since mastodon 0.1.0
   pub url: Option<String>,
   /// since mastodon 0.1.0
   pub display_name: Option<String>,
   /// since mastodon 0.1.0
   pub note: Option<String>,
   /// since mastodon 0.1.0
   pub avatar: Option<String>,
   /// since mastodon 1.1.2
   pub avatar_static: Option<String>,
   /// since mastodon 0.1.0
   pub header: Option<String>,
   /// since mastodon 1.1.2
   pub header_static: Option<String>,
   /// since mastodon 0.1.0
   pub locked: Option<bool>,
   /// since mastodon 2.4.0
   pub fields: Option<Vec<AccountField>>,
   /// since mastodon 2.4.0
   pub emojis: Option<Vec<CustomEmoji>>,
   /// since mastodon 2.4.0
   pub bot: Option<bool>,
   /// since mastodon 3.1.0
   pub group: Option<bool>,
   /// since mastodon 3.1.0
   pub discoverable: Option<bool>,
   /// since mastodon 4.0.0
   pub noindex: Option<bool>,
   /// since mastodon 2.1.0
   pub moved: Option<Box<Account>>,
   /// since mastodon 3.3.0
   pub suspended: Option<bool>,
   /// since mastodon 3.5.3
   pub limited: Option<bool>,
   /// since mastodon 0.1.0
   /// mastodon 3.4.0以降、時刻は深夜0時固定
   pub created_at: Option<String>,
   /// since mastodon 3.0.0
   /// mastodon 3.1.0以降、時刻なしの日付のみ
   pub last_status_at: Option<String>,
   /// since mastodon 0.1.0
   pub statuses_count: Option<u64>,
   /// since mastodon 0.1.0
   pub followers_count: Option<u64>,
   /// since mastodon 0.1.0
   pub following_count: Option<u64>,
   /// since mastodon 2.4.0
   /// CredentialAccountの場合のみ
   pub source: Option<CredentialAccountSource>,
   /// since mastodon 4.0.0
   /// CredentialAccountの場合のみ
   pub role: Option<Role>,
   /// since mastodon 3.3.0
   /// ミュート中アカウントの場合のみ
   pub mute_expires_at: Option<String>,
}

/// since mastodon 2.4.0
#[derive(Deserialize)]
pub struct AccountField {
   pub name: Option<String>,
   pub value: Option<String>,
   /// since mastodon 2.6.0
   pub verified_at: Option<String>,
}

#[derive(Deserialize)]
pub struct CredentialAccountSource {
   /// since mastodon 1.5.0
   pub note: Option<String>,
   /// since mastodon 2.4.0
   pub fields: Option<Vec<AccountField>>,
   /// since mastodon 1.5.0
   pub privacy: Option<String>,
   /// since mastodon 1.5.0
   pub sensitive: Option<bool>,
   /// since mastodon 2.4.2
   pub language: Option<String>,
   /// since mastodon 3.0.0
   pub follow_requests_count: Option<u64>,
}
