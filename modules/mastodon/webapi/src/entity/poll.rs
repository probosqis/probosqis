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

/// since mastodon 2.8.0
#[derive(Deserialize)]
pub struct Poll {
   /// since mastodon 2.8.0
   pub id: Option<String>,
   /// since mastodon 2.8.0
   pub expires_at: Option<String>,
   /// since mastodon 2.8.0
   pub expired: Option<bool>,
   /// since mastodon 2.8.0
   pub multiple: Option<bool>,
   /// since mastodon 2.8.0
   pub votes_count: Option<i64>,
   /// since mastodon 2.8.0
   pub voters_count: Option<i64>,
   /// since mastodon 2.8.0
   pub options: Option<Vec<PollOption>>,
   /// since mastodon 2.8.0
   pub emojis: Option<Vec<CustomEmoji>>,
   /// since mastodon 2.8.0
   pub voted: Option<bool>,
   /// since mastodon 2.8.0
   pub own_voted: Option<Vec<i64>>,
}

/// since mastodon 2.8.0
#[derive(Deserialize)]
pub struct PollOption {
   /// since mastodon 2.8.0
   pub title: Option<String>,
   /// since mastodon 2.8.0
   pub votes_count: Option<i64>,
}
