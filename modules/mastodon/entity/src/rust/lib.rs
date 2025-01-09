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

pub mod account;
pub mod application;
pub mod custom_emoji;
pub mod instance;
pub mod role;
pub mod status;
pub mod token;

#[cfg(feature="jni-test")]
mod jni_tests {
   use jni::JNIEnv;
   use jni::objects::JObject;
   use url::Url;
   use ext_panoptiqon::repository_holder::RepositoryHolder;
   use panoptiqon::cache::Cache;
   use crate::account::{Account, AccountId};
   use crate::instance::Instance;

   const fn new_instance_repo() -> RepositoryHolder<Url, Instance> {
      RepositoryHolder::new(|i| i.url.clone())
   }

   const fn new_account_repo() -> RepositoryHolder<(Url, AccountId), Account> {
      RepositoryHolder::new(|a| (a.instance.read().unwrap().url.clone(), a.id.clone()))
   }

   fn save_instance(
      env: &mut JNIEnv,
      instance_repo: &RepositoryHolder<Url, Instance>
   ) -> Cache<Instance> {
      use chrono::{TimeZone, Utc};

      let instance = Instance {
         url: "https://example.com/instance/url".parse().unwrap(),
         version: "version".to_string(),
         version_checked_time: Utc.with_ymd_and_hms(2000, 1, 1, 0, 0, 0).unwrap(),
      };

      instance_repo
         .write(env).unwrap()
         .save(instance)
   }

   fn save_account(
      env: &mut JNIEnv,
      account_repo: &RepositoryHolder<(Url, AccountId), Account>,
      instance_repo: &RepositoryHolder<Url, Instance>
   ) -> Cache<Account> {
      let account = Account {
         instance: save_instance(env, instance_repo),
         id: AccountId("account id".to_string()),
         username: None,
         acct: None,
         url: None,
         display_name: None,
         profile_note: None,
         avatar_image_url: None,
         avatar_static_image_url: None,
         header_image_url: None,
         header_static_image_url: None,
         is_locked: None,
         profile_fields: vec![],
         emojis_in_profile: vec![],
         is_bot: None,
         is_group: None,
         is_discoverable: None,
         is_noindex: None,
         moved_to: None,
         is_suspended: None,
         is_limited: None,
         created_time: None,
         last_status_post_time: None,
         status_count: None,
         follower_count: None,
         followee_count: None,
      };

      account_repo
         .write(env).unwrap()
         .save(account)
   }

   #[allow(non_upper_case_globals)]
   static account_toRust_instance_repo: RepositoryHolder<Url, Instance> = new_instance_repo();

   #[allow(non_upper_case_globals)]
   static account_toRust_account_repo: RepositoryHolder<(Url, AccountId), Account> = new_account_repo();

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJavaTest_account_1toRust_00024createInstance<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JObject<'local> {
      use panoptiqon::convert_java::ConvertJava;

