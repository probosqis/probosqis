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
use mastodon_entity::preview_card::{PreviewCard, PreviewCardAuthor};
use panoptiqon::cache::Cache;
use crate::cache;

use mastodon_webapi::entity::preview_card::{
   PreviewCard as ApiPreviewCard,
   PreviewCardAuthor as ApiPreviewCardAuthor,
};

#[cfg(feature = "jvm")]
use jni::JNIEnv;

pub fn from_api(
   #[cfg(feature = "jvm")] env: &mut JNIEnv,
   instance: Cache<Instance>,
   entity: ApiPreviewCard,
   account_cache_repository: &mut cache::account::Repository
) -> anyhow::Result<PreviewCard> {
   let ApiPreviewCard {
      url, title, description, r#type, authors, author_name, author_url,
      provider_name, provider_url, html, width, height, image, embed_url, blurhash,
   } = entity;

   let authors = match authors {
      Some(authors) => {
         authors.into_iter()
            .flat_map(|a|
               card_author_from_api(
                  #[cfg(feature = "jvm")] env,
                  instance.clone(), a, account_cache_repository
               )
            )
            .collect()
      },
      None => {
         vec![
            PreviewCardAuthor {
               name: author_name,
               url: author_url.and_then(|u| u.parse().ok()),
               account: None,
            }
         ]
      }
   };

   let preview_card = PreviewCard {
      url: url.and_then(|u| u.parse().ok()),
      title,
      description,
      card_type: r#type,
      authors,
      provider_name,
      provider_url: provider_url.and_then(|u| u.parse().ok()),
      html,
      width,
      height,
      image_url: image.and_then(|u| u.parse().ok()),
      embed_url: embed_url.and_then(|u| u.parse().ok()),
      blurhash,
   };

   Ok(preview_card)
}

pub fn card_author_from_api(
   #[cfg(feature = "jvm")] env: &mut JNIEnv,
   instance: Cache<Instance>,
   entity: ApiPreviewCardAuthor,
   account_cache_repository: &mut cache::account::Repository
) -> anyhow::Result<PreviewCardAuthor> {
   use crate::conversion;

   let ApiPreviewCardAuthor { name, url, account } = entity;

   let account = account
      .and_then(|a|
         conversion::account::from_api(
            #[cfg(feature = "jvm")] env,
            instance,
            a,
            account_cache_repository
         ).ok()
      )
      .map(|a| account_cache_repository.save(a));

   let preview_card_author = PreviewCardAuthor {
      name,
      url: url.and_then(|u| u.parse().ok()),
      account,
   };

   Ok(preview_card_author)
}
