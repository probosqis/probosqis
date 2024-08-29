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
use url::Url;

/// since 0.1.0
///
/// * `force_login` - since 2.6.0
/// * `lang` - since 3.5.0
pub fn get_authorize_url(
   instance_base_url: &Url,
   response_type: &str,
   client_id: &str,
   redirect_uri: &str,
   scope: Option<&str>,
   force_login: Option<bool>,
   lang: Option<&str>,
) -> Result<Url> {
   let mut url = instance_base_url.join("oauth/authorize")?;

   {
      let mut query_pairs = url.query_pairs_mut();
      query_pairs.append_pair("response_type", response_type);
      query_pairs.append_pair("client_id", client_id);
      query_pairs.append_pair("redirect_uri", redirect_uri);
      if let Some(scope) = scope {
         query_pairs.append_pair("scope", scope);
      }
      if let Some(force_login) = force_login {
         query_pairs.append_pair("force_login", &force_login.to_string());
      }
      if let Some(lang) = lang {
         query_pairs.append_pair("lang", lang);
      }
   }

   Ok(url)
}