      save_instance(&mut env, &account_toRust_instance_repo)
         .clone_into_java(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJavaTest_account_1toRust_00024createMovedTo<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JObject<'local> {
      use panoptiqon::convert_java::ConvertJava;

      save_account(&mut env, &account_toRust_account_repo, &account_toRust_instance_repo)
         .clone_into_java(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJavaTest_account_1toRust_00024assert(
      mut env: JNIEnv,
      _obj: JObject,
      account: JObject
   ) {
      use chrono::{TimeZone, Utc};
      use panoptiqon::convert_java::ConvertJava;
      use crate::account::AccountProfileField;
      use crate::custom_emoji::CustomEmoji;

      let account = Account::clone_from_java(&mut env, &account);

      let instance = save_instance(&mut env, &account_toRust_instance_repo);

      assert_eq!(
         Account {
            instance: instance.clone(),
            id: AccountId("id".to_string()),
            username: Some("username".to_string()),
            acct: Some("acct".to_string()),
            url: Some("https://example.com/url".parse().unwrap()),
            display_name: Some("displayName".to_string()),
            profile_note: Some("profileNote".to_string()),
            avatar_image_url: Some("https://example.com/avatar/image/url".parse().unwrap()),
            avatar_static_image_url: Some("https://example.com/avatar/static/image/url".parse().unwrap()),
            header_image_url: Some("https://example.com/header/image/url".parse().unwrap()),
            header_static_image_url: Some("https://example.com/header/static/image/url".parse().unwrap()),
            is_locked: Some(true),
            profile_fields: vec![
               AccountProfileField {
                  name: Some("name1".to_string()),
                  value: Some("value1".to_string()),
                  verified_time: Some(Utc.with_ymd_and_hms(2000, 1, 1, 0, 0, 0).unwrap()),
               },
               AccountProfileField {
                  name: Some("name2".to_string()),
                  value: Some("value2".to_string()),
                  verified_time: Some(Utc.with_ymd_and_hms(2000, 1, 2, 0, 0, 0).unwrap()),
               },
               AccountProfileField {
                  name: Some("name3".to_string()),
                  value: Some("value3".to_string()),
                  verified_time: Some(Utc.with_ymd_and_hms(2000, 1, 3, 0, 0, 0).unwrap()),
               },
               AccountProfileField {
                  name: Some("name4".to_string()),
                  value: Some("value4".to_string()),
                  verified_time: Some(Utc.with_ymd_and_hms(2000, 1, 4, 0, 0, 0).unwrap()),
               },
               AccountProfileField {
                  name: Some("name5".to_string()),
                  value: Some("value5".to_string()),
                  verified_time: Some(Utc.with_ymd_and_hms(2000, 1, 5, 0, 0, 0).unwrap()),
               },
            ],
            emojis_in_profile: vec![
               CustomEmoji {
                  instance: instance.clone(),
                  shortcode: "shortcode1".to_string(),
                  image_url: "https://example.com/image/url/1".parse().unwrap(),
                  static_image_url: Some("https://example.com/static/image/url/1".parse().unwrap()),
                  is_visible_in_picker: Some(true),
                  category: Some("category1".to_string()),
               },
               CustomEmoji {
                  instance: instance.clone(),
                  shortcode: "shortcode2".to_string(),
                  image_url: "https://example.com/image/url/2".parse().unwrap(),
                  static_image_url: Some("https://example.com/static/image/url/2".parse().unwrap()),
                  is_visible_in_picker: Some(false),
                  category: Some("category2".to_string()),
               },
            ],
            is_bot: Some(true),
            is_group: Some(true),
            is_discoverable: Some(true),
            is_noindex: Some(true),
            is_suspended: Some(true),
            is_limited: Some(true),
            created_time: Some(Utc.with_ymd_and_hms(2000, 1, 1, 0, 0, 0).unwrap()),
            last_status_post_time: Some(Utc.with_ymd_and_hms(2000, 2, 1, 0, 0, 0).unwrap()),
            status_count: Some(10000),
            follower_count: Some(100),
            followee_count: Some(200),
            moved_to: Some(
               save_account(&mut env, &account_toRust_account_repo, &account_toRust_instance_repo)
            ),
         },
         account
      );
   }

   #[allow(non_upper_case_globals)]
   static account_fromRust_instance_repo: RepositoryHolder<Url, Instance> = new_instance_repo();

   #[allow(non_upper_case_globals)]
   static account_fromRust_account_repo: RepositoryHolder<(Url, AccountId), Account> = new_account_repo();

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJavaTest_account_1fromRust_00024createAccount<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JObject<'local> {
      use chrono::{TimeZone, Utc};
      use panoptiqon::convert_java::ConvertJava;
      use crate::account::AccountProfileField;
      use crate::custom_emoji::CustomEmoji;

      let instance = save_instance(&mut env, &account_fromRust_instance_repo);

      let account = Account {
         instance: instance.clone(),
         id: AccountId("id".to_string()),
         username: Some("username".to_string()),
         acct: Some("acct".to_string()),
         url: Some("https://example.com/url".parse().unwrap()),
         display_name: Some("displayName".to_string()),
         profile_note: Some("profileNote".to_string()),
         avatar_image_url: Some("https://example.com/avatar/image/url".parse().unwrap()),
         avatar_static_image_url: Some("https://example.com/avatar/static/image/url".parse().unwrap()),
         header_image_url: Some("https://example.com/header/image/url".parse().unwrap()),
         header_static_image_url: Some("https://example.com/header/static/image/url".parse().unwrap()),
         is_locked: Some(true),
         profile_fields: vec![
            AccountProfileField {
               name: Some("name1".to_string()),
               value: Some("value1".to_string()),
               verified_time: Some(Utc.with_ymd_and_hms(2000, 1, 1, 0, 0, 0).unwrap()),
            },
            AccountProfileField {
               name: Some("name2".to_string()),
               value: Some("value2".to_string()),
               verified_time: Some(Utc.with_ymd_and_hms(2000, 1, 2, 0, 0, 0).unwrap()),
            },
            AccountProfileField {
               name: Some("name3".to_string()),
               value: Some("value3".to_string()),
               verified_time: Some(Utc.with_ymd_and_hms(2000, 1, 3, 0, 0, 0).unwrap()),
            },
            AccountProfileField {
               name: Some("name4".to_string()),
               value: Some("value4".to_string()),
               verified_time: Some(Utc.with_ymd_and_hms(2000, 1, 4, 0, 0, 0).unwrap()),
            },
            AccountProfileField {
               name: Some("name5".to_string()),
               value: Some("value5".to_string()),
               verified_time: Some(Utc.with_ymd_and_hms(2000, 1, 5, 0, 0, 0).unwrap()),
            },
         ],
         emojis_in_profile: vec![
            CustomEmoji {
               instance: instance.clone(),
               shortcode: "shortcode1".to_string(),
               image_url: "https://example.com/image/url/1".parse().unwrap(),
               static_image_url: Some("https://example.com/static/image/url/1".parse().unwrap()),
               is_visible_in_picker: Some(true),
               category: Some("category1".to_string()),
            },
            CustomEmoji {
               instance: instance.clone(),
               shortcode: "shortcode2".to_string(),
               image_url: "https://example.com/image/url/2".parse().unwrap(),
               static_image_url: Some("https://example.com/static/image/url/2".parse().unwrap()),
               is_visible_in_picker: Some(false),
               category: Some("category2".to_string()),
            },
         ],
         is_bot: Some(true),
         is_group: Some(true),
         is_discoverable: Some(true),
         is_noindex: Some(true),
         is_suspended: Some(true),
         is_limited: Some(true),
         created_time: Some(Utc.with_ymd_and_hms(2000, 1, 1, 0, 0, 0).unwrap()),
         last_status_post_time: Some(Utc.with_ymd_and_hms(2000, 2, 1, 0, 0, 0).unwrap()),
         status_count: Some(10000),
         follower_count: Some(100),
         followee_count: Some(200),
         moved_to: Some(
            save_account(&mut env, &account_fromRust_account_repo, &account_fromRust_instance_repo)
         ),
      };

      account.clone_into_java(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJavaTest_account_1fromRust_00024createInstance<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JObject<'local> {
      use panoptiqon::convert_java::ConvertJava;

      save_instance(&mut env, &account_fromRust_instance_repo)
         .clone_into_java(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJavaTest_account_1fromRust_00024createMovedTo<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JObject<'local> {
      use panoptiqon::convert_java::ConvertJava;

      save_account(&mut env, &account_fromRust_account_repo, &account_fromRust_instance_repo)
         .clone_into_java(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJavaTest_statusVisibility_1toRust_00024assert(
      mut env: JNIEnv,
      _obj: JObject,
      status_visibility: JObject
   ) {
      use panoptiqon::convert_java::ConvertJava;
      use crate::status::StatusVisibility;

      let status_visibility = StatusVisibility::clone_from_java(&mut env, &status_visibility);

      assert_eq!(
         StatusVisibility::Unlisted,
         status_visibility
      );
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJavaTest_statusVisibility_1fromRust_00024createStatusVisibility<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JObject<'local> {
      use panoptiqon::convert_java::ConvertJava;
      use crate::status::StatusVisibility;

      let status_visibility = StatusVisibility::Unlisted;
      status_visibility.clone_into_java(&mut env)
   }

   #[allow(non_upper_case_globals)]
   static customEmoji_toRust_instance_repo: RepositoryHolder<Url, Instance> = new_instance_repo();

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJavaTest_customEmoji_1toRust_00024createInstance<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JObject<'local> {
      use panoptiqon::convert_java::ConvertJava;

      save_instance(&mut env, &customEmoji_toRust_instance_repo)
         .clone_into_java(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJavaTest_customEmoji_1toRust_00024assert(
      mut env: JNIEnv,
      _obj: JObject,
      custom_emoji: JObject
   ) {
      use panoptiqon::convert_java::ConvertJava;
      use crate::custom_emoji::CustomEmoji;

      let custom_emoji = CustomEmoji::clone_from_java(&mut env, &custom_emoji);

      assert_eq!(
         CustomEmoji {
            instance: save_instance(&mut env, &customEmoji_toRust_instance_repo),
            shortcode: "shortcode".to_string(),
            image_url: "https://example.com/image/url".parse().unwrap(),
            static_image_url: Some("https://example.com/static/image/url".parse().unwrap()),
            is_visible_in_picker: Some(true),
            category: Some("category".to_string()),
         },
         custom_emoji
      );
   }

   #[allow(non_upper_case_globals)]
   static customEmoji_nulls_toRust_instance_repo: RepositoryHolder<Url, Instance> = new_instance_repo();

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJavaTest_customEmoji_1nulls_1toRust_00024createInstance<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JObject<'local> {
      use panoptiqon::convert_java::ConvertJava;

      save_instance(&mut env, &customEmoji_nulls_toRust_instance_repo)
         .clone_into_java(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJavaTest_customEmoji_1nulls_1toRust_00024assert(
      mut env: JNIEnv,
      _obj: JObject,
      custom_emoji: JObject
   ) {
      use panoptiqon::convert_java::ConvertJava;
      use crate::custom_emoji::CustomEmoji;

      let custom_emoji = CustomEmoji::clone_from_java(&mut env, &custom_emoji);

      assert_eq!(
         CustomEmoji {
            instance: save_instance(&mut env, &customEmoji_nulls_toRust_instance_repo),
            shortcode: "shortcode".to_string(),
            image_url: "https://example.com/image/url".parse().unwrap(),
            static_image_url: None,
            is_visible_in_picker: None,
            category: None,
         },
         custom_emoji
      );
   }

   #[allow(non_upper_case_globals)]
   static customEmoji_fromRust_instance_repo: RepositoryHolder<Url, Instance> = new_instance_repo();

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJavaTest_customEmoji_1fromRust_00024createCustomEmoji<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JObject<'local> {
      use panoptiqon::convert_java::ConvertJava;
      use crate::custom_emoji::CustomEmoji;

      let custom_emoji = CustomEmoji {
         instance: save_instance(&mut env, &customEmoji_fromRust_instance_repo),
         shortcode: "shortcode".to_string(),
         image_url: "https://example.com/image/url".parse().unwrap(),
         static_image_url: Some("https://example.com/static/image/url".parse().unwrap()),
         is_visible_in_picker: Some(true),
         category: Some("category".to_string()),
      };

      custom_emoji.clone_into_java(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJavaTest_customEmoji_1fromRust_00024createInstance<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JObject<'local> {
      use panoptiqon::convert_java::ConvertJava;

      save_instance(&mut env, &customEmoji_fromRust_instance_repo)
         .clone_into_java(&mut env)
   }

   #[allow(non_upper_case_globals)]
   static customEmoji_nulls_fromRust_instance_repo: RepositoryHolder<Url, Instance> = new_instance_repo();

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJavaTest_customEmoji_1nulls_1fromRust_00024createCustomEmoji<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JObject<'local> {
      use panoptiqon::convert_java::ConvertJava;
      use crate::custom_emoji::CustomEmoji;

      let custom_emoji = CustomEmoji {
         instance: save_instance(&mut env, &customEmoji_nulls_fromRust_instance_repo),
         shortcode: "shortcode".to_string(),
         image_url: "https://example.com/image/url".parse().unwrap(),
         static_image_url: None,
         is_visible_in_picker: None,
         category: None,
      };

      custom_emoji.clone_into_java(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJavaTest_customEmoji_1nulls_1fromRust_00024createInstance<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JObject<'local> {
      use panoptiqon::convert_java::ConvertJava;

      save_instance(&mut env, &customEmoji_nulls_fromRust_instance_repo)
         .clone_into_java(&mut env)
   }

   #[allow(non_upper_case_globals)]
   static role_toRust_instance_repo: RepositoryHolder<Url, Instance> = new_instance_repo();

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJavaTest_role_1toRust_00024createInstance<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JObject<'local> {
      use panoptiqon::convert_java::ConvertJava;

      save_instance(&mut env, &role_toRust_instance_repo)
         .clone_into_java(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJavaTest_role_1toRust_00024assert(
      mut env: JNIEnv,
      _obj: JObject,
      role: JObject
   ) {
      use panoptiqon::convert_java::ConvertJava;
      use crate::role::{Role, RoleId};

      let role = Role::clone_from_java(&mut env, &role);

      assert_eq!(
         Role {
            instance: save_instance(&mut env, &role_toRust_instance_repo),
            id: Some(RoleId("id".to_string())),
            name: Some("name".to_string()),
            color: Some("color".to_string()),
            permissions: Some("permissions".to_string()),
            is_highlighted: Some(true),
         },
         role
      );
   }

   #[allow(non_upper_case_globals)]
   static role_nulls_toRust_instance_repo: RepositoryHolder<Url, Instance> = new_instance_repo();

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJavaTest_role_1nulls_1toRust_00024createInstance<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JObject<'local> {
      use panoptiqon::convert_java::ConvertJava;

      save_instance(&mut env, &role_nulls_toRust_instance_repo)
         .clone_into_java(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJavaTest_role_1nulls_1toRust_00024assert(
      mut env: JNIEnv,
      _obj: JObject,
      role: JObject
   ) {
      use panoptiqon::convert_java::ConvertJava;
      use crate::role::Role;

      let role = Role::clone_from_java(&mut env, &role);

      assert_eq!(
         Role {
            instance: save_instance(&mut env, &role_nulls_toRust_instance_repo),
            id: None,
            name: None,
            color: None,
            permissions: None,
            is_highlighted: None,
         },
         role
      );
   }

   #[allow(non_upper_case_globals)]
   static role_fromRust_instance_repo: RepositoryHolder<Url, Instance> = new_instance_repo();

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJavaTest_role_1fromRust_00024createRole<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JObject<'local> {
      use panoptiqon::convert_java::ConvertJava;
      use crate::role::{Role, RoleId};

      let role = Role {
         instance: save_instance(&mut env, &role_fromRust_instance_repo),
         id: Some(RoleId("id".to_string())),
         name: Some("name".to_string()),
         color: Some("color".to_string()),
         permissions: Some("permissions".to_string()),
         is_highlighted: Some(true),
      };

      role.clone_into_java(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJavaTest_role_1fromRust_00024createInstance<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JObject<'local> {
      use panoptiqon::convert_java::ConvertJava;

      save_instance(&mut env, &role_fromRust_instance_repo)
         .clone_into_java(&mut env)
   }

   #[allow(non_upper_case_globals)]
   static role_nulls_fromRust_instance_repo: RepositoryHolder<Url, Instance> = new_instance_repo();

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJavaTest_role_1nulls_1fromRust_00024createRole<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JObject<'local> {
      use panoptiqon::convert_java::ConvertJava;
      use crate::role::Role;

      let role = Role {
         instance: save_instance(&mut env, &role_nulls_fromRust_instance_repo),
         id: None,
         name: None,
         color: None,
         permissions: None,
         is_highlighted: None,
      };

      role.clone_into_java(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJavaTest_role_1nulls_1fromRust_00024createInstance<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JObject<'local> {
      use panoptiqon::convert_java::ConvertJava;

      save_instance(&mut env, &role_nulls_fromRust_instance_repo)
         .clone_into_java(&mut env)
   }
}
