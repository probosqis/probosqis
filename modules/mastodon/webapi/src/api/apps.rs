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
use crate::entity::application::Application;

#[cfg(feature = "mock")]
use std::cell::RefCell;

thread_local! {
   #[cfg(feature = "mock")]
   static POST_APPS_V0: RefCell<Box<dyn Fn(&Client, &Url, &str, &str, Option<&str>, Option<&str>) -> anyhow::Result<Application>>>
      = RefCell::new(Box::new(|_, _, _, _, _, _| panic!()));

   #[cfg(feature = "mock")]
   static POST_APPS_V4_3_0: RefCell<Box<dyn Fn(&Client, &Url, &str, &[&str], Option<&str>, Option<&str>) -> anyhow::Result<Application>>>
      = RefCell::new(Box::new(|_, _, _, _, _, _| panic!()));
}

/// since mastodon 0.0.0
pub fn post_apps_v0(
   client: &Client,
   instance_base_url: &Url,
   client_name: &str,
   redirect_uris: &str,
   scopes: Option<&str>,
   website: Option<&str>
) -> anyhow::Result<Application> {
   #[cfg(not(feature = "mock"))]
   {
      use std::collections::HashMap;

      let url = instance_base_url.join("api/v1/apps")?;

      let mut form = HashMap::new();
      form.insert("client_name", client_name);
      form.insert("redirect_uris", redirect_uris);
      if let Some(scopes) = scopes {
         form.insert("scopes", scopes);
      }
      if let Some(website) = website {
         form.insert("website", website);
      }

      let application = client
         .post(url)
         .form(&form)
         .send()?
         .json()?;

      Ok(application)
   }

   #[cfg(feature = "mock")]
   {
      POST_APPS_V0.with(|f| {
         let f = f.borrow();
         f(client, instance_base_url, client_name, redirect_uris, scopes, website)
      })
   }
}

/// since mastodon 4.3.0
///
/// redirect_urisが配列を受け付けるように変更されたもの
pub fn post_apps_v4_3_0(
   client: &Client,
   instance_base_url: &Url,
   client_name: &str,
   redirect_uris: &[&str],
   scopes: Option<&str>,
   website: Option<&str>
) -> anyhow::Result<Application> {
   #[cfg(not(feature = "mock"))]
   {
      let url = instance_base_url.join("api/v1/apps")?;

      let mut form = Vec::new();
      form.push(("client_name", client_name));

      for u in redirect_uris {
         form.push(("redirect_uris[]", u));
      }

      if let Some(scopes) = scopes {
         form.push(("scopes", scopes));
      }
      if let Some(website) = website {
         form.push(("website", website));
      }

      let application = client
         .post(url)
         .form(&form)
         .send()?
         .json()?;

      Ok(application)
   }

   #[cfg(feature = "mock")]
   {
      POST_APPS_V4_3_0.with(|f| {
         let f = f.borrow();
         f(client, instance_base_url, client_name, redirect_uris, scopes, website)
      })
   }
}

#[allow(dead_code)]
#[cfg(feature = "mock")]
pub fn inject_post_apps_v0(
   post_app_v0: impl Fn(&Client, &Url, &str, &str, Option<&str>, Option<&str>) -> anyhow::Result<Application> + 'static
) {
   POST_APPS_V0.set(Box::new(post_app_v0));
}

#[allow(dead_code)]
#[cfg(feature = "mock")]
pub fn inject_post_apps_v4_3_0(
   post_app_v4_3_0: impl Fn(&Client, &Url, &str, &[&str], Option<&str>, Option<&str>) -> anyhow::Result<Application> + 'static
) {
   POST_APPS_V4_3_0.set(Box::new(post_app_v4_3_0));
}
