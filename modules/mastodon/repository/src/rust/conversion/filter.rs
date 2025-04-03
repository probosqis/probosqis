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

use mastodon_entity::filter::{Filter, FilterKeyword, FilterResult, FilterStatus};
use mastodon_webapi::entity::filter::{
   Filter as ApiFilter,
   FilterKeyword as ApiFilterKeyword,
   FilterResult as ApiFilterResult,
   FilterStatus as ApiFilterStatus,
};
use url::Url;

pub fn from_api(
   instance_url: Url,
   entity: ApiFilter
) -> anyhow::Result<Filter> {
   use anyhow::Context;
   use chrono::DateTime;
   use mastodon_entity::filter::{FilterAction, FilterContext, FilterId};

   let ApiFilter {
      id, title, context, expires_at, filter_action, keywords, statuses
   } = entity;

   let filter = Filter {
      id: FilterId(id.context("No filter ID")?),
      title,
      context: context.into_iter()
         .flatten()
         .map(|mut c| {
            c.make_ascii_lowercase();
            FilterContext(c)
         })
         .collect(),
      expire_time: expires_at
         .and_then(|time| DateTime::parse_from_rfc3339(&time).ok())
         .map(|time| time.to_utc()),
      filter_action: filter_action.map(|mut a| {
         a.make_ascii_lowercase();
         FilterAction(a)
      }),
      keywords: keywords.into_iter()
         .flatten()
         .flat_map(filter_keyword_from_api)
         .collect(),
      statuses: statuses.into_iter()
         .flatten()
         .flat_map(|s| filter_status_from_api(instance_url.clone(), s))
         .collect(),
   };

   Ok(filter)
}

pub fn filter_keyword_from_api(
   entity: ApiFilterKeyword
) -> anyhow::Result<FilterKeyword> {
   use anyhow::Context;
   use mastodon_entity::filter::FilterKeywordId;

   let ApiFilterKeyword { id, keyword, whole_word } = entity;

   let filter_keyword = FilterKeyword {
      id: FilterKeywordId(id.context("No filter keyword ID")?),
      keyword,
      whole_word,
   };

   Ok(filter_keyword)
}

pub fn filter_status_from_api(
   instance_url: Url,
   entity: ApiFilterStatus
) -> anyhow::Result<FilterStatus> {
   use anyhow::Context;
   use mastodon_entity::filter::FilterStatusId;
   use mastodon_entity::status::{StatusId, StatusLocalId};

   let ApiFilterStatus { id, status_id } = entity;

   let filter_status = FilterStatus {
      id: FilterStatusId(id.context("No filter status ID")?),
      status_id: StatusId {
         instance_url,
         local: StatusLocalId(status_id.context("No filtered status ID")?)
      },
   };

   Ok(filter_status)
}

pub fn filter_result_from_api(
   instance_url: Url,
   entity: ApiFilterResult
) -> anyhow::Result<FilterResult> {
   use mastodon_entity::status::{StatusId, StatusLocalId};

   let ApiFilterResult { filter, keyword_matches, status_matches } = entity;

   let filter_result = FilterResult {
      filter: filter.and_then(|filter| from_api(instance_url.clone(), filter).ok()),
      keyword_matches: keyword_matches.unwrap_or(vec![]),
      status_matches: status_matches.unwrap_or(vec![]).into_iter()
         .map(|id|
            StatusId {
               instance_url: instance_url.clone(),
               local: StatusLocalId(id)
            }
         )
         .collect(),
   };

   Ok(filter_result)
}
