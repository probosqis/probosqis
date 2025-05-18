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

use mastodon_entity::instance::Instance;
use mastodon_entity::poll::{Poll, PollOption};
use panoptiqon::cache::Cache;
use crate::cache;

use mastodon_webapi::entity::poll::{
   Poll as ApiPoll,
   PollOption as ApiPollOption,
};

pub fn from_api(
   instance: Cache<Instance>,
   entity: ApiPoll,
   no_credential_poll_cache_repository: &mut cache::poll::NoCredentialPollRepository
) -> anyhow::Result<Poll> {
   use anyhow::Context;
   use chrono::DateTime;
   use mastodon_entity::poll::{NoCredentialPoll, PollId, PollLocalId};
   use crate::conversion;

   let ApiPoll {
      id, expires_at, expired, multiple, votes_count, voters_count, options,
      emojis, voted, own_voted,
   } = entity;

   let id = PollId {
      instance_url: instance.get().url.clone(),
      local: PollLocalId(id.context("No poll id")?)
   };

   let no_credential_poll = NoCredentialPoll {
      id: id.clone(),
      expire_time: expires_at
         .and_then(|time| DateTime::parse_from_rfc3339(&time).ok())
         .map(|time| time.to_utc()),
      is_expired: expired,
      allows_multiple_choices: multiple,
      vote_count: votes_count,
      voter_count: voters_count,
      poll_options: options.unwrap_or(vec![]).into_iter()
         .flat_map(|opt| poll_option_from_api(opt))
         .collect(),
      emojis: emojis.unwrap_or(vec![]).into_iter()
         .flat_map(|emj| conversion::custom_emoji::from_api(instance.clone(), emj))
         .collect(),
   };

   let no_credential_poll = no_credential_poll_cache_repository
      .save(no_credential_poll);

   let poll = Poll {
      id: id.clone(),
      no_credential: no_credential_poll,
      is_voted: voted,
      voted_options: own_voted.unwrap_or(vec![]),
   };

   Ok(poll)
}

pub fn poll_option_from_api(entity: ApiPollOption) -> anyhow::Result<PollOption> {
   let ApiPollOption { title, votes_count } = entity;

   let poll_option = PollOption {
      title,
      vote_count: votes_count,
   };

   Ok(poll_option)
}
