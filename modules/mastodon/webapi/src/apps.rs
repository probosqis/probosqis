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
use ureq::Agent;
use url::Url;

use mastodon_entity::application::Application;

pub fn post_apps<'a>(
   agent: &Agent,
   instance_base_url: &Url,
   client_name: &str,
   redirect_uris: &str,
   scopes: Option<&str>,
   website: Option<&str>
) -> Result<Application> {
   let url = instance_base_url.join("api/v1/apps")?;

   let form = filter_some([
      ("client_name", Some(client_name)),
      ("redirect_uris", Some(redirect_uris)),
      ("scopes", scopes),
      ("website", website),
   ]);

   let application = agent
      .post(url.as_str())
      .send_form(&form)?
      .into_json()?;

   Ok(application)
}

#[inline]
fn filter_some<'a, const N: usize>(
   options: [(&'a str, Option<&'a str>); N]
) -> Vec<(&'a str, &'a str)> {
   let mut result = Vec::with_capacity(N);
   for (k, o) in options {
      if let Some(v) = o {
         result.push((k, v));
      }
   }
   result
}
