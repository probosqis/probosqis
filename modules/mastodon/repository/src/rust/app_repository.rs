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

use mastodon_entity::account::CredentialAccount;
use mastodon_entity::application::Application;
use mastodon_entity::instance::Instance;
use mastodon_entity::token::Token;
use panoptiqon::cache::Cache;
use url::Url;

#[cfg(not(feature = "jvm"))]
use std::marker::PhantomData;

#[cfg(feature = "jvm")]
use jni::JNIEnv;

pub struct AppRepository<'jni> {
   #[cfg(not(feature = "jvm"))]
   env: PhantomData<&'jni ()>,
   #[cfg(feature = "jvm")]
   env: JNIEnv<'jni>
}

impl AppRepository<'_> {
   const ANDROID_REDIRECT_URI: &'static str = "https://probosqis.wcaokaze.com/auth/callback";
   const DESKTOP_REDIRECT_URI: &'static str = "urn:ietf:wg:oauth:2.0:oob";

   #[cfg(not(feature = "jvm"))]
   pub fn new() -> AppRepository<'static> {
      AppRepository {
         env: PhantomData
      }
   }

   #[cfg(feature = "jvm")]
   pub fn new<'jni>(env: &JNIEnv<'jni>) -> AppRepository<'jni> {
      AppRepository {
         env: unsafe { env.unsafe_clone() }
      }
   }

   pub fn post_app(
      &mut self,
      instance: Instance,
      redirect_uri: &str
   ) -> anyhow::Result<Application> {
      use ext_reqwest::CLIENT;
      use mastodon_webapi::api::apps;
      use semver::Version;
      use crate::cache;
      use crate::conversion;

      let instance_version = Version::parse(&instance.version)
         .unwrap_or(Version::new(0, 0, 0));

      let api_application  = if instance_version < Version::new(4, 3, 0) {
         apps::post_apps_v0(
            &CLIENT, &instance.url,
            /* client_name = */ "Probosqis",
            /* redirect_uris = */ redirect_uri,
            /* scopes = */ Some("read write push"),
            /* website = */ None
         )?
      } else {
         apps::post_apps_v4_3_0(
            &CLIENT, &instance.url,
            /* client_name = */ "Probosqis",
            /* redirect_uris = */ &[
               Self::ANDROID_REDIRECT_URI,
               Self::DESKTOP_REDIRECT_URI,
            ],
            /* scopes = */ Some("read write push"),
            /* website = */ None
         )?
      };

      let instance_cache = cache::instance::repo()
         .write(#[cfg(feature = "jvm")] &mut self.env)?
         .save(instance);

      let application = conversion
         ::application::from_api(api_application, instance_cache)?;

      Ok(application)
   }

   pub fn get_authorize_url(
      &self,
      instance_cache: &Cache<Instance>,
      client_id: &str,
      redirect_uri: &str
   ) -> anyhow::Result<Url> {
      use mastodon_webapi::api::oauth;

      let authorize_url = {
         oauth::get_authorize_url(
            /* instance_base_url = */ &instance_cache.get().url,
            /* response_type = */ "code",
            client_id,
            redirect_uri,
            /* scope = */ Some("read write push"),
            /* force_login = */ None,
            /* lang = */ None
         )?
      };

      Ok(authorize_url)
   }

   pub fn get_token(
      &mut self,
      instance_cache: &Cache<Instance>,
      code: &str,
      client_id: &str,
      client_secret: &str,
      redirect_uri: &str
   ) -> anyhow::Result<Token> {
      use ext_reqwest::CLIENT;
      use mastodon_webapi::api::accounts;
      use mastodon_webapi::api::oauth;
      use crate::cache;
      use crate::conversion;

      let api_token = oauth::post_token(
         &CLIENT,
         /* instance_base_url */ &instance_cache.get().url,
         /* grant_type = */ "authorization_code",
         /* code = */ Some(code),
         client_id,
         client_secret,
         redirect_uri,
         /* scope = */ Some("read write push")
      )?;
      
      let api_credential_account = accounts::get_verify_credentials(
         &CLIENT,
         /* instance_base_url */ &instance_cache.get().url,
         &api_token.access_token
      )?;

      let credential_account = conversion::account::credential_account_from_api(
         #[cfg(feature = "jvm")] &mut self.env,
         instance_cache.clone(),
         api_credential_account
      )?;
      
      let credential_account = cache::account::credential_account_repo()
         .write(#[cfg(feature = "jvm")] &mut self.env)?
         .save(credential_account);
      
      let token = conversion::token::from_api(
         api_token, instance_cache.clone(), credential_account
      )?;

      Ok(token)
   }

   pub fn get_credential_account(
      &mut self,
      token: &Token
   ) -> anyhow::Result<CredentialAccount> {
      Ok(CredentialAccount::clone(&token.account.as_ref().unwrap().get()))
   }
}

#[cfg(feature = "jvm")]
mod jvm {
   use jni::JNIEnv;
   use jni::objects::JObject;
   use mastodon_entity::instance::Instance;
   use mastodon_entity::jvm_types::{
      JvmApplication, JvmCredentialAccount, JvmInstance, JvmToken,
   };
   use panoptiqon::cache::Cache;
   use panoptiqon::jvm_types::{JvmCache, JvmString};

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_repository_DesktopAppRepository_postApp<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      instance: JvmInstance<'local>
   ) -> JvmApplication<'local> {
      use ext_panoptiqon::unwrap_or_throw::UnwrapOrThrow;
      use super::AppRepository;

      post_app(&mut env, instance, AppRepository::DESKTOP_REDIRECT_URI)
         .unwrap_or_throw_io_exception(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_repository_AndroidAppRepository_postApp<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      instance: JvmInstance<'local>
   ) -> JvmApplication<'local> {
      use ext_panoptiqon::unwrap_or_throw::UnwrapOrThrow;
      use super::AppRepository;

      post_app(&mut env, instance, AppRepository::ANDROID_REDIRECT_URI)
         .unwrap_or_throw_io_exception(&mut env)
   }

   fn post_app<'local>(
      env: &mut JNIEnv<'local>,
      instance: JvmInstance<'local>,
      redirect_uri: &str
   ) -> anyhow::Result<JvmApplication<'local>> {
      use panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm};
      use super::AppRepository;

      let mut app_repository = AppRepository::new(env);

      let instance = Instance::clone_from_jvm(env, &instance);
      let application = app_repository.post_app(instance, redirect_uri)?;
      Ok(application.clone_into_jvm(env))
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_repository_DesktopAppRepository_getAuthorizeUrl<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      instance: JvmCache<'local, JvmInstance<'local>>,
      client_id: JvmString<'local>
   ) -> JvmString<'local> {
      use ext_panoptiqon::unwrap_or_throw::UnwrapOrThrow;
      use super::AppRepository;

      get_authorize_url(&mut env, instance, client_id, AppRepository::DESKTOP_REDIRECT_URI)
         .unwrap_or_throw_io_exception(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_repository_AndroidAppRepository_getAuthorizeUrl<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      instance: JvmCache<'local, JvmInstance<'local>>,
      client_id: JvmString<'local>
   ) -> JvmString<'local> {
      use ext_panoptiqon::unwrap_or_throw::UnwrapOrThrow;
      use super::AppRepository;

      get_authorize_url(&mut env, instance, client_id, AppRepository::ANDROID_REDIRECT_URI)
         .unwrap_or_throw_io_exception(&mut env)
   }

   fn get_authorize_url<'local>(
      env: &mut JNIEnv<'local>,
      instance: JvmCache<'local, JvmInstance<'local>>,
      client_id: JvmString<'local>,
      redirect_uri: &str
   ) -> anyhow::Result<JvmString<'local>> {
      use panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm};
      use super::AppRepository;

      let app_repository = AppRepository::new(env);

      let instance_cache = get_instance_cache_from_jni(env, &instance)?;
      let client_id = String::clone_from_jvm(env, &client_id);

      let authorize_url = app_repository
         .get_authorize_url(&instance_cache, &client_id, redirect_uri)?;

      let authorize_url = authorize_url.as_str().clone_into_jvm(env);
      Ok(authorize_url)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_repository_DesktopAppRepository_getToken<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      instance: JvmCache<'local, JvmInstance<'local>>,
      code: JvmString<'local>,
      client_id: JvmString<'local>,
      client_secret: JvmString<'local>
   ) -> JvmToken<'local> {
      use ext_panoptiqon::unwrap_or_throw::UnwrapOrThrow;
      use super::AppRepository;

      get_token(&mut env, instance, code, client_id, client_secret, AppRepository::DESKTOP_REDIRECT_URI)
         .unwrap_or_throw_io_exception(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_repository_AndroidAppRepository_getToken<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      instance: JvmCache<'local, JvmInstance<'local>>,
      code: JvmString<'local>,
      client_id: JvmString<'local>,
      client_secret: JvmString<'local>
   ) -> JvmToken<'local> {
      use ext_panoptiqon::unwrap_or_throw::UnwrapOrThrow;
      use super::AppRepository;

      get_token(&mut env, instance, code, client_id, client_secret, AppRepository::ANDROID_REDIRECT_URI)
         .unwrap_or_throw_io_exception(&mut env)
   }

   fn get_token<'local>(
      env: &mut JNIEnv<'local>,
      instance: JvmCache<'local, JvmInstance<'local>>,
      code: JvmString<'local>,
      client_id: JvmString<'local>,
      client_secret: JvmString<'local>,
      redirect_uri: &str
   ) -> anyhow::Result<JvmToken<'local>> {
      use panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm};
      use super::AppRepository;

      let mut app_repository = AppRepository::new(env);

      let instance_cache = get_instance_cache_from_jni(env, &instance)?;

      let code = String::clone_from_jvm(env, &code);
      let client_id = String::clone_from_jvm(env, &client_id);
      let client_secret = String::clone_from_jvm(env, &client_secret);

      let token = app_repository.get_token(
         &instance_cache, &code, &client_id, &client_secret, redirect_uri
      )?;

      Ok(token.clone_into_jvm(env))
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_repository_AndroidAppRepository_getCredentialAccount<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      token: JvmToken<'local>
   ) -> JvmCredentialAccount<'local> {
      use ext_panoptiqon::unwrap_or_throw::UnwrapOrThrow;

      get_credential_account(&mut env, token)
         .unwrap_or_throw_io_exception(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_repository_DesktopAppRepository_getCredentialAccount<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      token: JvmToken<'local>
   ) -> JvmCredentialAccount<'local> {
      use ext_panoptiqon::unwrap_or_throw::UnwrapOrThrow;

      get_credential_account(&mut env, token)
         .unwrap_or_throw_io_exception(&mut env)
   }

   fn get_credential_account<'local>(
      env: &mut JNIEnv<'local>,
      token: JvmToken<'local>
   ) -> anyhow::Result<JvmCredentialAccount<'local>> {
      use mastodon_entity::token::Token;
      use panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm};
      use super::AppRepository;

      let mut app_repository = AppRepository::new(env);

      let token = Token::clone_from_jvm(env, &token);
      let credential_account = app_repository.get_credential_account(&token)?;
      Ok(credential_account.clone_into_jvm(env))
   }

   fn get_instance_cache_from_jni<'local>(
      env: &mut JNIEnv<'local>,
      java_instance: &JvmCache<'local, JvmInstance<'local>>,
   ) -> anyhow::Result<Cache<Instance>> {
      use panoptiqon::convert_jvm::CloneFromJvm;
      use panoptiqon::jvm_type::JvmType;
      use crate::cache;

      if env.is_instance_of(
         java_instance.j_object(),
         "com/wcaokaze/probosqis/panoptiqon/RepositoryCache"
      )? {
         Ok(Cache::<Instance>::clone_from_jvm(env, &java_instance))
      } else {
         let instance_java_instance = env.call_method(
            java_instance.j_object(),
            "getValue", "()Ljava/lang/Object;", &[]
         )?.l()?;

         let jvm_instance = unsafe {
            JvmInstance::from_j_object(instance_java_instance)
         };

         let instance = Instance::clone_from_jvm(env, &jvm_instance);

         let mut repo = cache::instance::repo().write(env)?;
         Ok(repo.save(instance))
      }
   }
}

