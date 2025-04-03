/*
 * Copyright 2024-2025 wcaokaze
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

/// since mastodon 0.9.9
#[derive(Deserialize)]
pub struct Application {
   /// since mastodon 0.9.9
   pub name: String,
   /// since mastodon 0.9.9
   /// mastodon 3.5.1以降nullable
   pub website: Option<String>,
   /// since mastodon 4.3.0
   pub scopes: Option<Vec<String>>,
   /// since mastodon 4.3.0
   pub redirect_uris: Option<Vec<String>>,
   /// since mastodon 0.0.0
   /// mastodon 4.3.0以降非推奨(redirect_uris推奨)
   pub redirect_uri: Option<String>,
   /// since mastodon 2.8.0
   /// mastodon 4.3.0以降非推奨(Instance.configuration.vapid.public_key推奨)
   pub vapid_key: Option<Vec<String>>,
   /// since mastodon 0.9.9
   /// mastodon 4.3.0以降CredentialApplicationの場合のみ
   pub client_id: Option<String>,
   /// since mastodon 0.9.9
   /// mastodon 4.3.0以降CredentialApplicationの場合のみ
   pub client_secret: Option<String>,
   /// since mastodon 4.3.0
   pub client_secret_expires_at: Option<String>,
}
