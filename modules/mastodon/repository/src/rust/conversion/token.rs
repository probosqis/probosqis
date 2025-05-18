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
use anyhow::Result;
use chrono::DateTime;
use mastodon_entity::account::CredentialAccount;
use mastodon_entity::instance::Instance;
use mastodon_entity::token::Token;
use mastodon_webapi::entity::token::Token as ApiToken;
use panoptiqon::cache::Cache;

pub fn from_api(
   entity: ApiToken,
   instance_cache: Cache<Instance>,
   credential_account_cache: Cache<CredentialAccount>
) -> Result<Token> {
   let ApiToken { access_token, token_type, scope, created_at } = entity;
   
   let account_id = credential_account_cache.get().id.clone();

   let token = Token {
      instance: instance_cache, account: Some(credential_account_cache),
      account_id, access_token, token_type, scope,
      created_at: DateTime::from_timestamp(created_at, 0).unwrap()
   };

   Ok(token)
}
