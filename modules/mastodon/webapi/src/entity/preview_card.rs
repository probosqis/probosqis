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

#[derive(Deserialize)]
pub struct PreviewCard {
   /// since mastodon 1.0.0
   pub url: Option<String>,
   /// since mastodon 1.0.0
   pub title: Option<String>,
   /// since mastodon 1.0.0
   pub description: Option<String>,
   /// since mastodon 1.3.0
   #[serde(rename = "type")]
   pub r#type: Option<String>,
   /// since mastodon 4.3.0
   pub authors: Option<Vec<PreviewCardAuthor>>,
   /// since mastodon 1.3.0
   /// mastodon 4.3.0以降非推奨(authors.name推奨)
   pub author_name: Option<String>,
   /// since mastodon 1.3.0
   /// mastodon 4.3.0以降非推奨(authors.url推奨)
   pub author_url: Option<String>,
   /// since mastodon 1.3.0
   pub provider_name: Option<String>,
   /// since mastodon 1.3.0
   pub provider_url: Option<String>,
   /// since mastodon 1.3.0
   pub html: Option<String>,
   /// since mastodon 1.3.0
   pub width: Option<i64>,
   /// since mastodon 1.3.0
   pub height: Option<i64>,
   /// since mastodon 1.0.0
   pub image: Option<String>,
   /// since mastodon 2.1.0
   pub embed_url: Option<String>,
   /// since mastodon 3.2.0
   pub blurhash: Option<String>,
}

#[derive(Deserialize)]
pub struct PreviewCardAuthor {
   /// since mastodon 4.3.0
   pub name: Option<String>,
   /// since mastodon 4.3.0
   pub url: Option<String>,
   /// since mastodon 4.3.0
   pub account: Option<Account>,
}