#[cfg(all(test, not(feature = "jvm")))]
mod test {
   use mastodon_webapi::entity::application::Application;
   use super::AppRepository;

   fn dummy_application() -> Application {
      Application {
         name: "app name".to_string(),
         website: None,
         scopes: None,
         redirect_uris: None,
         redirect_uri: None,
         vapid_key: None,
         client_id: None,
         client_secret: None,
         client_secret_expires_at: None,
      }
   }

   #[test]
   fn switch_function_by_instance_version() {
      use std::sync::{Arc, Mutex};
      use chrono::DateTime;
      use mastodon_entity::instance::Instance;
      use mastodon_webapi::api::apps;
      use url::Url;

      let mut repository = AppRepository::new();

      let v0_called     = Arc::new(Mutex::new(false));
      let v4_3_0_called = Arc::new(Mutex::new(false));

      {
         let v0_called = v0_called.clone();
         apps::inject_post_apps_v0(move |_, _, _, _, _, _| {
            *v0_called.lock().unwrap() = true;
            Ok(dummy_application())
         });
      }

      {
         let v4_3_0_called = v4_3_0_called.clone();
         apps::inject_post_apps_v4_3_0(move |_, _, _, _, _, _| {
            *v4_3_0_called.lock().unwrap() = true;
            Ok(dummy_application())
         });
      }

      let instance = |version: &'static str| Instance {
         url: Url::parse("https://example.com/").unwrap(),
         version: version.to_string(),
         version_checked_time: DateTime::UNIX_EPOCH
      };

