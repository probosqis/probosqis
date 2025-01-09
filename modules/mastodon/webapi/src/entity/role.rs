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
use serde::Deserialize;

/// since mastodon 4.0.0
#[derive(Deserialize)]
pub struct Role {
   pub id: Option<String>,
   pub name: Option<String>,
   pub color: Option<String>,
   pub permissions: Option<String>,
   pub highlighted: Option<bool>
}
