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

use reqwest::blocking::Client;
use url::Url;
use crate::entity::token::Token;

#[cfg(feature = "mock")]
use std::cell::RefCell;

thread_local! {
   #[cfg(feature = "mock")]
   static GET_AUTHORIZE_URL: RefCell<Box<dyn Fn(&Url, &str, &str, &str, Option<&str>, Option<bool>, Option<&str>) -> anyhow::Result<Url>>>
      = RefCell::new(Box::new(|_, _, _, _, _, _, _| panic!()));

   #[cfg(feature = "mock")]
   static POST_TOKEN: RefCell<Box<dyn Fn(&Client, &Url, &str, Option<&str>, &str, &str, &str, Option<&str>) -> anyhow::Result<Token>>>
      = RefCell::new(Box::new(|_, _, _, _, _, _, _, _| panic!()));
}

/// since mastodon 0.1.0
///
/// * `force_login` - since mastodon 2.6.0
/// * `lang` - since mastodon 3.5.0
pub fn get_authorize_url(
   instance_base_url: &Url,
   response_type: &str,
   client_id: &str,
   redirect_uri: &str,
   scope: Option<&str>,
   force_login: Option<bool>,
   lang: Option<&str>,
) -> anyhow::Result<Url> {
   #[cfg(not(feature = "mock"))]
   {
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

   #[cfg(feature = "mock")]
   {
      GET_AUTHORIZE_URL.with(|f| {
         let f = f.borrow();
         f(instance_base_url, response_type, client_id, redirect_uri, scope, force_login, lang)
      })
   }
}

/// since mastodon 0.1.0
pub fn post_token(
   client: &Client,
   instance_base_url: &Url,
   grant_type: &str,
   code: Option<&str>,
   client_id: &str,
   client_secret: &str,
   redirect_uri: &str,
   scope: Option<&str>,
) -> anyhow::Result<Token> {
   #[cfg(not(feature = "mock"))]
   {
      use std::collections::HashMap;

      let url = instance_base_url.join("oauth/token")?;

      let mut form = HashMap::new();
      form.insert("grant_type", grant_type);
      if let Some(code) = code {
         form.insert("code", code);
      }
      form.insert("client_id", client_id);
      form.insert("client_secret", client_secret);
      form.insert("redirect_uri", redirect_uri);
      if let Some(scope) = scope {
         form.insert("scope", scope);
      }

      let token = client
         .post(url)
         .form(&form)
         .send()?
         .json()?;

      Ok(token)
   }

   #[cfg(feature = "mock")]
   {
      POST_TOKEN.with(|f| {
         let f = f.borrow();
         f(client, instance_base_url, grant_type, code, client_id, client_secret, redirect_uri, scope)
      })
   }
}

#[allow(dead_code)]
#[cfg(feature = "mock")]
pub fn inject_get_authorize_url(
   get_authorize_url: impl Fn(&Url, &str, &str, &str, Option<&str>, Option<bool>, Option<&str>) -> anyhow::Result<Url> + 'static
) {
   GET_AUTHORIZE_URL.set(Box::new(get_authorize_url));
}

#[allow(dead_code)]
#[cfg(feature = "mock")]
pub fn inject_post_token(
   post_token: impl Fn(&Client, &Url, &str, Option<&str>, &str, &str, &str, Option<&str>) -> anyhow::Result<Token> + 'static
) {
   POST_TOKEN.set(Box::new(post_token));
}
