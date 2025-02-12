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
use mastodon_entity::custom_emoji::CustomEmoji;
use mastodon_entity::instance::Instance;
use mastodon_webapi::entity::custom_emoji::CustomEmoji as ApiCustomEmoji;
use panoptiqon::cache::Cache;

pub fn from_api(
   instance: Cache<Instance>,
   entity: ApiCustomEmoji
) -> anyhow::Result<CustomEmoji> {
   use anyhow::Context;

   let ApiCustomEmoji {
      shortcode, url, static_url, visible_in_picker, category
   } = entity;

   let custom_emoji = CustomEmoji {
      instance,
      shortcode: shortcode.context("No custom_emoji shortcode")?,
      image_url: url.context("No custom_emoji url")
         .and_then(|url| url.parse().context("Url parse error"))?,
      static_image_url: static_url.and_then(|url| url.parse().ok()),
      is_visible_in_picker: visible_in_picker,
      category
   };

   Ok(custom_emoji)
}
