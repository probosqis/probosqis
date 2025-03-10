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

use chrono::DateTime;
use mastodon_entity::application::Application;
use mastodon_entity::instance::Instance;
use panoptiqon::cache::Cache;

use mastodon_webapi::entity::application::Application as ApiApplication;

pub fn from_api(
   entity: ApiApplication,
   instance_cache: Cache<Instance>
) -> anyhow::Result<Application> {
   let ApiApplication {
      name, website, scopes, redirect_uris, redirect_uri, vapid_key: _,
      client_id, client_secret, client_secret_expires_at,
   } = entity;

   let redirect_uris = redirect_uris
      .or_else(||
         redirect_uri.map(|uri_lines|
            uri_lines.lines().map(|str| str.to_string()).collect()
         )
      )
      .unwrap_or(vec![]);

   let application = Application {
      instance: instance_cache,
      name,
      website: website.and_then(|url| url.parse().ok()),
      scopes: scopes.unwrap_or(vec![]),
      redirect_uris,
      client_id,
      client_secret,
      client_secret_expire_time: client_secret_expires_at
         .and_then(|time| DateTime::parse_from_rfc3339(&time).ok())
         .map(|time| time.to_utc()),
   };

   Ok(application)
}
