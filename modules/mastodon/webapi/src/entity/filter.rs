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

/// since mastodon 4.0.0
#[derive(Deserialize)]
pub struct Filter {
   /// since mastodon 4.0.0
   pub id: Option<String>,
   /// since mastodon 4.0.0
   pub title: Option<String>,
   /// since mastodon 4.0.0
   pub context: Option<Vec<String>>,
   /// since mastodon 4.0.0
   pub expires_at: Option<String>,
   /// since mastodon 4.0.0
   pub filter_action: Option<String>,
   /// since mastodon 4.0.0
   pub keywords: Option<Vec<FilterKeyword>>,
   /// since mastodon 4.0.0
   pub statuses: Option<Vec<FilterStatus>>,
}

/// since mastodon 4.0.0
#[derive(Deserialize)]
pub struct FilterKeyword {
   /// since mastodon 4.0.0
   pub id: Option<String>,
   /// since mastodon 4.0.0
   pub keyword: Option<String>,
   /// since mastodon 4.0.0
   pub whole_word: Option<bool>,
}

/// since mastodon 4.0.0
#[derive(Deserialize)]
pub struct FilterStatus {
   /// since mastodon 4.0.0
   pub id: Option<String>,
   /// since mastodon 4.0.0
   pub status_id: Option<String>,
}

/// since mastodon 4.0.0
#[derive(Deserialize)]
pub struct FilterResult {
   /// since mastodon 4.0.0
   pub filter: Option<Filter>,
   /// since mastodon 4.0.0
   pub keyword_matches: Option<Vec<String>>,
   /// since mastodon 4.0.0
   pub status_matches: Option<Vec<String>>,
}
