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

#[cfg(not(feature = "jvm"))]
use std::marker::PhantomData;

#[cfg(feature = "jvm")]
use jni::JNIEnv;

#[cfg(not(any(test, feature = "jni-test")))]
use mastodon_webapi::api::apps;

#[cfg(any(test, feature = "jni-test"))]
mod apps {
   use std::cell::RefCell;

   use anyhow::Result;
   use reqwest::blocking::Client;
   use url::Url;

   use mastodon_webapi::entity::application::Application as ApiApplication;

   thread_local! {
      static POST_APPS_V0: RefCell<Box<dyn Fn(&Client, &Url, &str, &str, Option<&str>, Option<&str>) -> Result<ApiApplication>>>
         = RefCell::new(Box::new(|_, _, _, _, _, _| panic!()));

      static POST_APPS_V4_3_0: RefCell<Box<dyn Fn(&Client, &Url, &str, &[&str], Option<&str>, Option<&str>) -> Result<ApiApplication>>>
         = RefCell::new(Box::new(|_, _, _, _, _, _| panic!()));
   }

   pub fn post_apps_v0(
      client: &Client,
      instance_base_url: &Url,
      client_name: &str,
      redirect_uris: &str,
      scopes: Option<&str>,
      website: Option<&str>
   ) -> Result<ApiApplication> {
      POST_APPS_V0.with(|f| {
         let f = f.borrow();
         f(client, instance_base_url, client_name, redirect_uris, scopes, website)
      })
   }

   pub fn post_apps_v4_3_0(
      client: &Client,
      instance_base_url: &Url,
      client_name: &str,
      redirect_uris: &[&str],
      scopes: Option<&str>,
      website: Option<&str>
   ) -> Result<ApiApplication> {
      POST_APPS_V4_3_0.with(|f| {
         let f = f.borrow();
         f(client, instance_base_url, client_name, redirect_uris, scopes, website)
      })
   }

   #[allow(dead_code)]
   pub fn inject_post_apps_v0(
      post_app_v0: impl Fn(&Client, &Url, &str, &str, Option<&str>, Option<&str>) -> Result<ApiApplication> + 'static
   ) {
      POST_APPS_V0.set(Box::new(post_app_v0));
   }

   #[allow(dead_code)]
   pub fn inject_post_apps_v4_3_0(
      post_app_v4_3_0: impl Fn(&Client, &Url, &str, &[&str], Option<&str>, Option<&str>) -> Result<ApiApplication> + 'static
   ) {
      POST_APPS_V4_3_0.set(Box::new(post_app_v4_3_0));
   }
}

struct AppRepository<'jni> {
   #[cfg(not(feature = "jvm"))]
   env: PhantomData<&'jni ()>,
   #[cfg(feature = "jvm")]
   env: JNIEnv<'jni>
}

impl AppRepository<'_> {
   const ANDROID_REDIRECT_URI: &'static str = "https://probosqis.wcaokaze.com/auth/callback";
   const DESKTOP_REDIRECT_URI: &'static str = "urn:ietf:wg:oauth:2.0:oob";

   #[cfg(not(feature = "jvm"))]
   fn new() -> AppRepository<'static> {
      AppRepository {
         env: PhantomData
      }
   }

   #[cfg(feature = "jvm")]
   fn new<'jni>(env: &JNIEnv<'jni>) -> AppRepository<'jni> {
      AppRepository {
         env: unsafe { env.unsafe_clone() }
      }
   }

   fn post_app(
      &mut self,
      instance: Instance,
      redirect_uri: &str
   ) -> anyhow::Result<Application> {
      use ext_reqwest::CLIENT;
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

   fn get_credential_account(
      &mut self,
      token: &Token
   ) -> anyhow::Result<CredentialAccount> {
      use ext_reqwest::CLIENT;
      use mastodon_webapi::api::accounts;
      use crate::conversion;

      let instance_lock = token.instance.read()
         .map_err(|_| anyhow::format_err!("instance cache was poisoned"))?;

      let api_credential_account = accounts::get_verify_credentials(
         &CLIENT,
         &instance_lock.url,
         &token.access_token
      )?;

      let credential_account = conversion::account::credential_account_from_api(
         #[cfg(feature = "jvm")] &mut self.env,
         token.instance.clone(),
         api_credential_account
      )?;

      Ok(credential_account)
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
      use mastodon_webapi::api::oauth;
      use panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm};

      let instance_cache = get_instance_cache_from_jni(env, &instance)?;
      let client_id = String::clone_from_jvm(env, &client_id);

      let authorize_url = oauth::get_authorize_url(
         /* instance_base_url = */ &instance_cache.read().unwrap().url,
         /* response_type = */ "code",
         /* client_id = */ &client_id,
         redirect_uri,
         /* scope = */ Some("read write push"),
         /* force_login = */ None,
         /* lang = */ None
      )?;

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
      use ext_reqwest::CLIENT;
      use mastodon_webapi::api::oauth;
      use panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm};
      use crate::conversion;

      let instance_cache = get_instance_cache_from_jni(env, &instance)?;

      let code = String::clone_from_jvm(env, &code);
      let client_id = String::clone_from_jvm(env, &client_id);
      let client_secret = String::clone_from_jvm(env, &client_secret);

      let api_token = oauth::post_token(
         &CLIENT,
         /* instance_base_url */ &instance_cache.read().unwrap().url,
         /* grant_type = */ "authorization_code",
         /* code = */ Some(&code),
         /* client_id = */ &client_id,
         /* client_secret = */ &client_secret,
         redirect_uri,
         /* scope = */ Some("read write push")
      )?;

      let token = conversion::token::from_api(api_token, instance_cache)?;
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

#[cfg(test)]
mod test {
   use mastodon_webapi::entity::application::Application;
   use super::apps;
   use super::AppRepository;

   fn dummy_application() -> Application {
      Application {
         name: "app name".to_string(),
         website: None,
         client_id: None,
         client_secret: None
      }
   }

   #[test]
   fn switch_function_by_instance_version() {
      use std::sync::{Arc, Mutex};
      use chrono::DateTime;
      use mastodon_entity::instance::Instance;
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
}