      {
         let _application = repository
            .post_app(instance("4.1.0"), AppRepository::ANDROID_REDIRECT_URI);
         assert_eq!(true,  *v0_called    .lock().unwrap());
         assert_eq!(false, *v4_3_0_called.lock().unwrap());
      }

      *v0_called    .lock().unwrap() = false;
      *v4_3_0_called.lock().unwrap() = false;

      {
         let _application = repository
            .post_app(instance("4.2.0"), AppRepository::ANDROID_REDIRECT_URI);
         assert_eq!(true,  *v0_called    .lock().unwrap());
         assert_eq!(false, *v4_3_0_called.lock().unwrap());
      }

      *v0_called    .lock().unwrap() = false;
      *v4_3_0_called.lock().unwrap() = false;

      {
         let _application = repository
            .post_app(instance("4.2.9"), AppRepository::ANDROID_REDIRECT_URI);
         assert_eq!(true,  *v0_called    .lock().unwrap());
         assert_eq!(false, *v4_3_0_called.lock().unwrap());
      }

      *v0_called    .lock().unwrap() = false;
      *v4_3_0_called.lock().unwrap() = false;

      {
         let _application = repository
            .post_app(instance("4.3.0"), AppRepository::ANDROID_REDIRECT_URI);
         assert_eq!(false, *v0_called    .lock().unwrap());
         assert_eq!(true,  *v4_3_0_called.lock().unwrap());
      }

