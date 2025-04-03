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
   use ext_panoptiqon::repository_holder::RepositoryHolder;
   use mastodon_entity::instance::Instance;

   static REPO: RepositoryHolder<Instance> = RepositoryHolder::new();

   pub fn repo() -> &'static RepositoryHolder<Instance> {
      &REPO
   }
}

pub mod account {
   use ext_panoptiqon::repository_holder::RepositoryHolder;
   use mastodon_entity::account::Account;

   pub type Repository = panoptiqon::repository::Repository<Account>;

   static REPO: RepositoryHolder<Account> = RepositoryHolder::new();

   pub fn repo() -> &'static RepositoryHolder<Account> {
      &REPO
   }
}

pub mod account_icon {
   use entity::image_bytes::ImageBytes;
   use ext_panoptiqon::repository_holder::RepositoryHolder;

   static REPO: RepositoryHolder<ImageBytes> = RepositoryHolder::new();

   pub fn repo() -> &'static RepositoryHolder<ImageBytes> {
      &REPO
   }
}

pub mod poll {
   use ext_panoptiqon::repository_holder::RepositoryHolder;
   use mastodon_entity::poll::NoCredentialPoll;

   pub type NoCredentialPollRepository
      = panoptiqon::repository::Repository<NoCredentialPoll>;

   static NO_CREDENTIAL_POLL_REPO: RepositoryHolder<NoCredentialPoll>
      = RepositoryHolder::new();

   pub fn no_credential_poll_repo() -> &'static RepositoryHolder<NoCredentialPoll> {
      &NO_CREDENTIAL_POLL_REPO
   }
}

pub mod status {
   use ext_panoptiqon::repository_holder::RepositoryHolder;
   use mastodon_entity::status::{NoCredentialStatus, Status};

   pub type StatusRepository = panoptiqon::repository::Repository<Status>;

   pub type NoCredentialStatusRepository
      = panoptiqon::repository::Repository<NoCredentialStatus>;

   static REPO: RepositoryHolder<Status> = RepositoryHolder::new();

   static NO_CREDENTIAL_REPO: RepositoryHolder<NoCredentialStatus>
      = RepositoryHolder::new();

   pub fn status_repo() -> &'static RepositoryHolder<Status> {
      &REPO
   }

   pub fn no_credential_status_repo(
   ) -> &'static RepositoryHolder<NoCredentialStatus> {
      &NO_CREDENTIAL_REPO
   }
}
