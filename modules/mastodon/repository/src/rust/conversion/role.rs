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
use mastodon_entity::role::Role;
use mastodon_webapi::entity::role::Role as ApiRole;
use panoptiqon::cache::Cache;

pub fn from_api(
   instance: Cache<Instance>,
   entity: ApiRole
) -> anyhow::Result<Role> {
   use mastodon_entity::role::RoleId;

   let ApiRole { id, name, color, permissions, highlighted } = entity;

   let role = Role {
      instance,
      id: id.map(|id| RoleId(id)),
      name,
      color,
      permissions,
      is_highlighted: highlighted
   };

   Ok(role)
}