      *v0_called    .lock().unwrap() = false;
      *v4_3_0_called.lock().unwrap() = false;

      {
         let _application = repository
            .post_app(instance("4.3.1"), AppRepository::ANDROID_REDIRECT_URI);
         assert_eq!(false, *v0_called    .lock().unwrap());
         assert_eq!(true,  *v4_3_0_called.lock().unwrap());
      }

      *v0_called    .lock().unwrap() = false;
      *v4_3_0_called.lock().unwrap() = false;

      {
         let _application = repository
            .post_app(instance("4.4.0"), AppRepository::ANDROID_REDIRECT_URI);
         assert_eq!(false, *v0_called    .lock().unwrap());
         assert_eq!(true,  *v4_3_0_called.lock().unwrap());
      }
   }

   #[test]
   fn account_conversion_uses_newer_redirect_uris_field() {
      use chrono::DateTime;
      use mastodon_entity::instance::Instance;
      use mastodon_webapi::api::apps;
      use url::Url;

      let mut repository = AppRepository::new();

      let instance = Instance {
         url: Url::parse("https://example.com/").unwrap(),
         version: "0.0.0".to_string(),
         version_checked_time: DateTime::UNIX_EPOCH,
      };

      apps::inject_post_apps_v0(|_, _, _, _, _, _| {
         Ok(
            Application {
               redirect_uris: Some(vec![
                  "https://example.com/callback/newer1".to_string(),
                  "https://example.com/callback/newer2".to_string(),
               ]),
               redirect_uri: Some(
                  "https://example.com/callback/older1\n\
                   https://example.com/callback/older2".to_string()
               ),
               ..dummy_application()
            }
         )
      });

      let application = repository
         .post_app(instance.clone(), "https://example.com/callback");

      assert_eq!(
         vec![
            "https://example.com/callback/newer1".to_string(),
            "https://example.com/callback/newer2".to_string(),
         ],
         application.unwrap().redirect_uris
      );

      apps::inject_post_apps_v0(|_, _, _, _, _, _| {
         Ok(
            Application {
               redirect_uris: None,
               redirect_uri: Some(
                  "https://example.com/callback/older1\n\
                   https://example.com/callback/older2".to_string()
               ),
               ..dummy_application()
            }
         )
      });

      let application = repository
         .post_app(instance.clone(), "https://example.com/callback");

      assert_eq!(
         vec![
            "https://example.com/callback/older1".to_string(),
            "https://example.com/callback/older2".to_string(),
         ],
         application.unwrap().redirect_uris
      );
   }

   #[test]
   fn authorize_url() {
      use chrono::{TimeZone, Utc};
      use mastodon_entity::instance::Instance;
      use mastodon_webapi::api::oauth;
      use url::Url;
      use crate::cache;

      let repository = AppRepository::new();

      oauth::inject_get_authorize_url(|instance_base_url, _, _, _, _, _, _|
         Ok(instance_base_url.join("oauth/authorize")?)
      );

      let instance = Instance {
         url: "https://example.com/".parse().unwrap(),
         version: "0.0.0".to_string(),
         version_checked_time: Utc.with_ymd_and_hms(2000, 1, 1, 0, 0, 0).unwrap(),
      };

      let instance_cache = cache::instance::repo().write().unwrap().save(instance);

      let authorize_url = repository.get_authorize_url(
         &instance_cache,
         "client_id",
         "redirect_uri"
      ).unwrap();

      assert_eq!(
         Url::parse("https://example.com/oauth/authorize").unwrap(),
         authorize_url
      );
   }

   #[test]
   fn token() {
      use chrono::{TimeZone, Utc};
      use isolang::Language;
      use mastodon_entity::account::{
         Account, AccountId, AccountLocalId, AccountProfileField,
         CredentialAccount,
      };
      use mastodon_entity::custom_emoji::CustomEmoji;
      use mastodon_entity::instance::Instance;
      use mastodon_entity::status::StatusVisibility;
      use mastodon_entity::token::Token;
      use mastodon_webapi::api::accounts;
      use mastodon_webapi::api::oauth;
      use mastodon_webapi::entity::account::{
         Account as ApiAccount,
         AccountField as ApiAccountField,
         CredentialAccountSource as ApiCredentialAccountSource,
      };
      use mastodon_webapi::entity::custom_emoji::CustomEmoji as ApiCustomEmoji;
      use mastodon_webapi::entity::token::Token as ApiToken;
      use crate::cache;

      let mut repository = AppRepository::new();

      oauth::inject_post_token(|_, _, _, _, _, _, _, _|
         Ok(
            ApiToken {
               access_token: "access_token".to_string(),
               token_type: "token_type".to_string(),
               scope: "scope".to_string(),
               created_at: 0,
            }
         )
      );

      accounts::inject_get_verify_credentials(|_, _, _|
         Ok(
            ApiAccount {
               id: Some("account id".to_string()),
               username: Some("username".to_string()),
               acct: Some("acct".to_string()),
               url: Some("https://example.com/url".to_string()),
               display_name: Some("display_name".to_string()),
               note: Some("note".to_string()),
               avatar: Some("https://example.com/avatar/image/url".to_string()),
               avatar_static: Some("https://example.com/avatar/static/image/url".to_string()),
               header: Some("https://example.com/header/image/url".to_string()),
               header_static: Some("https://example.com/header/static/image/url".to_string()),
               locked: Some(false),
               fields: Some(vec![
                  ApiAccountField {
                     name: Some("name".to_string()),
                     value: Some("value".to_string()),
                     verified_at: Some("2000-01-02T00:00:00.000Z".to_string()),
                  },
               ]),
               emojis: Some(vec![
                  ApiCustomEmoji {
                     shortcode: Some("shortcode".to_string()),
                     url: Some("https://example.com/custom/emoji/url".to_string()),
                     static_url: Some("https://example.com/custom/emoji/static/url".to_string()),
                     visible_in_picker: Some(false),
                     category: Some("category".to_string()),
                  },
               ]),
               bot: Some(true),
               group: Some(false),
               discoverable: Some(true),
               noindex: Some(false),
               moved: Some(Box::new(ApiAccount {
                  id: Some("moved account id".to_string()),
                  username: None,
                  acct: None,
                  url: None,
                  display_name: None,
                  note: None,
                  avatar: None,
                  avatar_static: None,
                  header: None,
                  header_static: None,
                  locked: None,
                  fields: None,
                  emojis: None,
                  bot: None,
                  group: None,
                  discoverable: None,
                  noindex: None,
                  moved: None,
                  suspended: None,
                  limited: None,
                  created_at: None,
                  last_status_at: None,
                  statuses_count: None,
                  followers_count: None,
                  following_count: None,
                  source: None,
                  role: None,
                  mute_expires_at: None,
               })),
               suspended: Some(false),
               limited: Some(false),
               created_at: Some("2000-01-02T00:00:00.000Z".to_string()),
               last_status_at: Some("2000-01-02T00:00:00.000Z".to_string()),
               statuses_count: Some(10000),
               followers_count: Some(100),
               following_count: Some(1000),
               source: Some(ApiCredentialAccountSource {
                  note: Some("note".to_string()),
                  fields: Some(vec![
                     ApiAccountField {
                        name: Some("name".to_string()),
                        value: Some("value".to_string()),
                        verified_at: Some("2000-01-02T00:00:00.000Z".to_string()),
                     },
                  ]),
                  privacy: Some("public".to_string()),
                  sensitive: Some(false),
                  language: Some("ja".to_string()),
                  follow_requests_count: Some(1),
               }),
               role: None,
               mute_expires_at: None,
            }
         )
      );

      let instance = Instance {
         url: "https://example.com/".parse().unwrap(),
         version: "0.0.0".to_string(),
         version_checked_time: Utc.with_ymd_and_hms(2000, 1, 1, 0, 0, 0).unwrap(),
      };

      let instance_cache = cache::instance::repo().write().unwrap().save(instance);

      let token = repository.get_token(
         &instance_cache,
         "code",
         "client_id",
         "client_secret",
         "redirect_uri"
      ).unwrap();

      assert_eq!(
         Token {
            instance: instance_cache.clone(),
            account: {
               assert_eq!(
                  CredentialAccount {
                     id: AccountId {
                        instance_url: "https://example.com/".parse().unwrap(),
                        local: AccountLocalId("account id".to_string()),
                     },
                     account: {
                        assert_eq!(
                           Account {
                              instance: instance_cache.clone(),
                              id: AccountId {
                                 instance_url: "https://example.com/".parse().unwrap(),
                                 local: AccountLocalId("account id".to_string()),
                              },
                              username: Some("username".to_string()),
                              acct: Some("acct".to_string()),
                              url: Some("https://example.com/url".parse().unwrap()),
                              display_name: Some("display_name".to_string()),
                              profile_note: Some("note".to_string()),
                              avatar_image_url: Some("https://example.com/avatar/image/url".parse().unwrap()),
                              avatar_static_image_url: Some("https://example.com/avatar/static/image/url".parse().unwrap()),
                              header_image_url: Some("https://example.com/header/image/url".parse().unwrap()),
                              header_static_image_url: Some("https://example.com/header/static/image/url".parse().unwrap()),
                              is_locked: Some(false),
                              profile_fields: vec![
                                 AccountProfileField {
                                    name: Some("name".to_string()),
                                    value: Some("value".to_string()),
                                    verified_time: Some(Utc.with_ymd_and_hms(2000, 1, 2, 0, 0, 0).unwrap()),
                                 },
                              ],
                              emojis_in_profile: vec![
                                 CustomEmoji {
                                    instance: instance_cache.clone(),
                                    shortcode: "shortcode".to_string(),
                                    image_url: "https://example.com/custom/emoji/url".parse().unwrap(),
                                    static_image_url: Some("https://example.com/custom/emoji/static/url".parse().unwrap()),
                                    is_visible_in_picker: Some(false),
                                    category: Some("category".to_string()),
                                 },
                              ],
                              is_bot: Some(true),
                              is_group: Some(false),
                              is_discoverable: Some(true),
                              is_noindex: Some(false),
                              moved_to: {
                                 assert_eq!(
                                    Account {
                                       instance: instance_cache.clone(),
                                       id: AccountId {
                                          instance_url: "https://example.com/".parse().unwrap(),
                                          local: AccountLocalId("moved account id".to_string()),
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
                                    },
                                    *token.account.as_ref().unwrap().get().account.get().moved_to.as_ref().unwrap().get()
                                 );

                                 token.account.as_ref().unwrap().get().account.get().moved_to.clone()
                              },
                              is_suspended: Some(false),
                              is_limited: Some(false),
                              created_time: Some(Utc.with_ymd_and_hms(2000, 1, 2, 0, 0, 0).unwrap()),
                              last_status_post_time: Some(Utc.with_ymd_and_hms(2000, 1, 2, 0, 0, 0).unwrap()),
                              status_count: Some(10000),
                              follower_count: Some(100),
                              followee_count: Some(1000),
                           },
                           *token.account.as_ref().unwrap().get().account.get()
                        );

                        token.account.as_ref().unwrap().get().account.clone()
                     },
                     raw_profile_note: Some("note".to_string()),
                     raw_profile_fields: vec![
                        AccountProfileField {
                           name: Some("name".to_string()),
                           value: Some("value".to_string()),
                           verified_time: Some(Utc.with_ymd_and_hms(2000, 1, 2, 0, 0, 0).unwrap()),
                        },
                     ],
                     default_post_visibility: Some(StatusVisibility("public".to_string())),
                     default_post_sensitivity: Some(false),
                     default_post_language: Some(Language::from_639_1("ja").unwrap()),
                     follow_request_count: Some(1),
                     role: None,
                  },
                  *token.account.as_ref().unwrap().get()
               );

               token.account.clone()
            },
            account_id: AccountId {
               instance_url: "https://example.com/".parse().unwrap(),
               local: AccountLocalId("account id".to_string())
            },
            access_token: "access_token".to_string(),
            token_type: "token_type".to_string(),
            scope: "scope".to_string(),
            created_at: Utc.timestamp_nanos(0),
         },
         token
      );
   }
}
