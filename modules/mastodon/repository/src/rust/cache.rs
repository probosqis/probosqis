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

pub mod instance {
   use url::Url;
   use ext_panoptiqon::repository_holder::RepositoryHolder;
   use mastodon_entity::instance::Instance;

   pub type Key = Url;
   pub type Repository = panoptiqon::repository::Repository<Url, Instance>;

   static REPO: RepositoryHolder<Url, Instance> = RepositoryHolder::new(
      |i| i.url.clone()
   );

   pub fn repo() -> &'static RepositoryHolder<Url, Instance> {
      &REPO
   }
}

pub mod account {
   use url::Url;
   use ext_panoptiqon::repository_holder::RepositoryHolder;
   use mastodon_entity::account::{Account, AccountId};

   pub type Key = (Url, AccountId);
   pub type Repository = panoptiqon::repository::Repository<Key, Account>;

   static REPO: RepositoryHolder<Key, Account> = RepositoryHolder::new(
      |a| (a.instance.read().unwrap().url.clone(), a.id.clone())
   );

   pub fn repo() -> &'static RepositoryHolder<Key, Account> {
      &REPO
   }
}
