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

pub fn get_verify_credentials(
   client: &Client,
   instance_base_url: &Url,
   access_token: &str
) -> anyhow::Result<Account> {
   let url = instance_base_url.join("api/v1/accounts/verify_credentials")?;

   let account = client
      .get(url)
      .bearer_auth(access_token)
      .send()?
      .json()?;

   Ok(account)
}
