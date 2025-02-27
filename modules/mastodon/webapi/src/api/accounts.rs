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

use reqwest::blocking::Client;
use url::Url;
use crate::entity::account::Account;

#[cfg(feature = "mock")]
use std::cell::RefCell;

thread_local! {
   #[cfg(feature = "mock")]
   static GET_VERIFY_CREDENTIALS: RefCell<Box<dyn Fn(&Client, &Url, &str) -> anyhow::Result<Account>>>
      = RefCell::new(Box::new(|_, _, _| panic!()));
}

pub fn get_verify_credentials(
   client: &Client,
   instance_base_url: &Url,
   access_token: &str
) -> anyhow::Result<Account> {
   #[cfg(not(feature = "mock"))]
   {
      let url = instance_base_url.join("api/v1/accounts/verify_credentials")?;

      let account = client
         .get(url)
         .bearer_auth(access_token)
         .send()?
         .json()?;

      Ok(account)
   }

   #[cfg(feature = "mock")]
   {
      GET_VERIFY_CREDENTIALS.with(|f| {
         let f = f.borrow();
         f(client, instance_base_url, access_token)
      })
   }
}

#[allow(dead_code)]
#[cfg(feature = "mock")]
pub fn inject_get_verify_credentials(
   get_verify_credentials: impl Fn(&Client, &Url, &str) -> anyhow::Result<Account> + 'static
) {
   GET_VERIFY_CREDENTIALS.set(Box::new(get_verify_credentials));
}
