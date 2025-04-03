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
pub mod filter;
pub mod instance;
pub mod media_attachment;
pub mod poll;
pub mod preview_card;
pub mod role;
pub mod status;
pub mod token;
mod cache;

#[cfg(feature = "jvm")]
pub mod jvm_types;

#[cfg(feature="jni-test")]
mod jni_tests {
   use jni::JNIEnv;
   use jni::objects::JObject;
   use ext_panoptiqon::repository_holder::RepositoryHolder;
   use panoptiqon::cache::Cache;
   use panoptiqon::jvm_types::JvmCache;
   use crate::account::Account;
   use crate::instance::Instance;
   use crate::jvm_types::{
      JvmAccount, JvmCustomEmoji, JvmFilterResult, JvmInstance, JvmMediaAttachment,
      JvmPoll, JvmPollNoCredential, JvmPreviewCard, JvmRole, JvmStatus,
      JvmStatusNoCredential,
   };
   use crate::poll::NoCredentialPoll;
   use crate::status::{NoCredentialStatus, Status};

   fn save_instance(
      env: &mut JNIEnv,
      instance_repo: &RepositoryHolder<Instance>
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
      account_repo: &RepositoryHolder<Account>,
      instance_repo: &RepositoryHolder<Instance>
   ) -> Cache<Account> {
      use crate::account::{AccountId, AccountLocalId};

      let instance = save_instance(env, instance_repo);
      let instance_url = instance.get().url.clone();

      let account = Account {
         instance,
         id: AccountId {
            instance_url,
            local: AccountLocalId("account id".to_string()),
         },
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
   static account_toRust_instance_repo: RepositoryHolder<Instance> = RepositoryHolder::new();

   #[allow(non_upper_case_globals)]
   static account_toRust_account_repo: RepositoryHolder<Account> = RepositoryHolder::new();

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_account_1toRust_00024createInstance<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmCache<'local, JvmInstance<'local>> {
      use panoptiqon::convert_jvm::CloneIntoJvm;

      save_instance(&mut env, &account_toRust_instance_repo)
         .clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_account_1toRust_00024createMovedTo<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmCache<'local, JvmAccount<'local>> {
      use panoptiqon::convert_jvm::CloneIntoJvm;

      save_account(&mut env, &account_toRust_account_repo, &account_toRust_instance_repo)
         .clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_account_1toRust_00024assert<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      account: JvmAccount<'local>
   ) {
      use chrono::{TimeZone, Utc};
      use panoptiqon::convert_jvm::CloneFromJvm;
      use crate::account::{AccountId, AccountLocalId, AccountProfileField};
      use crate::custom_emoji::CustomEmoji;

      let account = Account::clone_from_jvm(&mut env, &account);

      let instance = save_instance(&mut env, &account_toRust_instance_repo);
      let instance_url = instance.get().url.clone();

      assert_eq!(
         Account {
            instance: instance.clone(),
            id: AccountId {
               instance_url,
               local: AccountLocalId("id".to_string()),
            },
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
   static account_fromRust_instance_repo: RepositoryHolder<Instance> = RepositoryHolder::new();

   #[allow(non_upper_case_globals)]
   static account_fromRust_account_repo: RepositoryHolder<Account> = RepositoryHolder::new();

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_account_1fromRust_00024createAccount<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmAccount<'local> {
      use chrono::{TimeZone, Utc};
      use panoptiqon::convert_jvm::CloneIntoJvm;
      use crate::account::{AccountId, AccountLocalId, AccountProfileField};
      use crate::custom_emoji::CustomEmoji;

      let instance = save_instance(&mut env, &account_fromRust_instance_repo);
      let instance_url = instance.get().url.clone();

      let account = Account {
         instance: instance.clone(),
         id: AccountId {
            instance_url,
            local: AccountLocalId("id".to_string()),
         },
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

      account.clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_account_1fromRust_00024createInstance<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmCache<'local, JvmInstance<'local>> {
      use panoptiqon::convert_jvm::CloneIntoJvm;

      save_instance(&mut env, &account_fromRust_instance_repo)
         .clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_account_1fromRust_00024createMovedTo<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmCache<'local, JvmAccount<'local>> {
      use panoptiqon::convert_jvm::CloneIntoJvm;

      save_account(&mut env, &account_fromRust_account_repo, &account_fromRust_instance_repo)
         .clone_into_jvm(&mut env)
   }

   #[allow(non_upper_case_globals)]
   static customEmoji_toRust_instance_repo: RepositoryHolder<Instance> = RepositoryHolder::new();

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_customEmoji_1toRust_00024createInstance<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmCache<'local, JvmInstance<'local>> {
      use panoptiqon::convert_jvm::CloneIntoJvm;

      save_instance(&mut env, &customEmoji_toRust_instance_repo)
         .clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_customEmoji_1toRust_00024assert<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      custom_emoji: JvmCustomEmoji<'local>
   ) {
      use panoptiqon::convert_jvm::CloneFromJvm;
      use crate::custom_emoji::CustomEmoji;

      let custom_emoji = CustomEmoji::clone_from_jvm(&mut env, &custom_emoji);

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
   static customEmoji_nulls_toRust_instance_repo: RepositoryHolder<Instance> = RepositoryHolder::new();

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_customEmoji_1nulls_1toRust_00024createInstance<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmCache<'local, JvmInstance<'local>> {
      use panoptiqon::convert_jvm::CloneIntoJvm;

      save_instance(&mut env, &customEmoji_nulls_toRust_instance_repo)
         .clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_customEmoji_1nulls_1toRust_00024assert<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      custom_emoji: JvmCustomEmoji<'local>
   ) {
      use panoptiqon::convert_jvm::CloneFromJvm;
      use crate::custom_emoji::CustomEmoji;

      let custom_emoji = CustomEmoji::clone_from_jvm(&mut env, &custom_emoji);

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
   static customEmoji_fromRust_instance_repo: RepositoryHolder<Instance> = RepositoryHolder::new();

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_customEmoji_1fromRust_00024createCustomEmoji<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmCustomEmoji<'local> {
      use panoptiqon::convert_jvm::CloneIntoJvm;
      use crate::custom_emoji::CustomEmoji;

      let custom_emoji = CustomEmoji {
         instance: save_instance(&mut env, &customEmoji_fromRust_instance_repo),
         shortcode: "shortcode".to_string(),
         image_url: "https://example.com/image/url".parse().unwrap(),
         static_image_url: Some("https://example.com/static/image/url".parse().unwrap()),
         is_visible_in_picker: Some(true),
         category: Some("category".to_string()),
      };

      custom_emoji.clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_customEmoji_1fromRust_00024createInstance<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmCache<'local, JvmInstance<'local>> {
      use panoptiqon::convert_jvm::CloneIntoJvm;

      save_instance(&mut env, &customEmoji_fromRust_instance_repo)
         .clone_into_jvm(&mut env)
   }

   #[allow(non_upper_case_globals)]
   static customEmoji_nulls_fromRust_instance_repo: RepositoryHolder<Instance> = RepositoryHolder::new();

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_customEmoji_1nulls_1fromRust_00024createCustomEmoji<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmCustomEmoji<'local> {
      use panoptiqon::convert_jvm::CloneIntoJvm;
      use crate::custom_emoji::CustomEmoji;

      let custom_emoji = CustomEmoji {
         instance: save_instance(&mut env, &customEmoji_nulls_fromRust_instance_repo),
         shortcode: "shortcode".to_string(),
         image_url: "https://example.com/image/url".parse().unwrap(),
         static_image_url: None,
         is_visible_in_picker: None,
         category: None,
      };

      custom_emoji.clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_customEmoji_1nulls_1fromRust_00024createInstance<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmCache<'local, JvmInstance<'local>> {
      use panoptiqon::convert_jvm::CloneIntoJvm;

      save_instance(&mut env, &customEmoji_nulls_fromRust_instance_repo)
         .clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_filterResult_1toRust_00024assert<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      filter_result: JvmFilterResult<'local>
   ) {
      use chrono::{TimeZone, Utc};
      use panoptiqon::convert_jvm::CloneFromJvm;
      use crate::filter::{
         Filter, FilterAction, FilterContext, FilterId, FilterKeyword,
         FilterKeywordId, FilterResult, FilterStatus, FilterStatusId,
      };
      use crate::status::{StatusId, StatusLocalId};

      let filter_result = FilterResult::clone_from_jvm(&mut env, &filter_result);

      assert_eq!(
         FilterResult {
            filter: Some(Filter {
               id: FilterId("filter id".to_string()),
               title: Some("title".to_string()),
               context: vec![
                  FilterContext("home".to_string()),
                  FilterContext("public".to_string()),
                  FilterContext("illegal context".to_string()),
               ],
               expire_time: Some(Utc.with_ymd_and_hms(2000, 1, 1, 0, 0, 0).unwrap()),
               filter_action: Some(FilterAction("hide".to_string())),
               keywords: vec![
                  FilterKeyword {
                     id: FilterKeywordId("filter keyword id1".to_string()),
                     keyword: Some("keyword1".to_string()),
                     whole_word: Some(false),
                  },
                  FilterKeyword {
                     id: FilterKeywordId("filter keyword id2".to_string()),
                     keyword: Some("keyword2".to_string()),
                     whole_word: Some(true),
                  },
               ],
               statuses: vec![
                  FilterStatus {
                     id: FilterStatusId("filter status id1".to_string()),
                     status_id: StatusId {
                        instance_url: "https://example.com/instance/url".parse().unwrap(),
                        local: StatusLocalId("status id1".to_string())
                     },
                  },
                  FilterStatus {
                     id: FilterStatusId("filter status id2".to_string()),
                     status_id: StatusId {
                        instance_url: "https://example.com/instance/url".parse().unwrap(),
                        local: StatusLocalId("status id2".to_string())
                     },
                  },
               ],
            }),
            keyword_matches: vec![
               "keyword1".to_string(),
            ],
            status_matches: vec![
               StatusId {
                  instance_url: "https://example.com/instance/url".parse().unwrap(),
                  local: StatusLocalId("status id1".to_string())
               },
               StatusId {
                  instance_url: "https://example.com/instance/url".parse().unwrap(),
                  local: StatusLocalId("status id2".to_string())
               },
            ],
         },
         filter_result
      );
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_filterResult_1nulls_1toRust_00024assert<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      filter_result: JvmFilterResult<'local>
   ) {
      use panoptiqon::convert_jvm::CloneFromJvm;
      use crate::filter::{
         Filter, FilterId, FilterKeyword, FilterKeywordId, FilterResult,
      };

      let filter_result = FilterResult::clone_from_jvm(&mut env, &filter_result);

      assert_eq!(
         FilterResult {
            filter: Some(Filter {
               id: FilterId("filter id".to_string()),
               title: None,
               context: vec![],
               expire_time: None,
               filter_action: None,
               keywords: vec![
                  FilterKeyword {
                     id: FilterKeywordId("filter keyword id1".to_string()),
                     keyword: None,
                     whole_word: None,
                  },
               ],
               statuses: vec![],
            }),
            keyword_matches: vec![],
            status_matches: vec![],
         },
         filter_result
      );
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_filterResult_1fromRust_00024createFilterResult<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmFilterResult<'local> {
      use chrono::{TimeZone, Utc};
      use panoptiqon::convert_jvm::CloneIntoJvm;
      use crate::filter::{
         Filter, FilterAction, FilterContext, FilterId, FilterKeyword,
         FilterKeywordId, FilterResult, FilterStatus, FilterStatusId,
      };
      use crate::status::{StatusId, StatusLocalId};

      let filter_result = FilterResult {
         filter: Some(Filter {
            id: FilterId("filter id".to_string()),
            title: Some("title".to_string()),
            context: vec![
               FilterContext("home".to_string()),
               FilterContext("public".to_string()),
               FilterContext("illegal context".to_string()),
            ],
            expire_time: Some(Utc.with_ymd_and_hms(2000, 1, 1, 0, 0, 0).unwrap()),
            filter_action: Some(FilterAction("hide".to_string())),
            keywords: vec![
               FilterKeyword {
                  id: FilterKeywordId("filter keyword id1".to_string()),
                  keyword: Some("keyword1".to_string()),
                  whole_word: Some(false),
               },
               FilterKeyword {
                  id: FilterKeywordId("filter keyword id2".to_string()),
                  keyword: Some("keyword2".to_string()),
                  whole_word: Some(true),
               },
            ],
            statuses: vec![
               FilterStatus {
                  id: FilterStatusId("filter status id1".to_string()),
                  status_id: StatusId {
                     instance_url: "https://example.com/instance/url".parse().unwrap(),
                     local: StatusLocalId("status id1".to_string())
                  },
               },
               FilterStatus {
                  id: FilterStatusId("filter status id2".to_string()),
                  status_id: StatusId {
                     instance_url: "https://example.com/instance/url".parse().unwrap(),
                     local: StatusLocalId("status id2".to_string())
                  },
               },
            ],
         }),
         keyword_matches: vec![
            "keyword1".to_string(),
         ],
         status_matches: vec![
            StatusId {
               instance_url: "https://example.com/instance/url".parse().unwrap(),
               local: StatusLocalId("status id1".to_string())
            },
            StatusId {
               instance_url: "https://example.com/instance/url".parse().unwrap(),
               local: StatusLocalId("status id2".to_string())
            },
         ],
      };

      filter_result.clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_filterResult_1nulls_1fromRust_00024createFilterResult<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmFilterResult<'local> {
      use panoptiqon::convert_jvm::CloneIntoJvm;
      use crate::filter::{
         Filter, FilterId, FilterKeyword, FilterKeywordId, FilterResult,
      };

      let filter_result = FilterResult {
         filter: Some(Filter {
            id: FilterId("filter id".to_string()),
            title: None,
            context: vec![],
            expire_time: None,
            filter_action: None,
            keywords: vec![
               FilterKeyword {
                  id: FilterKeywordId("filter keyword id1".to_string()),
                  keyword: None,
                  whole_word: None,
               },
            ],
            statuses: vec![],
         }),
         keyword_matches: vec![],
         status_matches: vec![],
      };

      filter_result.clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_mediaAttachment_1image_1toRust_00024assert<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      media_attachment: JvmMediaAttachment<'local>
   ) {
      use panoptiqon::convert_jvm::CloneFromJvm;
      use crate::media_attachment::{
         ImageFocus, ImageSize, MediaAttachment, MediaAttachmentId,
         MediaAttachmentMetadata,
      };

      let media_attachment = MediaAttachment::clone_from_jvm(&mut env, &media_attachment);

      assert_eq!(
         MediaAttachment {
            id: MediaAttachmentId("media attachment id".to_string()),
            url: Some("https://example.com/".parse().unwrap()),
            preview_url: Some("https://example.com/preview".parse().unwrap()),
            remote_url: Some("https://example.com/remote".parse().unwrap()),
            metadata: Some(MediaAttachmentMetadata::Image {
               original_size: Some(ImageSize {
                  width: 1000,
                  height: 2000,
               }),
               small_size: Some(ImageSize {
                  width: 100,
                  height: 200,
               }),
               focus: Some(ImageFocus {
                  x: 0.1,
                  y: 0.2,
               }),
            }),
            description: Some("description".to_string()),
            blurhash: Some("blurhash".to_string()),
         },
         media_attachment
      );
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_mediaAttachment_1video_1toRust_00024assert<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      media_attachment: JvmMediaAttachment<'local>
   ) {
      use std::time::Duration;
      use panoptiqon::convert_jvm::CloneFromJvm;
      use crate::media_attachment::{
         ImageSize, MediaAttachment, MediaAttachmentId, MediaAttachmentMetadata,
         VideoSize,
      };

      let media_attachment = MediaAttachment::clone_from_jvm(&mut env, &media_attachment);

      assert_eq!(
         MediaAttachment {
            id: MediaAttachmentId("media attachment id".to_string()),
            url: Some("https://example.com/".parse().unwrap()),
            preview_url: Some("https://example.com/preview".parse().unwrap()),
            remote_url: Some("https://example.com/remote".parse().unwrap()),
            metadata: Some(MediaAttachmentMetadata::Video {
               original_size: Some(VideoSize {
                  width: Some(1000),
                  height: Some(2000),
                  frame_rate: Some("frameRate".to_string()),
                  duration: Some(Duration::from_secs_f64(1.23)),
                  bitrate: Some(123),
               }),
               small_size: Some(ImageSize {
                  width: 100,
                  height: 200,
               }),
               length: Some("length".to_string()),
               fps: Some(42),
               audio_encode: Some("audioEncode".to_string()),
               audio_bitrate: Some("audioBitrate".to_string()),
               audio_channels: Some("audioChannels".to_string()),
            }),
            description: Some("description".to_string()),
            blurhash: Some("blurhash".to_string()),
         },
         media_attachment
      );
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_mediaAttachment_1gifv_1toRust_00024assert<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      media_attachment: JvmMediaAttachment<'local>
   ) {
      use std::time::Duration;
      use panoptiqon::convert_jvm::CloneFromJvm;
      use crate::media_attachment::{
         ImageSize, MediaAttachment, MediaAttachmentId, MediaAttachmentMetadata,
         VideoSize,
      };

      let media_attachment = MediaAttachment::clone_from_jvm(&mut env, &media_attachment);

      assert_eq!(
         MediaAttachment {
            id: MediaAttachmentId("media attachment id".to_string()),
            url: Some("https://example.com/".parse().unwrap()),
            preview_url: Some("https://example.com/preview".parse().unwrap()),
            remote_url: Some("https://example.com/remote".parse().unwrap()),
            metadata: Some(MediaAttachmentMetadata::Gifv {
               original_size: Some(VideoSize {
                  width: Some(1000),
                  height: Some(2000),
                  frame_rate: Some("frameRate".to_string()),
                  duration: Some(Duration::from_secs_f64(1.23)),
                  bitrate: Some(123),
               }),
               small_size: Some(ImageSize {
                  width: 100,
                  height: 200,
               }),
               length: Some("length".to_string()),
               fps: Some(42),
            }),
            description: Some("description".to_string()),
            blurhash: Some("blurhash".to_string()),
         },
         media_attachment
      );
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_mediaAttachment_1audio_1toRust_00024assert<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      media_attachment: JvmMediaAttachment<'local>
   ) {
      use std::time::Duration;
      use panoptiqon::convert_jvm::CloneFromJvm;
      use crate::media_attachment::{
         AudioSize, MediaAttachment, MediaAttachmentId, MediaAttachmentMetadata,
      };

      let media_attachment = MediaAttachment::clone_from_jvm(&mut env, &media_attachment);

      assert_eq!(
         MediaAttachment {
            id: MediaAttachmentId("media attachment id".to_string()),
            url: Some("https://example.com/".parse().unwrap()),
            preview_url: Some("https://example.com/preview".parse().unwrap()),
            remote_url: Some("https://example.com/remote".parse().unwrap()),
            metadata: Some(MediaAttachmentMetadata::Audio {
               original_size: Some(AudioSize {
                  duration: Some(Duration::from_secs_f64(1.23)),
                  bitrate: Some(123),
               }),
               length: Some("length".to_string()),
               audio_encode: Some("audioEncode".to_string()),
               audio_bitrate: Some("audioBitrate".to_string()),
               audio_channels: Some("audioChannels".to_string()),
            }),
            description: Some("description".to_string()),
            blurhash: Some("blurhash".to_string()),
         },
         media_attachment
      );
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_mediaAttachment_1nulls_1toRust_00024assert<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      media_attachment: JvmMediaAttachment<'local>
   ) {
      use panoptiqon::convert_jvm::CloneFromJvm;
      use crate::media_attachment::{MediaAttachment, MediaAttachmentId};

      let media_attachment = MediaAttachment::clone_from_jvm(&mut env, &media_attachment);

      assert_eq!(
         MediaAttachment {
            id: MediaAttachmentId("media attachment id".to_string()),
            url: None,
            preview_url: None,
            remote_url: None,
            metadata: None,
            description: None,
            blurhash: None,
         },
         media_attachment
      );
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_mediaAttachment_1image_1nulls_1toRust_00024assert<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      media_attachment: JvmMediaAttachment<'local>
   ) {
      use panoptiqon::convert_jvm::CloneFromJvm;
      use crate::media_attachment::{
         MediaAttachment, MediaAttachmentId, MediaAttachmentMetadata,
      };

      let media_attachment = MediaAttachment::clone_from_jvm(&mut env, &media_attachment);

      assert_eq!(
         MediaAttachment {
            id: MediaAttachmentId("media attachment id".to_string()),
            url: None,
            preview_url: None,
            remote_url: None,
            metadata: Some(MediaAttachmentMetadata::Image {
               original_size: None,
               small_size: None,
               focus: None,
            }),
            description: None,
            blurhash: None,
         },
         media_attachment
      );
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_mediaAttachment_1video_1nulls_1toRust_00024assert<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      media_attachment: JvmMediaAttachment<'local>
   ) {
      use panoptiqon::convert_jvm::CloneFromJvm;
      use crate::media_attachment::{
         MediaAttachment, MediaAttachmentId, MediaAttachmentMetadata,
      };

      let media_attachment = MediaAttachment::clone_from_jvm(&mut env, &media_attachment);

      assert_eq!(
         MediaAttachment {
            id: MediaAttachmentId("media attachment id".to_string()),
            url: None,
            preview_url: None,
            remote_url: None,
            metadata: Some(MediaAttachmentMetadata::Video {
               original_size: None,
               small_size: None,
               length: None,
               fps: None,
               audio_encode: None,
               audio_bitrate: None,
               audio_channels: None,
            }),
            description: None,
            blurhash: None,
         },
         media_attachment
      );
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_mediaAttachment_1gifv_1nulls_1toRust_00024assert<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      media_attachment: JvmMediaAttachment<'local>
   ) {
      use panoptiqon::convert_jvm::CloneFromJvm;
      use crate::media_attachment::{
         MediaAttachment, MediaAttachmentId, MediaAttachmentMetadata,
      };

      let media_attachment = MediaAttachment::clone_from_jvm(&mut env, &media_attachment);

      assert_eq!(
         MediaAttachment {
            id: MediaAttachmentId("media attachment id".to_string()),
            url: None,
            preview_url: None,
            remote_url: None,
            metadata: Some(MediaAttachmentMetadata::Gifv {
               original_size: None,
               small_size: None,
               length: None,
               fps: None,
            }),
            description: None,
            blurhash: None,
         },
         media_attachment
      );
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_mediaAttachment_1audio_1nulls_1toRust_00024assert<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      media_attachment: JvmMediaAttachment<'local>
   ) {
      use panoptiqon::convert_jvm::CloneFromJvm;
      use crate::media_attachment::{
         MediaAttachment, MediaAttachmentId, MediaAttachmentMetadata,
      };

      let media_attachment = MediaAttachment::clone_from_jvm(&mut env, &media_attachment);

      assert_eq!(
         MediaAttachment {
            id: MediaAttachmentId("media attachment id".to_string()),
            url: None,
            preview_url: None,
            remote_url: None,
            metadata: Some(MediaAttachmentMetadata::Audio {
               original_size: None,
               length: None,
               audio_encode: None,
               audio_bitrate: None,
               audio_channels: None,
            }),
            description: None,
            blurhash: None,
         },
         media_attachment
      );
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_mediaAttachment_1image_1fromRust_00024createMediaAttachment<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmMediaAttachment<'local> {
      use panoptiqon::convert_jvm::CloneIntoJvm;
      use crate::media_attachment::{
         ImageFocus, ImageSize, MediaAttachment, MediaAttachmentId,
         MediaAttachmentMetadata,
      };

      let media_attachment = MediaAttachment {
         id: MediaAttachmentId("media attachment id".to_string()),
         url: Some("https://example.com/".parse().unwrap()),
         preview_url: Some("https://example.com/preview".parse().unwrap()),
         remote_url: Some("https://example.com/remote".parse().unwrap()),
         metadata: Some(MediaAttachmentMetadata::Image {
            original_size: Some(ImageSize {
               width: 1000,
               height: 2000,
            }),
            small_size: Some(ImageSize {
               width: 100,
               height: 200,
            }),
            focus: Some(ImageFocus {
               x: 0.1,
               y: 0.2,
            }),
         }),
         description: Some("description".to_string()),
         blurhash: Some("blurhash".to_string()),
      };

      media_attachment.clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_mediaAttachment_1video_1fromRust_00024createMediaAttachment<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmMediaAttachment<'local> {
      use std::time::Duration;
      use panoptiqon::convert_jvm::CloneIntoJvm;
      use crate::media_attachment::{
         ImageSize, MediaAttachment, MediaAttachmentId, MediaAttachmentMetadata,
         VideoSize,
      };

      let media_attachment = MediaAttachment {
         id: MediaAttachmentId("media attachment id".to_string()),
         url: Some("https://example.com/".parse().unwrap()),
         preview_url: Some("https://example.com/preview".parse().unwrap()),
         remote_url: Some("https://example.com/remote".parse().unwrap()),
         metadata: Some(MediaAttachmentMetadata::Video {
            original_size: Some(VideoSize {
               width: Some(1000),
               height: Some(2000),
               frame_rate: Some("frameRate".to_string()),
               duration: Some(Duration::from_secs_f64(1.23)),
               bitrate: Some(123),
            }),
            small_size: Some(ImageSize {
               width: 100,
               height: 200,
            }),
            length: Some("length".to_string()),
            fps: Some(42),
            audio_encode: Some("audioEncode".to_string()),
            audio_bitrate: Some("audioBitrate".to_string()),
            audio_channels: Some("audioChannels".to_string()),
         }),
         description: Some("description".to_string()),
         blurhash: Some("blurhash".to_string()),
      };

      media_attachment.clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_mediaAttachment_1gifv_1fromRust_00024createMediaAttachment<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmMediaAttachment<'local> {
      use std::time::Duration;
      use panoptiqon::convert_jvm::CloneIntoJvm;
      use crate::media_attachment::{
         ImageSize, MediaAttachment, MediaAttachmentId, MediaAttachmentMetadata,
         VideoSize,
      };

      let media_attachment = MediaAttachment {
         id: MediaAttachmentId("media attachment id".to_string()),
         url: Some("https://example.com/".parse().unwrap()),
         preview_url: Some("https://example.com/preview".parse().unwrap()),
         remote_url: Some("https://example.com/remote".parse().unwrap()),
         metadata: Some(MediaAttachmentMetadata::Gifv {
            original_size: Some(VideoSize {
               width: Some(1000),
               height: Some(2000),
               frame_rate: Some("frameRate".to_string()),
               duration: Some(Duration::from_secs_f64(1.23)),
               bitrate: Some(123),
            }),
            small_size: Some(ImageSize {
               width: 100,
               height: 200,
            }),
            length: Some("length".to_string()),
            fps: Some(42),
         }),
         description: Some("description".to_string()),
         blurhash: Some("blurhash".to_string()),
      };

      media_attachment.clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_mediaAttachment_1audio_1fromRust_00024createMediaAttachment<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmMediaAttachment<'local> {
      use std::time::Duration;
      use panoptiqon::convert_jvm::CloneIntoJvm;
      use crate::media_attachment::{
         AudioSize, MediaAttachment, MediaAttachmentId, MediaAttachmentMetadata,
      };

      let media_attachment = MediaAttachment {
         id: MediaAttachmentId("media attachment id".to_string()),
         url: Some("https://example.com/".parse().unwrap()),
         preview_url: Some("https://example.com/preview".parse().unwrap()),
         remote_url: Some("https://example.com/remote".parse().unwrap()),
         metadata: Some(MediaAttachmentMetadata::Audio {
            original_size: Some(AudioSize {
               duration: Some(Duration::from_secs_f64(1.23)),
               bitrate: Some(123),
            }),
            length: Some("length".to_string()),
            audio_encode: Some("audioEncode".to_string()),
            audio_bitrate: Some("audioBitrate".to_string()),
            audio_channels: Some("audioChannels".to_string()),
         }),
         description: Some("description".to_string()),
         blurhash: Some("blurhash".to_string()),
      };

      media_attachment.clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_mediaAttachment_1nulls_1fromRust_00024createMediaAttachment<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmMediaAttachment<'local> {
      use panoptiqon::convert_jvm::CloneIntoJvm;
      use crate::media_attachment::{MediaAttachment, MediaAttachmentId};

      let media_attachment = MediaAttachment {
         id: MediaAttachmentId("media attachment id".to_string()),
         url: None,
         preview_url: None,
         remote_url: None,
         metadata: None,
         description: None,
         blurhash: None,
      };

      media_attachment.clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_mediaAttachment_1image_1nulls_1fromRust_00024createMediaAttachment<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmMediaAttachment<'local> {
      use panoptiqon::convert_jvm::CloneIntoJvm;
      use crate::media_attachment::{
         MediaAttachment, MediaAttachmentId, MediaAttachmentMetadata,
      };

      let media_attachment = MediaAttachment {
         id: MediaAttachmentId("media attachment id".to_string()),
         url: None,
         preview_url: None,
         remote_url: None,
         metadata: Some(MediaAttachmentMetadata::Image {
            original_size: None,
            small_size: None,
            focus: None,
         }),
         description: None,
         blurhash: None,
      };

      media_attachment.clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_mediaAttachment_1video_1nulls_1fromRust_00024createMediaAttachment<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmMediaAttachment<'local> {
      use panoptiqon::convert_jvm::CloneIntoJvm;
      use crate::media_attachment::{
         MediaAttachment, MediaAttachmentId, MediaAttachmentMetadata,
      };

      let media_attachment = MediaAttachment {
         id: MediaAttachmentId("media attachment id".to_string()),
         url: None,
         preview_url: None,
         remote_url: None,
         metadata: Some(MediaAttachmentMetadata::Video {
            original_size: None,
            small_size: None,
            length: None,
            fps: None,
            audio_encode: None,
            audio_bitrate: None,
            audio_channels: None,
         }),
         description: None,
         blurhash: None,
      };

      media_attachment.clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_mediaAttachment_1gifv_1nulls_1fromRust_00024createMediaAttachment<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmMediaAttachment<'local> {
      use panoptiqon::convert_jvm::CloneIntoJvm;
      use crate::media_attachment::{
         MediaAttachment, MediaAttachmentId, MediaAttachmentMetadata,
      };

      let media_attachment = MediaAttachment {
         id: MediaAttachmentId("media attachment id".to_string()),
         url: None,
         preview_url: None,
         remote_url: None,
         metadata: Some(MediaAttachmentMetadata::Gifv {
            original_size: None,
            small_size: None,
            length: None,
            fps: None,
         }),
         description: None,
         blurhash: None,
      };

      media_attachment.clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_mediaAttachment_1audio_1nulls_1fromRust_00024createMediaAttachment<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmMediaAttachment<'local> {
      use panoptiqon::convert_jvm::CloneIntoJvm;
      use crate::media_attachment::{
         MediaAttachment, MediaAttachmentId, MediaAttachmentMetadata,
      };

      let media_attachment = MediaAttachment {
         id: MediaAttachmentId("media attachment id".to_string()),
         url: None,
         preview_url: None,
         remote_url: None,
         metadata: Some(MediaAttachmentMetadata::Audio {
            original_size: None,
            length: None,
            audio_encode: None,
            audio_bitrate: None,
            audio_channels: None,
         }),
         description: None,
         blurhash: None,
      };

      media_attachment.clone_into_jvm(&mut env)
   }

   #[allow(non_upper_case_globals)]
   static poll_toRust_instance_repo: RepositoryHolder<Instance> = RepositoryHolder::new();

   #[allow(non_upper_case_globals)]
   static poll_toRust_no_credential_poll_repo: RepositoryHolder<NoCredentialPoll> = RepositoryHolder::new();

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_poll_1toRust_00024createInstance<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmCache<'local, JvmInstance<'local>> {
      use panoptiqon::convert_jvm::CloneIntoJvm;

      save_instance(&mut env, &poll_toRust_instance_repo)
         .clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_poll_1toRust_00024assertNoCredential<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      no_credential_poll: JvmPollNoCredential<'local>
   ) {
      use chrono::{TimeZone, Utc};
      use panoptiqon::convert_jvm::CloneFromJvm;
      use crate::custom_emoji::CustomEmoji;
      use crate::poll::{NoCredentialPoll, PollId, PollLocalId, PollOption};

      let no_credential_poll = NoCredentialPoll::clone_from_jvm(
         &mut env, &no_credential_poll
      );

      let instance = poll_toRust_instance_repo.read(&mut env).unwrap()
         .load("https://example.com/instance/url".parse().unwrap()).unwrap();

      let instance_url = instance.get().url.clone();

      assert_eq!(
         NoCredentialPoll {
            id: PollId {
               instance_url: instance_url.clone(),
               local: PollLocalId("poll id".to_string()),
            },
            expire_time: Some(Utc.with_ymd_and_hms(2000, 1, 1, 0, 0, 0).unwrap()),
            is_expired: Some(true),
            allows_multiple_choices: Some(false),
            vote_count: Some(123),
            voter_count: Some(45),
            poll_options: vec![
               PollOption {
                  title: Some("title1".to_string()),
                  vote_count: Some(1),
               },
               PollOption {
                  title: Some("title2".to_string()),
                  vote_count: Some(2),
               },
               PollOption {
                  title: Some("title3".to_string()),
                  vote_count: Some(3),
               },
            ],
            emojis: vec![
               CustomEmoji {
                  instance: instance.clone(),
                  shortcode: "shortcode1".to_string(),
                  image_url: "https://example.com/image/url/1".parse().unwrap(),
                  static_image_url:
                  Some("https://example.com/static/image/url/1".parse().unwrap()),
                  is_visible_in_picker: Some(true),
                  category: Some("category1".to_string()),
               },
               CustomEmoji {
                  instance: instance.clone(),
                  shortcode: "shortcode2".to_string(),
                  image_url: "https://example.com/image/url/2".parse().unwrap(),
                  static_image_url:
                  Some("https://example.com/static/image/url/2".parse().unwrap()),
                  is_visible_in_picker: Some(false),
                  category: Some("category2".to_string()),
               },
            ],
         },
         no_credential_poll
      );
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_poll_1toRust_00024saveNoCredential<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      no_credential_poll: JvmPollNoCredential<'local>
   ) -> JvmCache<'local, JvmPollNoCredential<'local>> {
      use panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm};

      let no_credential_poll = NoCredentialPoll::clone_from_jvm(
         &mut env, &no_credential_poll
      );

      let cache = poll_toRust_no_credential_poll_repo
         .write(&mut env).unwrap()
         .save(no_credential_poll);

      cache.clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_poll_1toRust_00024assert<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      poll: JvmPoll<'local>
   ) {
      use panoptiqon::convert_jvm::CloneFromJvm;
      use crate::poll::{Poll, PollId, PollLocalId};

      let poll = Poll::clone_from_jvm(&mut env, &poll);

      let instance = save_instance(&mut env, &poll_toRust_instance_repo);

      let poll_id = PollId {
         instance_url: instance.get().url.clone(),
         local: PollLocalId("poll id".to_string()),
      };

      let no_credential = poll_toRust_no_credential_poll_repo
         .read(&mut env).unwrap()
         .load(poll_id.clone()).unwrap();

      assert_eq!(
         Poll {
            id: poll_id,
            no_credential,
            is_voted: Some(true),
            voted_options: vec![0],
         },
         poll
      );
   }

   #[allow(non_upper_case_globals)]
   static poll_nulls_toRust_instance_repo: RepositoryHolder<Instance> = RepositoryHolder::new();

   #[allow(non_upper_case_globals)]
   static poll_nulls_toRust_no_credential_poll_repo: RepositoryHolder<NoCredentialPoll> = RepositoryHolder::new();

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_poll_1nulls_1toRust_00024createInstance<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmCache<'local, JvmInstance<'local>> {
      use panoptiqon::convert_jvm::CloneIntoJvm;

      save_instance(&mut env, &poll_nulls_toRust_instance_repo)
         .clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_poll_1nulls_1toRust_00024assertNoCredential<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      no_credential_poll: JvmPollNoCredential<'local>
   ) {
      use panoptiqon::convert_jvm::CloneFromJvm;
      use crate::poll::{NoCredentialPoll, PollId, PollLocalId, PollOption};

      let no_credential_poll = NoCredentialPoll::clone_from_jvm(
         &mut env, &no_credential_poll
      );

      let instance = poll_nulls_toRust_instance_repo.read(&mut env).unwrap()
         .load("https://example.com/instance/url".parse().unwrap()).unwrap();

      let instance_url = instance.get().url.clone();

      assert_eq!(
         NoCredentialPoll {
            id: PollId {
               instance_url: instance_url.clone(),
               local: PollLocalId("poll id".to_string()),
            },
            expire_time: None,
            is_expired: None,
            allows_multiple_choices: None,
            vote_count: None,
            voter_count: None,
            poll_options: vec![
               PollOption {
                  title: None,
                  vote_count: None,
               },
            ],
            emojis: vec![],
         },
         no_credential_poll
      );
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_poll_1nulls_1toRust_00024saveNoCredential<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      no_credential_poll: JvmPollNoCredential<'local>
   ) -> JvmCache<'local, JvmPollNoCredential<'local>> {
      use panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm};

      let no_credential_poll = NoCredentialPoll::clone_from_jvm(
         &mut env, &no_credential_poll
      );

      let cache = poll_nulls_toRust_no_credential_poll_repo
         .write(&mut env).unwrap()
         .save(no_credential_poll);

      cache.clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_poll_1nulls_1toRust_00024assert<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      poll: JvmPoll<'local>
   ) {
      use panoptiqon::convert_jvm::CloneFromJvm;
      use crate::poll::{Poll, PollId, PollLocalId};

      let poll = Poll::clone_from_jvm(&mut env, &poll);

      let instance = save_instance(&mut env, &poll_nulls_toRust_instance_repo);

      let poll_id = PollId {
         instance_url: instance.get().url.clone(),
         local: PollLocalId("poll id".to_string()),
      };

      let no_credential = poll_nulls_toRust_no_credential_poll_repo
         .read(&mut env).unwrap()
         .load(poll_id.clone()).unwrap();

      assert_eq!(
         Poll {
            id: poll_id,
            no_credential,
            is_voted: None,
            voted_options: vec![],
         },
         poll
      );
   }

   #[allow(non_upper_case_globals)]
   static poll_fromRust_instance_repo: RepositoryHolder<Instance> = RepositoryHolder::new();

   #[allow(non_upper_case_globals)]
   static poll_fromRust_no_credential_poll_repo: RepositoryHolder<NoCredentialPoll>
      = RepositoryHolder::new();

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_poll_1fromRust_00024createPoll<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmPoll<'local> {
      use chrono::{TimeZone, Utc};
      use panoptiqon::convert_jvm::CloneIntoJvm;
      use crate::custom_emoji::CustomEmoji;
      use crate::poll::{Poll, PollId, PollLocalId, PollOption};

      let instance = save_instance(&mut env, &poll_fromRust_instance_repo);
      let instance_url = instance.get().url.clone();

      let no_credential = NoCredentialPoll {
         id: PollId {
            instance_url: instance_url.clone(),
            local: PollLocalId("poll id".to_string()),
         },
         expire_time: Some(Utc.with_ymd_and_hms(2000, 1, 1, 0, 0, 0).unwrap()),
         is_expired: Some(true),
         allows_multiple_choices: Some(false),
         vote_count: Some(123),
         voter_count: Some(45),
         poll_options: vec![
            PollOption {
               title: Some("title1".to_string()),
               vote_count: Some(1),
            },
            PollOption {
               title: Some("title2".to_string()),
               vote_count: Some(2),
            },
            PollOption {
               title: Some("title3".to_string()),
               vote_count: Some(3),
            },
         ],
         emojis: vec![
            CustomEmoji {
               instance: instance.clone(),
               shortcode: "shortcode1".to_string(),
               image_url: "https://example.com/image/url/1".parse().unwrap(),
               static_image_url:
               Some("https://example.com/static/image/url/1".parse().unwrap()),
               is_visible_in_picker: Some(true),
               category: Some("category1".to_string()),
            },
            CustomEmoji {
               instance: instance.clone(),
               shortcode: "shortcode2".to_string(),
               image_url: "https://example.com/image/url/2".parse().unwrap(),
               static_image_url:
               Some("https://example.com/static/image/url/2".parse().unwrap()),
               is_visible_in_picker: Some(false),
               category: Some("category2".to_string()),
            },
         ],
      };

      let no_credential = poll_fromRust_no_credential_poll_repo
         .write(&mut env).unwrap()
         .save(no_credential);

      let poll = Poll {
         id: PollId {
            instance_url: instance_url.clone(),
            local: PollLocalId("poll id".to_string()),
         },
         no_credential,
         is_voted: Some(true),
         voted_options: vec![0],
      };

      poll.clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_poll_1fromRust_00024getInstanceCache<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmCache<'local, JvmInstance<'local>> {
      use panoptiqon::convert_jvm::CloneIntoJvm;

      let instance = poll_fromRust_instance_repo.read(&mut env).unwrap()
         .load("https://example.com/instance/url".parse().unwrap()).unwrap();

      instance.clone_into_jvm(&mut env)
   }

   #[allow(non_upper_case_globals)]
   static poll_nulls_fromRust_instance_repo: RepositoryHolder<Instance> = RepositoryHolder::new();

   #[allow(non_upper_case_globals)]
   static poll_nulls_fromRust_no_credential_poll_repo: RepositoryHolder<NoCredentialPoll>
      = RepositoryHolder::new();

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_poll_1nulls_1fromRust_00024createPoll<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmPoll<'local> {
      use panoptiqon::convert_jvm::CloneIntoJvm;
      use crate::poll::{Poll, PollId, PollLocalId, PollOption};

      let instance = save_instance(&mut env, &poll_nulls_fromRust_instance_repo);
      let instance_url = instance.get().url.clone();

      let no_credential = NoCredentialPoll {
         id: PollId {
            instance_url: instance_url.clone(),
            local: PollLocalId("poll id".to_string()),
         },
         expire_time: None,
         is_expired: None,
         allows_multiple_choices: None,
         vote_count: None,
         voter_count: None,
         poll_options: vec![
            PollOption {
               title: None,
               vote_count: None,
            },
         ],
         emojis: vec![],
      };

      let no_credential = poll_nulls_fromRust_no_credential_poll_repo
         .write(&mut env).unwrap()
         .save(no_credential);

      let poll = Poll {
         id: PollId {
            instance_url: instance_url.clone(),
            local: PollLocalId("poll id".to_string()),
         },
         no_credential,
         is_voted: None,
         voted_options: vec![],
      };

      poll.clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_poll_1nulls_1fromRust_00024getInstanceCache<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmCache<'local, JvmInstance<'local>> {
      use panoptiqon::convert_jvm::CloneIntoJvm;

      let instance = poll_nulls_fromRust_instance_repo.read(&mut env).unwrap()
         .load("https://example.com/instance/url".parse().unwrap()).unwrap();

      instance.clone_into_jvm(&mut env)
   }

   #[allow(non_upper_case_globals)]
   static previewCard_toRust_instance_repo: RepositoryHolder<Instance> = RepositoryHolder::new();

   #[allow(non_upper_case_globals)]
   static previewCard_toRust_account_repo: RepositoryHolder<Account> = RepositoryHolder::new();

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_previewCard_1toRust_00024createInstance<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmCache<'local, JvmInstance<'local>> {
      use panoptiqon::convert_jvm::CloneIntoJvm;

      save_instance(&mut env, &previewCard_toRust_instance_repo)
         .clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_previewCard_1toRust_00024saveAccount<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      account: JvmAccount<'local>
   ) -> JvmCache<'local, JvmAccount<'local>> {
      use panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm};

      let account = Account::clone_from_jvm(&mut env, &account);

      previewCard_toRust_account_repo.write(&mut env).unwrap()
         .save(account)
         .clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_previewCard_1toRust_00024assert<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      preview_card: JvmPreviewCard<'local>
   ) {
      use panoptiqon::convert_jvm::CloneFromJvm;
      use crate::account::{AccountId, AccountLocalId};
      use crate::preview_card::PreviewCard;

      let preview_card = PreviewCard::clone_from_jvm(&mut env, &preview_card);

      assert_eq!(
         preview_card.authors.iter()
            .map(|author| author.account.as_ref().map(|a| a.get().id.clone()))
            .collect::<Vec<_>>(),
         vec![
            Some(AccountId {
               instance_url: "https://example.com/instance/url".parse().unwrap(),
               local: AccountLocalId("account id0".to_string())
            }),
            Some(AccountId {
               instance_url: "https://example.com/instance/url".parse().unwrap(),
               local: AccountLocalId("account id1".to_string())
            }),
         ]
      );

      assert_eq!(
         PreviewCard {
            url: Some("https://example.com/preview/card/url".parse().unwrap()),
            title: Some("title".to_string()),
            description: Some("description".to_string()),
            card_type: Some("link".to_string()),
            authors: preview_card.authors.clone(),
            provider_name: Some("provider name".to_string()),
            provider_url: Some("https://example.com/provider/url".parse().unwrap()),
            html: Some("html".to_string()),
            width: Some(123),
            height: Some(456),
            image_url: Some("https://example.com/image/url/3".parse().unwrap()),
            embed_url: Some("https://example.com/embed/url".parse().unwrap()),
            blurhash: Some("blurhash".to_string()),
         },
         preview_card
      );
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_previewCard_1nulls_1toRust_00024assert<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      preview_card: JvmPreviewCard<'local>
   ) {
      use panoptiqon::convert_jvm::CloneFromJvm;
      use crate::preview_card::PreviewCard;

      let preview_card = PreviewCard::clone_from_jvm(&mut env, &preview_card);

      assert_eq!(
         PreviewCard {
            url: None,
            title: None,
            description: None,
            card_type: None,
            authors: vec![],
            provider_name: None,
            provider_url: None,
            html: None,
            width: None,
            height: None,
            image_url: None,
            embed_url: None,
            blurhash: None,
         },
         preview_card
      );
   }

   #[allow(non_upper_case_globals)]
   static previewCard_fromRust_instance_repo: RepositoryHolder<Instance> = RepositoryHolder::new();

   #[allow(non_upper_case_globals)]
   static previewCard_fromRust_account_repo: RepositoryHolder<Account> = RepositoryHolder::new();

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_previewCard_1fromRust_00024createPreviewCard<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmPreviewCard<'local> {
      use panoptiqon::convert_jvm::CloneIntoJvm;
      use crate::account::{Account, AccountId, AccountLocalId};
      use crate::preview_card::{PreviewCard, PreviewCardAuthor};

      let instance = save_instance(&mut env, &previewCard_fromRust_instance_repo);
      let instance_url = instance.get().url.clone();

      let preview_card = PreviewCard {
         url: Some("https://example.com/preview/card/url".parse().unwrap()),
         title: Some("title".to_string()),
         description: Some("description".to_string()),
         card_type: Some("link".to_string()),
         authors: (0..=1)
            .map(|i| {
               let account = Account {
                  instance: instance.clone(),
                  id: AccountId {
                     instance_url: instance_url.clone(),
                     local: AccountLocalId(format!("account id{i}"))
                  },
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

               let account_cache = previewCard_fromRust_account_repo
                  .write(&mut env).unwrap()
                  .save(account);

               PreviewCardAuthor {
                  name: Some("author name".to_string()),
                  url: Some("https://example.com/author".parse().unwrap()),
                  account: Some(account_cache),
               }
            })
            .collect(),
         provider_name: Some("provider name".to_string()),
         provider_url: Some("https://example.com/provider/url".parse().unwrap()),
         html: Some("html".to_string()),
         width: Some(123),
         height: Some(456),
         image_url: Some("https://example.com/image/url/3".parse().unwrap()),
         embed_url: Some("https://example.com/embed/url".parse().unwrap()),
         blurhash: Some("blurhash".to_string()),
      };

      preview_card.clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_previewCard_1nulls_1fromRust_00024createPreviewCard<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmPreviewCard<'local> {
      use panoptiqon::convert_jvm::CloneIntoJvm;
      use crate::preview_card::PreviewCard;

      let preview_card = PreviewCard {
         url: None,
         title: None,
         description: None,
         card_type: None,
         authors: vec![],
         provider_name: None,
         provider_url: None,
         html: None,
         width: None,
         height: None,
         image_url: None,
         embed_url: None,
         blurhash: None,
      };

      preview_card.clone_into_jvm(&mut env)
   }

   #[allow(non_upper_case_globals)]
   static role_toRust_instance_repo: RepositoryHolder<Instance> = RepositoryHolder::new();

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_role_1toRust_00024createInstance<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmCache<'local, JvmInstance<'local>> {
      use panoptiqon::convert_jvm::CloneIntoJvm;

      save_instance(&mut env, &role_toRust_instance_repo)
         .clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_role_1toRust_00024assert<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      role: JvmRole<'local>
   ) {
      use panoptiqon::convert_jvm::CloneFromJvm;
      use crate::role::{Role, RoleId};

      let role = Role::clone_from_jvm(&mut env, &role);

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
   static role_nulls_toRust_instance_repo: RepositoryHolder<Instance> = RepositoryHolder::new();

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_role_1nulls_1toRust_00024createInstance<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmCache<'local, JvmInstance<'local>> {
      use panoptiqon::convert_jvm::CloneIntoJvm;

      save_instance(&mut env, &role_nulls_toRust_instance_repo)
         .clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_role_1nulls_1toRust_00024assert<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      role: JvmRole<'local>
   ) {
      use panoptiqon::convert_jvm::CloneFromJvm;
      use crate::role::Role;

      let role = Role::clone_from_jvm(&mut env, &role);

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
   static role_fromRust_instance_repo: RepositoryHolder<Instance> = RepositoryHolder::new();

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_role_1fromRust_00024createRole<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmRole<'local> {
      use panoptiqon::convert_jvm::CloneIntoJvm;
      use crate::role::{Role, RoleId};

      let role = Role {
         instance: save_instance(&mut env, &role_fromRust_instance_repo),
         id: Some(RoleId("id".to_string())),
         name: Some("name".to_string()),
         color: Some("color".to_string()),
         permissions: Some("permissions".to_string()),
         is_highlighted: Some(true),
      };

      role.clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_role_1fromRust_00024createInstance<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmCache<'local, JvmInstance<'local>> {
      use panoptiqon::convert_jvm::CloneIntoJvm;

      save_instance(&mut env, &role_fromRust_instance_repo)
         .clone_into_jvm(&mut env)
   }

   #[allow(non_upper_case_globals)]
   static role_nulls_fromRust_instance_repo: RepositoryHolder<Instance> = RepositoryHolder::new();

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_role_1nulls_1fromRust_00024createRole<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmRole<'local> {
      use panoptiqon::convert_jvm::CloneIntoJvm;
      use crate::role::Role;

      let role = Role {
         instance: save_instance(&mut env, &role_nulls_fromRust_instance_repo),
         id: None,
         name: None,
         color: None,
         permissions: None,
         is_highlighted: None,
      };

      role.clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_role_1nulls_1fromRust_00024createInstance<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmCache<'local, JvmInstance<'local>> {
      use panoptiqon::convert_jvm::CloneIntoJvm;

      save_instance(&mut env, &role_nulls_fromRust_instance_repo)
         .clone_into_jvm(&mut env)
   }

   #[allow(non_upper_case_globals)]
   static status_toRust_instance_repo: RepositoryHolder<Instance> = RepositoryHolder::new();

   #[allow(non_upper_case_globals)]
   static status_toRust_account_repo: RepositoryHolder<Account> = RepositoryHolder::new();

   #[allow(non_upper_case_globals)]
   static status_toRust_noCredentialStatus_repo: RepositoryHolder<NoCredentialStatus> = RepositoryHolder::new();

   #[allow(non_upper_case_globals)]
   static status_toRust_status_repo: RepositoryHolder<Status> = RepositoryHolder::new();

   #[allow(non_upper_case_globals)]
   static status_toRust_noCredentialPoll_repo: RepositoryHolder<NoCredentialPoll> = RepositoryHolder::new();

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_status_1toRust_00024createInstance<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmCache<'local, JvmInstance<'local>> {
      use panoptiqon::convert_jvm::CloneIntoJvm;

      save_instance(&mut env, &status_toRust_instance_repo)
         .clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_status_1toRust_00024createAccount<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmCache<'local, JvmAccount<'local>> {
      use panoptiqon::convert_jvm::CloneIntoJvm;

      save_account(&mut env, &status_toRust_account_repo, &status_toRust_instance_repo)
         .clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_status_1toRust_00024createBoostedStatus<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmCache<'local, JvmStatus<'local>> {
      use panoptiqon::convert_jvm::CloneIntoJvm;
      use crate::status::{StatusId, StatusLocalId};

      let status = Status {
         id: StatusId {
            instance_url: "https://example.com/instance/url".parse().unwrap(),
            local: StatusLocalId("boosted status id".to_string())
         },
         no_credential: status_toRust_noCredentialStatus_repo.write(&mut env).unwrap().save(
            NoCredentialStatus {
               id: StatusId {
                  instance_url: "https://example.com/instance/url".parse().unwrap(),
                  local: StatusLocalId("boosted status id".to_string())
               },
               uri: None,
               created_time: None,
               account: None,
               content: None,
               visibility: None,
               is_sensitive: None,
               spoiler_text: None,
               media_attachments: vec![],
               application: None,
               mentions: vec![],
               hashtags: vec![],
               emojis: vec![],
               boost_count: None,
               favorite_count: None,
               reply_count: None,
               url: None,
               replied_status_id: None,
               replied_account_id: None,
               boosted_status: None,
               poll: None,
               card: None,
               language: None,
               text: None,
               edited_time: None,
            }
         ),
         boosted_status: None,
         poll: None,
         is_favorited: None,
         is_boosted: None,
         is_muted: None,
         is_bookmarked: None,
         is_pinned: None,
         filter_results: vec![],
      };

      status_toRust_status_repo.write(&mut env).unwrap().save(status)
         .clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_status_1toRust_00024createNoCredentialPoll<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmCache<'local, JvmPollNoCredential<'local>> {
      use panoptiqon::convert_jvm::CloneIntoJvm;
      use crate::poll::{PollId, PollLocalId};

      let no_credential_poll = NoCredentialPoll {
         id: PollId {
            instance_url: "https://example.com/instance/url".parse().unwrap(),
            local: PollLocalId("poll id".to_string())
         },
         expire_time: None,
         is_expired: None,
         allows_multiple_choices: None,
         vote_count: None,
         voter_count: None,
         poll_options: vec![],
         emojis: vec![],
      };

      status_toRust_noCredentialPoll_repo.write(&mut env).unwrap().save(no_credential_poll)
         .clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_status_1toRust_00024createPreviewCardAuthorAccount<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmCache<'local, JvmAccount<'local>> {
      use panoptiqon::convert_jvm::CloneIntoJvm;
      use crate::account::{AccountId, AccountLocalId};

      let instance = status_toRust_instance_repo.read(&mut env).unwrap()
         .load("https://example.com/instance/url".parse().unwrap()).unwrap();

      let instance_url = instance.get().url.clone();

      let account = Account {
         instance,
         id: AccountId {
            instance_url,
            local: AccountLocalId("account id".to_string()),
         },
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

      status_toRust_account_repo.write(&mut env).unwrap().save(account)
         .clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_status_1toRust_00024assertNoCredential<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      no_credential_status: JvmStatusNoCredential<'local>
   ) {
      use std::time::Duration;
      use chrono::{TimeZone, Utc};
      use isolang::Language;
      use panoptiqon::convert_jvm::CloneFromJvm;
      use crate::account::{AccountId, AccountLocalId};
      use crate::application::Application;
      use crate::custom_emoji::CustomEmoji;
      use crate::media_attachment::{
         ImageFocus, ImageSize, MediaAttachment, MediaAttachmentId,
         MediaAttachmentMetadata, VideoSize,
      };
      use crate::poll::{PollId, PollLocalId};
      use crate::preview_card::{PreviewCard, PreviewCardAuthor};
      use crate::status::{
         StatusHashtag, StatusId, StatusLocalId, StatusMention, StatusVisibility,
      };

      let no_credential_status = NoCredentialStatus::clone_from_jvm(
         &mut env, &no_credential_status
      );

      assert_eq!(
         NoCredentialStatus {
            id: StatusId {
               instance_url: "https://example.com/instance/url".parse().unwrap(),
               local: StatusLocalId("status id".to_string())
            },
            uri: Some("uri".to_string()),
            created_time: Some(Utc.with_ymd_and_hms(2000, 1, 1, 0, 0, 0).unwrap()),
            account: {
               let id = AccountId {
                  instance_url: "https://example.com/instance/url".parse().unwrap(),
                  local: AccountLocalId("account id".to_string())
               };

               let account = status_toRust_account_repo
                  .read(&mut env).unwrap()
                  .load(id).unwrap();

               Some(account)
            },
            content: Some("content".to_string()),
            visibility: Some(StatusVisibility("public".to_string())),
            is_sensitive: Some(true),
            spoiler_text: Some("spoilerText".to_string()),
            media_attachments: vec![
               MediaAttachment {
                  id: MediaAttachmentId("media attachment id1".to_string()),
                  url: Some("https://example.com/media/attachment/1".parse().unwrap()),
                  preview_url: Some("https://example.com/preview/1".parse().unwrap()),
                  remote_url: Some("https://example.com/remote/1".parse().unwrap()),
                  metadata: Some(MediaAttachmentMetadata::Image {
                     original_size: Some(ImageSize {
                        width: 100,
                        height: 200,
                     }),
                     small_size: Some(ImageSize {
                        width: 10,
                        height: 20,
                     }),
                     focus: Some(ImageFocus {
                        x: 0.1,
                        y: 0.2,
                     }),
                  }),
                  description: Some("description".to_string()),
                  blurhash: Some("blurhash".to_string()),
               },
               MediaAttachment {
                  id: MediaAttachmentId("media attachment id2".to_string()),
                  url: Some("https://example.com/media/attachment/2".parse().unwrap()),
                  preview_url: Some("https://example.com/preview/2".parse().unwrap()),
                  remote_url: Some("https://example.com/remote/2".parse().unwrap()),
                  metadata: Some(MediaAttachmentMetadata::Gifv {
                     original_size: Some(VideoSize {
                        width: Some(100),
                        height: Some(200),
                        frame_rate: Some("frameRate".to_string()),
                        duration: Some(Duration::from_secs(12)),
                        bitrate: Some(34),
                     }),
                     small_size: Some(ImageSize {
                        width: 10,
                        height: 20,
                     }),
                     length: Some("length".to_string()),
                     fps: Some(30),
                  }),
                  description: Some("description".to_string()),
                  blurhash: Some("blurhash".to_string()),
               },
            ],
            application: Some(Application {
               instance: {
                  let instance_url
                     = "https://example.com/instance/url".parse().unwrap();

                  status_toRust_instance_repo.read(&mut env).unwrap()
                     .load(instance_url).unwrap()
               },
               name: "app name".to_string(),
               website: Some("https://example.com/app".parse().unwrap()),
               scopes: vec!["read".to_string(), "write".to_string()],
               redirect_uris: vec!["redirectUri".to_string()],
               client_id: Some("clientId".to_string()),
               client_secret: Some("clientSecret".to_string()),
               client_secret_expire_time:
                  Some(Utc.with_ymd_and_hms(2000, 1, 2, 0, 0, 0).unwrap()),
            }),
            mentions: vec![
               StatusMention {
                  mentioned_account_id: Some(AccountId {
                     instance_url: "https://example.com/instance/url".parse().unwrap(),
                     local: AccountLocalId("mentioned account id1".to_string())
                  }),
                  mentioned_account_username:
                     Some("mentioned account username1".to_string()),
                  mentioned_account_url:
                     Some("https://example.com/mentioned/account/1".parse().unwrap()),
                  mentioned_account_acct: Some("mentioned account acct1".to_string()),
               },
               StatusMention {
                  mentioned_account_id: Some(AccountId {
                     instance_url: "https://example.com/instance/url".parse().unwrap(),
                     local: AccountLocalId("mentioned account id2".to_string())
                  }),
                  mentioned_account_username:
                     Some("mentioned account username2".to_string()),
                  mentioned_account_url:
                     Some("https://example.com/mentioned/account/2".parse().unwrap()),
                  mentioned_account_acct: Some("mentioned account acct2".to_string()),
               },
            ],
            hashtags: vec![
               StatusHashtag {
                  name: Some("hashtag1".to_string()),
                  url: Some("https://example.com/hashtag1".parse().unwrap()),
               },
               StatusHashtag {
                  name: Some("hashtag2".to_string()),
                  url: Some("https://example.com/hashtag2".parse().unwrap()),
               },
            ],
            emojis: vec![
               CustomEmoji {
                  instance: {
                     let instance_url
                        = "https://example.com/instance/url".parse().unwrap();

                     status_toRust_instance_repo.read(&mut env).unwrap()
                        .load(instance_url).unwrap()
                  },
                  shortcode: "shortcode".to_string(),
                  image_url: "https://example.com/image/url".parse().unwrap(),
                  static_image_url:
                     Some("https://example.com/static/image/url".parse().unwrap()),
                  is_visible_in_picker: Some(true),
                  category: Some("category".to_string()),
               },
            ],
            boost_count: Some(1),
            favorite_count: Some(2),
            reply_count: Some(3),
            url: Some("https://example.com/status".parse().unwrap()),
            replied_status_id: Some(StatusId {
               instance_url: "https://example.com/instance/url".parse().unwrap(),
               local: StatusLocalId("replied status id".to_string())
            }),
            replied_account_id: Some(AccountId {
               instance_url: "https://example.com/instance/url".parse().unwrap(),
               local: AccountLocalId("replied account id".to_string())
            }),
            boosted_status: {
               let id = StatusId {
                  instance_url: "https://example.com/instance/url".parse().unwrap(),
                  local: StatusLocalId("boosted status id".to_string())
               };

               let boosted_status = status_toRust_noCredentialStatus_repo
                  .read(&mut env).unwrap()
                  .load(id).unwrap();

               Some(boosted_status)
            },
            poll: {
               let id = PollId {
                  instance_url: "https://example.com/instance/url".parse().unwrap(),
                  local: PollLocalId("poll id".to_string())
               };

               let no_credential_poll = status_toRust_noCredentialPoll_repo
                  .read(&mut env).unwrap()
                  .load(id).unwrap();

               Some(no_credential_poll)
            },
            card: Some(PreviewCard {
               url: Some("https://example.com/preview/card/url".parse().unwrap()),
               title: Some("title".to_string()),
               description: Some("description".to_string()),
               card_type: Some("link".to_string()),
               authors: {
                  let id = AccountId {
                     instance_url: "https://example.com/instance/url".parse().unwrap(),
                     local: AccountLocalId("account id".to_string()),
                  };

                  let account = status_toRust_account_repo
                     .read(&mut env).unwrap()
                     .load(id).unwrap();

                  vec![
                     PreviewCardAuthor {
                        name: Some("author name".to_string()),
                        url: Some("https://example.com/author".parse().unwrap()),
                        account: Some(account),
                     }
                  ]
               },
               provider_name: Some("provider name".to_string()),
               provider_url: Some("https://example.com/provider/url".parse().unwrap()),
               html: Some("html".to_string()),
               width: Some(123),
               height: Some(456),
               image_url: Some("https://example.com/image/url/3".parse().unwrap()),
               embed_url: Some("https://example.com/embed/url".parse().unwrap()),
               blurhash: Some("blurhash".to_string()),
            }),
            language: Some(Language::from_639_1("ja").unwrap()),
            text: Some("text".to_string()),
            edited_time: Some(Utc.with_ymd_and_hms(2000, 1, 4, 0, 0, 0).unwrap()),
         },
         no_credential_status
      )
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_status_1toRust_00024saveNoCredential<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      no_credential_status: JvmStatusNoCredential<'local>
   ) -> JvmCache<'local, JvmStatusNoCredential<'local>> {
      use panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm};

      let no_credential_status = NoCredentialStatus::clone_from_jvm(
         &mut env, &no_credential_status
      );

      status_toRust_noCredentialStatus_repo.write(&mut env).unwrap()
         .save(no_credential_status)
         .clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_status_1toRust_00024assert<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      status: JvmStatus<'local>
   ) {
      use chrono::{TimeZone, Utc};
      use panoptiqon::convert_jvm::CloneFromJvm;
      use crate::filter::{
         Filter, FilterAction, FilterContext, FilterId, FilterKeyword,
         FilterKeywordId, FilterResult, FilterStatus, FilterStatusId,
      };
      use crate::poll::{Poll, PollId, PollLocalId};
      use crate::status::{StatusId, StatusLocalId};

      let status = Status::clone_from_jvm(&mut env, &status);

      assert_eq!(
         Status {
            id: StatusId {
               instance_url: "https://example.com/instance/url".parse().unwrap(),
               local: StatusLocalId("status id".to_string())
            },
            no_credential: {
               let id = StatusId {
                  instance_url: "https://example.com/instance/url".parse().unwrap(),
                  local: StatusLocalId("status id".to_string())
               };

               status_toRust_noCredentialStatus_repo.read(&mut env).unwrap()
                  .load(id).unwrap()
            },
            boosted_status: {
               let id = StatusId {
                  instance_url: "https://example.com/instance/url".parse().unwrap(),
                  local: StatusLocalId("boosted status id".to_string())
               };

               let status = status_toRust_status_repo.read(&mut env).unwrap()
                  .load(id).unwrap();

               Some(status)
            },
            poll: Some(Poll {
               id: PollId {
                  instance_url: "https://example.com/instance/url".parse().unwrap(),
                  local: PollLocalId("poll id".to_string())
               },
               no_credential: {
                  let id = PollId {
                     instance_url: "https://example.com/instance/url".parse().unwrap(),
                     local: PollLocalId("poll id".to_string())
                  };

                  status_toRust_noCredentialPoll_repo
                     .read(&mut env).unwrap()
                     .load(id).unwrap()
               },
               is_voted: Some(true),
               voted_options: vec![0],
            }),
            is_favorited: Some(true),
            is_boosted: Some(false),
            is_muted: Some(true),
            is_bookmarked: Some(false),
            is_pinned: Some(true),
            filter_results: vec![
               FilterResult {
                  filter: Some(Filter {
                     id: FilterId("filter id".to_string()),
                     title: Some("title".to_string()),
                     context: vec![FilterContext("home".to_string())],
                     expire_time:
                        Some(Utc.with_ymd_and_hms(2000, 1, 5, 0, 0, 0).unwrap()),
                     filter_action: Some(FilterAction("hide".to_string())),
                     keywords: vec![
                        FilterKeyword {
                           id: FilterKeywordId("filter keyword id".to_string()),
                           keyword: Some("keyword".to_string()),
                           whole_word: Some(false),
                        },
                     ],
                     statuses: vec![
                        FilterStatus {
                           id: FilterStatusId("filter status id".to_string()),
                           status_id: StatusId {
                              instance_url: "https://example.com/instance/url".parse().unwrap(),
                              local: StatusLocalId("filtered status id".to_string())
                           },
                        }
                     ],
                  }),
                  keyword_matches: vec!["keyword".to_string()],
                  status_matches: vec![
                     StatusId {
                        instance_url: "https://example.com/instance/url".parse().unwrap(),
                        local: StatusLocalId("filtered status id".to_string())
                     },
                  ],
               }
            ],
         },
         status
      );
   }

   #[allow(non_upper_case_globals)]
   static status_nulls_toRust_instance_repo: RepositoryHolder<Instance> = RepositoryHolder::new();

   #[allow(non_upper_case_globals)]
   static status_nulls_toRust_account_repo: RepositoryHolder<Account> = RepositoryHolder::new();

   #[allow(non_upper_case_globals)]
   static status_nulls_toRust_noCredentialStatus_repo: RepositoryHolder<NoCredentialStatus> = RepositoryHolder::new();

   #[allow(non_upper_case_globals)]
   static status_nulls_toRust_status_repo: RepositoryHolder<Status> = RepositoryHolder::new();

   #[allow(non_upper_case_globals)]
   static status_nulls_toRust_noCredentialPoll_repo: RepositoryHolder<NoCredentialPoll> = RepositoryHolder::new();

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_status_1nulls_1toRust_00024createInstance<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmCache<'local, JvmInstance<'local>> {
      use panoptiqon::convert_jvm::CloneIntoJvm;

      save_instance(&mut env, &status_nulls_toRust_instance_repo)
         .clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_status_1nulls_1toRust_00024assertNoCredential<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      no_credential_status: JvmStatusNoCredential<'local>
   ) {
      use panoptiqon::convert_jvm::CloneFromJvm;
      use crate::status::{StatusId, StatusLocalId};

      let no_credential_status = NoCredentialStatus::clone_from_jvm(
         &mut env, &no_credential_status
      );

      assert_eq!(
         NoCredentialStatus {
            id: StatusId {
               instance_url: "https://example.com/instance/url".parse().unwrap(),
               local: StatusLocalId("status id".to_string())
            },
            uri: None,
            created_time: None,
            account: None,
            content: None,
            visibility: None,
            is_sensitive: None,
            spoiler_text: None,
            media_attachments: vec![],
            application: None,
            mentions: vec![],
            hashtags: vec![],
            emojis: vec![],
            boost_count: None,
            favorite_count: None,
            reply_count: None,
            url: None,
            replied_status_id: None,
            replied_account_id: None,
            boosted_status: None,
            poll: None,
            card: None,
            language: None,
            text: None,
            edited_time: None,
         },
         no_credential_status
      )
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_status_1nulls_1toRust_00024saveNoCredential<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      no_credential_status: JvmStatusNoCredential<'local>
   ) -> JvmCache<'local, JvmStatusNoCredential<'local>> {
      use panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm};

      let no_credential_status = NoCredentialStatus::clone_from_jvm(
         &mut env, &no_credential_status
      );

      status_nulls_toRust_noCredentialStatus_repo.write(&mut env).unwrap()
         .save(no_credential_status)
         .clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_status_1nulls_1toRust_00024assert<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      status: JvmStatus<'local>
   ) {
      use panoptiqon::convert_jvm::CloneFromJvm;
      use crate::status::{StatusId, StatusLocalId};

      let status = Status::clone_from_jvm(&mut env, &status);

      assert_eq!(
         Status {
            id: StatusId {
               instance_url: "https://example.com/instance/url".parse().unwrap(),
               local: StatusLocalId("status id".to_string())
            },
            no_credential: {
               let id = StatusId {
                  instance_url: "https://example.com/instance/url".parse().unwrap(),
                  local: StatusLocalId("status id".to_string())
               };

               status_nulls_toRust_noCredentialStatus_repo.read(&mut env).unwrap()
                  .load(id).unwrap()
            },
            boosted_status: None,
            poll: None,
            is_favorited: None,
            is_boosted: None,
            is_muted: None,
            is_bookmarked: None,
            is_pinned: None,
            filter_results: vec![],
         },
         status
      );
   }

   #[allow(non_upper_case_globals)]
   static status_fromRust_instance_repo: RepositoryHolder<Instance> = RepositoryHolder::new();

   #[allow(non_upper_case_globals)]
   static status_fromRust_account_repo: RepositoryHolder<Account> = RepositoryHolder::new();

   #[allow(non_upper_case_globals)]
   static status_fromRust_noCredentialStatus_repo: RepositoryHolder<NoCredentialStatus> = RepositoryHolder::new();

   #[allow(non_upper_case_globals)]
   static status_fromRust_status_repo: RepositoryHolder<Status> = RepositoryHolder::new();

   #[allow(non_upper_case_globals)]
   static status_fromRust_noCredentialPoll_repo: RepositoryHolder<NoCredentialPoll> = RepositoryHolder::new();

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_status_1fromRust_00024createStatus<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmStatus<'local> {
      use std::time::Duration;
      use chrono::{TimeZone, Utc};
      use isolang::Language;
      use panoptiqon::convert_jvm::CloneIntoJvm;
      use crate::account::{AccountId, AccountLocalId};
      use crate::application::Application;
      use crate::custom_emoji::CustomEmoji;
      use crate::filter::{
         Filter, FilterAction, FilterContext, FilterId, FilterKeyword,
         FilterKeywordId, FilterResult, FilterStatus, FilterStatusId,
      };
      use crate::media_attachment::{
         ImageFocus, ImageSize, MediaAttachment, MediaAttachmentId,
         MediaAttachmentMetadata, VideoSize,
      };
      use crate::poll::{Poll, PollId, PollLocalId};
      use crate::preview_card::{PreviewCard, PreviewCardAuthor};
      use crate::status::{
         StatusHashtag, StatusId, StatusLocalId, StatusMention, StatusVisibility,
      };

      let instance = save_instance(&mut env, &status_fromRust_instance_repo);

      let boosted_status = Status {
         id: StatusId {
            instance_url: "https://example.com/instance/url".parse().unwrap(),
            local: StatusLocalId("boosted status id".to_string())
         },
         no_credential: status_toRust_noCredentialStatus_repo.write(&mut env).unwrap().save(
            NoCredentialStatus {
               id: StatusId {
                  instance_url: "https://example.com/instance/url".parse().unwrap(),
                  local: StatusLocalId("boosted status id".to_string())
               },
               uri: None,
               created_time: None,
               account: None,
               content: None,
               visibility: None,
               is_sensitive: None,
               spoiler_text: None,
               media_attachments: vec![],
               application: None,
               mentions: vec![],
               hashtags: vec![],
               emojis: vec![],
               boost_count: None,
               favorite_count: None,
               reply_count: None,
               url: None,
               replied_status_id: None,
               replied_account_id: None,
               boosted_status: None,
               poll: None,
               card: None,
               language: None,
               text: None,
               edited_time: None,
            }
         ),
         boosted_status: None,
         poll: None,
         is_favorited: None,
         is_boosted: None,
         is_muted: None,
         is_bookmarked: None,
         is_pinned: None,
         filter_results: vec![],
      };

      let no_credential_poll = status_fromRust_noCredentialPoll_repo
         .write(&mut env).unwrap()
         .save(
            NoCredentialPoll {
               id: PollId {
                  instance_url: "https://example.com/instance/url".parse().unwrap(),
                  local: PollLocalId("poll id".to_string())
               },
               expire_time: None,
               is_expired: None,
               allows_multiple_choices: None,
               vote_count: None,
               voter_count: None,
               poll_options: vec![],
               emojis: vec![],
            }
         );

      let no_credential = NoCredentialStatus {
         id: StatusId {
            instance_url: "https://example.com/instance/url".parse().unwrap(),
            local: StatusLocalId("status id".to_string())
         },
         uri: Some("uri".to_string()),
         created_time: Some(Utc.with_ymd_and_hms(2000, 1, 1, 0, 0, 0).unwrap()),
         account: Some(
            save_account(
               &mut env,
               &status_fromRust_account_repo,
               &status_fromRust_instance_repo
            )
         ),
         content: Some("content".to_string()),
         visibility: Some(StatusVisibility("public".to_string())),
         is_sensitive: Some(true),
         spoiler_text: Some("spoilerText".to_string()),
         media_attachments: vec![
            MediaAttachment {
               id: MediaAttachmentId("media attachment id1".to_string()),
               url: Some("https://example.com/media/attachment/1".parse().unwrap()),
               preview_url: Some("https://example.com/preview/1".parse().unwrap()),
               remote_url: Some("https://example.com/remote/1".parse().unwrap()),
               metadata: Some(MediaAttachmentMetadata::Image {
                  original_size: Some(ImageSize {
                     width: 100,
                     height: 200,
                  }),
                  small_size: Some(ImageSize {
                     width: 10,
                     height: 20,
                  }),
                  focus: Some(ImageFocus {
                     x: 0.1,
                     y: 0.2,
                  }),
               }),
               description: Some("description".to_string()),
               blurhash: Some("blurhash".to_string()),
            },
            MediaAttachment {
               id: MediaAttachmentId("media attachment id2".to_string()),
               url: Some("https://example.com/media/attachment/2".parse().unwrap()),
               preview_url: Some("https://example.com/preview/2".parse().unwrap()),
               remote_url: Some("https://example.com/remote/2".parse().unwrap()),
               metadata: Some(MediaAttachmentMetadata::Gifv {
                  original_size: Some(VideoSize {
                     width: Some(100),
                     height: Some(200),
                     frame_rate: Some("frameRate".to_string()),
                     duration: Some(Duration::from_secs(12)),
                     bitrate: Some(34),
                  }),
                  small_size: Some(ImageSize {
                     width: 10,
                     height: 20,
                  }),
                  length: Some("length".to_string()),
                  fps: Some(30),
               }),
               description: Some("description".to_string()),
               blurhash: Some("blurhash".to_string()),
            },
         ],
         application: Some(Application {
            instance: instance.clone(),
            name: "app name".to_string(),
            website: Some("https://example.com/app".parse().unwrap()),
            scopes: vec!["read".to_string(), "write".to_string()],
            redirect_uris: vec!["redirectUri".to_string()],
            client_id: Some("clientId".to_string()),
            client_secret: Some("clientSecret".to_string()),
            client_secret_expire_time:
            Some(Utc.with_ymd_and_hms(2000, 1, 2, 0, 0, 0).unwrap()),
         }),
         mentions: vec![
            StatusMention {
               mentioned_account_id: Some(AccountId {
                  instance_url: "https://example.com/instance/url".parse().unwrap(),
                  local: AccountLocalId("mentioned account id1".to_string())
               }),
               mentioned_account_username:
               Some("mentioned account username1".to_string()),
               mentioned_account_url:
               Some("https://example.com/mentioned/account/1".parse().unwrap()),
               mentioned_account_acct: Some("mentioned account acct1".to_string()),
            },
            StatusMention {
               mentioned_account_id: Some(AccountId {
                  instance_url: "https://example.com/instance/url".parse().unwrap(),
                  local: AccountLocalId("mentioned account id2".to_string())
               }),
               mentioned_account_username:
               Some("mentioned account username2".to_string()),
               mentioned_account_url:
               Some("https://example.com/mentioned/account/2".parse().unwrap()),
               mentioned_account_acct: Some("mentioned account acct2".to_string()),
            },
         ],
         hashtags: vec![
            StatusHashtag {
               name: Some("hashtag1".to_string()),
               url: Some("https://example.com/hashtag1".parse().unwrap()),
            },
            StatusHashtag {
               name: Some("hashtag2".to_string()),
               url: Some("https://example.com/hashtag2".parse().unwrap()),
            },
         ],
         emojis: vec![
            CustomEmoji {
               instance: instance.clone(),
               shortcode: "shortcode".to_string(),
               image_url: "https://example.com/image/url".parse().unwrap(),
               static_image_url:
               Some("https://example.com/static/image/url".parse().unwrap()),
               is_visible_in_picker: Some(true),
               category: Some("category".to_string()),
            },
         ],
         boost_count: Some(1),
         favorite_count: Some(2),
         reply_count: Some(3),
         url: Some("https://example.com/status".parse().unwrap()),
         replied_status_id: Some(StatusId {
            instance_url: "https://example.com/instance/url".parse().unwrap(),
            local: StatusLocalId("replied status id".to_string())
         }),
         replied_account_id: Some(AccountId {
            instance_url: "https://example.com/instance/url".parse().unwrap(),
            local: AccountLocalId("replied account id".to_string())
         }),
         boosted_status: Some(boosted_status.no_credential.clone()),
         poll: Some(no_credential_poll.clone()),
         card: Some(PreviewCard {
            url: Some("https://example.com/preview/card/url".parse().unwrap()),
            title: Some("title".to_string()),
            description: Some("description".to_string()),
            card_type: Some("link".to_string()),
            authors: {
               let account = Account {
                  instance: instance.clone(),
                  id: AccountId {
                     instance_url: "https://example.com/instance/url".parse().unwrap(),
                     local: AccountLocalId("account id".to_string()),
                  },
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

               let account_cache = status_fromRust_account_repo
                  .write(&mut env).unwrap()
                  .save(account);

               vec![
                  PreviewCardAuthor {
                     name: Some("author name".to_string()),
                     url: Some("https://example.com/author".parse().unwrap()),
                     account: Some(account_cache),
                  }
               ]
            },
            provider_name: Some("provider name".to_string()),
            provider_url: Some("https://example.com/provider/url".parse().unwrap()),
            html: Some("html".to_string()),
            width: Some(123),
            height: Some(456),
            image_url: Some("https://example.com/image/url/3".parse().unwrap()),
            embed_url: Some("https://example.com/embed/url".parse().unwrap()),
            blurhash: Some("blurhash".to_string()),
         }),
         language: Some(Language::from_639_1("ja").unwrap()),
         text: Some("text".to_string()),
         edited_time: Some(Utc.with_ymd_and_hms(2000, 1, 4, 0, 0, 0).unwrap()),
      };

      let status = Status {
         id: StatusId {
            instance_url: "https://example.com/instance/url".parse().unwrap(),
            local: StatusLocalId("status id".to_string())
         },
         no_credential: status_fromRust_noCredentialStatus_repo
            .write(&mut env).unwrap()
            .save(no_credential),
         boosted_status: Some(
            status_fromRust_status_repo
               .write(&mut env).unwrap()
               .save(boosted_status)
         ),
         poll: Some(Poll {
            id: PollId {
               instance_url: "https://example.com/instance/url".parse().unwrap(),
               local: PollLocalId("poll id".to_string())
            },
            no_credential: no_credential_poll.clone(),
            is_voted: Some(true),
            voted_options: vec![0],
         }),
         is_favorited: Some(true),
         is_boosted: Some(false),
         is_muted: Some(true),
         is_bookmarked: Some(false),
         is_pinned: Some(true),
         filter_results: vec![
            FilterResult {
               filter: Some(Filter {
                  id: FilterId("filter id".to_string()),
                  title: Some("title".to_string()),
                  context: vec![FilterContext("home".to_string())],
                  expire_time:
                  Some(Utc.with_ymd_and_hms(2000, 1, 5, 0, 0, 0).unwrap()),
                  filter_action: Some(FilterAction("hide".to_string())),
                  keywords: vec![
                     FilterKeyword {
                        id: FilterKeywordId("filter keyword id".to_string()),
                        keyword: Some("keyword".to_string()),
                        whole_word: Some(false),
                     },
                  ],
                  statuses: vec![
                     FilterStatus {
                        id: FilterStatusId("filter status id".to_string()),
                        status_id: StatusId {
                           instance_url: "https://example.com/instance/url".parse().unwrap(),
                           local: StatusLocalId("filtered status id".to_string())
                        },
                     }
                  ],
               }),
               keyword_matches: vec!["keyword".to_string()],
               status_matches: vec![
                  StatusId {
                     instance_url: "https://example.com/instance/url".parse().unwrap(),
                     local: StatusLocalId("filtered status id".to_string())
                  },
               ],
            }
         ],
      };

      status.clone_into_jvm(&mut env)
   }

   #[allow(non_upper_case_globals)]
   static status_nulls_fromRust_noCredentialStatus_repo: RepositoryHolder<NoCredentialStatus> = RepositoryHolder::new();

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_entity_ConvertJniTest_status_1nulls_1fromRust_00024createStatus<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmStatus<'local> {
      use panoptiqon::convert_jvm::CloneIntoJvm;
      use crate::status::{StatusId, StatusLocalId};

      let no_credential = NoCredentialStatus {
         id: StatusId {
            instance_url: "https://example.com/instance/url".parse().unwrap(),
            local: StatusLocalId("status id".to_string())
         },
         uri: None,
         created_time: None,
         account: None,
         content: None,
         visibility: None,
         is_sensitive: None,
         spoiler_text: None,
         media_attachments: vec![],
         application: None,
         mentions: vec![],
         hashtags: vec![],
         emojis: vec![],
         boost_count: None,
         favorite_count: None,
         reply_count: None,
         url: None,
         replied_status_id: None,
         replied_account_id: None,
         boosted_status: None,
         poll: None,
         card: None,
         language: None,
         text: None,
         edited_time: None,
      };

      let status = Status {
         id: StatusId {
            instance_url: "https://example.com/instance/url".parse().unwrap(),
            local: StatusLocalId("status id".to_string())
         },
         no_credential: status_nulls_fromRust_noCredentialStatus_repo
            .write(&mut env).unwrap()
            .save(no_credential),
         boosted_status: None,
         poll: None,
         is_favorited: None,
         is_boosted: None,
         is_muted: None,
         is_bookmarked: None,
         is_pinned: None,
         filter_results: vec![],
      };

      status.clone_into_jvm(&mut env)
   }
}
