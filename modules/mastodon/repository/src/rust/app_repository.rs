/*
 * Copyright 2024 wcaokaze
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

#[cfg(feature="jvm")]
mod jvm {
   use anyhow::Result;
   use chrono::DateTime;
   use jni::JNIEnv;
   use jni::objects::{JObject, JString};

   use ext_reqwest::CLIENT;
   use ext_reqwest::unwrap_or_throw::UnwrapOrThrow;
   use mastodon_entity::application::Application;
   use mastodon_entity::instance::Instance;
   use mastodon_entity::token::Token;
   use mastodon_webapi::api::{apps, oauth};
   use mastodon_webapi::entity::application::Application as ApiApplication;
   use mastodon_webapi::entity::token::Token as ApiToken;
   use panoptiqon::cache::Cache;
   use panoptiqon::convert_java::ConvertJava;

   use crate::cache;

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_repository_DesktopAppRepository_postApp<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      instance: JObject<'local>
   ) -> JObject<'local> {
      post_app(&mut env, instance).unwrap_or_throw_io_exception(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_repository_AndroidAppRepository_postApp<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      instance: JObject<'local>
   ) -> JObject<'local> {
      post_app(&mut env, instance).unwrap_or_throw_io_exception(&mut env)
   }

   fn post_app<'local>(
      env: &mut JNIEnv<'local>,
      instance: JObject<'local>
   ) -> Result<JObject<'local>> {
      let instance = Instance::clone_from_java(env, &instance);

      let ApiApplication { name, website, client_id, client_secret } = apps::post_apps_v0(
         &CLIENT, &instance.url,
         /* client_name = */ "Probosqis",
         /* redirect_uris = */ "https://probosqis.wcaokaze.com/auth/callback",
         /* scopes = */ Some("read write push"),
         /* website = */ None
      )?;

      let instance_cache = cache::instance_repo().write(env).unwrap().save(instance);

      let application = Application {
         instance: instance_cache,
         name, website, client_id, client_secret
      };

      Ok(application.clone_into_java(env))
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_repository_DesktopAppRepository_getAuthorizeUrl<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      instance: JObject<'local>,
      client_id: JString<'local>
   ) -> JString<'local> {
      get_authorize_url(&mut env, instance, client_id)
         .unwrap_or_throw_io_exception(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_repository_AndroidAppRepository_getAuthorizeUrl<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      instance: JObject<'local>,
      client_id: JString<'local>
   ) -> JString<'local> {
      get_authorize_url(&mut env, instance, client_id)
         .unwrap_or_throw_io_exception(&mut env)
   }

   fn get_authorize_url<'local>(
      env: &mut JNIEnv<'local>,
      instance: JObject<'local>,
      client_id: JString<'local>
   ) -> Result<JString<'local>> {
      let instance_cache = get_instance_cache_from_java(env, &instance)?;
      let client_id: String = env.get_string(&client_id)?.into();

      let authorize_url = oauth::get_authorize_url(
         /* instance_base_url = */ &instance_cache.lock().unwrap().url,
         /* response_type = */ "code",
         /* client_id = */ &client_id,
         /* redirect_uri = */ "https://probosqis.wcaokaze.com/auth/callback",
         /* scope = */ Some("read write push"),
         /* force_login = */ None,
         /* lang = */ None
      )?;

      let authorize_url = env.new_string(authorize_url.as_str())?;
      Ok(authorize_url)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_repository_DesktopAppRepository_getToken<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      instance: JObject<'local>,
      code: JString<'local>,
      client_id: JString<'local>,
      client_secret: JString<'local>
   ) -> JObject<'local> {
      get_token(&mut env, instance, code, client_id, client_secret)
         .unwrap_or_throw_io_exception(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_repository_AndroidAppRepository_getToken<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      instance: JObject<'local>,
      code: JString<'local>,
      client_id: JString<'local>,
      client_secret: JString<'local>
   ) -> JObject<'local> {
      get_token(&mut env, instance, code, client_id, client_secret)
         .unwrap_or_throw_io_exception(&mut env)
   }

   fn get_token<'local>(
      env: &mut JNIEnv<'local>,
      instance: JObject<'local>,
      code: JString<'local>,
      client_id: JString<'local>,
      client_secret: JString<'local>
   ) -> Result<JObject<'local>> {
      let instance_cache = get_instance_cache_from_java(env, &instance)?;

      let code: String = env.get_string(&code)?.into();
      let client_id: String = env.get_string(&client_id)?.into();
      let client_secret: String = env.get_string(&client_secret)?.into();

      let ApiToken { access_token, token_type, scope, created_at } = oauth::post_token(
         &CLIENT,
         /* instance_base_url */ &instance_cache.lock().unwrap().url,
         /* grant_type = */ "authorization_code",
         /* code = */ Some(&code),
         /* client_id = */ &client_id,
         /* client_secret = */ &client_secret,
         /* redirect_uri = */ "https://probosqis.wcaokaze.com/auth/callback",
         /* scope = */ Some("read write push")
      )?;

      let token = Token {
         instance: instance_cache, access_token, token_type, scope,
         created_at: DateTime::from_timestamp(created_at, 0).unwrap()
      };

      Ok(token.clone_into_java(env))
   }

   fn get_instance_cache_from_java<'local>(
      env: &mut JNIEnv<'local>,
      java_instance: &JObject<'local>,
   ) -> Result<Cache<Instance>> {
      if env.is_instance_of(
            &java_instance, "com/wcaokaze/probosqis/panoptiqon/RepositoryCache")?
      {
         Ok(Cache::<Instance>::clone_from_java(env, &java_instance))
      } else {
         let instance_java_instance = env
            .call_method(&java_instance, "getValue", "()Ljava/lang/Object;", &[])?.l()?;
         let instance = Instance::clone_from_java(env, &instance_java_instance);

         let mut repo = cache::instance_repo().write(env)?;
         Ok(repo.save(instance))
      }
   }
}
