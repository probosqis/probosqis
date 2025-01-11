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
use mastodon_entity::status::StatusVisibility;

pub fn visibility_from_api(entity: String) -> anyhow::Result<StatusVisibility> {
   match entity.as_str() {
      "public"   => Ok(StatusVisibility::Public),
      "unlisted" => Ok(StatusVisibility::Unlisted),
      "private"  => Ok(StatusVisibility::Private),
      "direct"   => Ok(StatusVisibility::Direct),
      _          => Err(anyhow::format_err!("unknown visibility: {}", entity)),
   }
}
