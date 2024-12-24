/*
 * Copyright 2024 wcaokaze
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
use anyhow::Result;
use mastodon_entity::application::Application;
use mastodon_webapi::entity::application::Application as ApiApplication;

use mastodon_entity::instance::Instance;
use panoptiqon::cache::Cache;

pub fn from_api(
   entity: ApiApplication,
   instance_cache: Cache<Instance>
) -> Result<Application> {
   let ApiApplication { name, website, client_id, client_secret } = entity;

   let application = Application {
      instance: instance_cache,
      name, website, client_id, client_secret
   };

   Ok(application)
}
