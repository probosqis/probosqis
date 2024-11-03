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
   use jni::objects::{JObject, JString, JThrowable};
   use url::Url;

   use ext_reqwest::CLIENT;
   use mastodon_entity::application::Application;
   use mastodon_entity::token::Token;
   use mastodon_webapi::api::{apps, oauth};
   use mastodon_webapi::entity::application::Application as ApiApplication;
   use mastodon_webapi::entity::token::Token as ApiToken;
   use panoptiqon::convert_java::ConvertJava;

   fn throw_io_exception(env: &mut JNIEnv) {
      let exception = JThrowable::from(
         env.new_object("java/io/IOException", "()V", &[]).unwrap()
      );
      env.throw(exception).unwrap();
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_repository_DesktopAppRepository_postApp<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      instance_base_url: JString<'local>
   ) -> JObject<'local> {
      post_app(&mut env, instance_base_url)
         .unwrap_or_else(|_| {
            throw_io_exception(&mut env);
            JObject::null()
         })
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_repository_AndroidAppRepository_postApp<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      instance_base_url: JString<'local>
   ) -> JObject<'local> {
      post_app(&mut env, instance_base_url)
         .unwrap_or_else(|_| {
            throw_io_exception(&mut env);
            JObject::null()
         })
   }

   fn post_app<'local>(
      env: &mut JNIEnv<'local>,
      instance_base_url: JString<'local>
   ) -> Result<JObject<'local>> {
      let instance_base_url: String = env.get_string(&instance_base_url)?.into();
      let instance_base_url: Url = instance_base_url.parse()?;

      let ApiApplication { name, website, client_id, client_secret } = apps::post_apps(
         &CLIENT, &instance_base_url,
         /* client_name = */ "Probosqis",
         /* redirect_uris = */ "https://probosqis.wcaokaze.com/auth/callback",
         /* scopes = */ Some("read write push"),
         /* website = */ None
      )?;

      let application = Application {
         instance_base_url, name, website, client_id, client_secret
      };

      Ok(application.clone_into_java(env))
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_repository_DesktopAppRepository_getAuthorizeUrl<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      instance_base_url: JString<'local>,
      client_id: JString<'local>
   ) -> JString<'local> {
      get_authorize_url(&mut env, instance_base_url, client_id)
         .unwrap_or_else(|_| {
            throw_io_exception(&mut env);
            JObject::null().into()
         })
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_repository_AndroidAppRepository_getAuthorizeUrl<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      instance_base_url: JString<'local>,
      client_id: JString<'local>
   ) -> JString<'local> {
      get_authorize_url(&mut env, instance_base_url, client_id)
         .unwrap_or_else(|_| {
            throw_io_exception(&mut env);
            JObject::null().into()
         })
   }

   fn get_authorize_url<'local>(
      env: &mut JNIEnv<'local>,
      instance_base_url: JString<'local>,
      client_id: JString<'local>
   ) -> Result<JString<'local>> {
      let instance_base_url: String = env.get_string(&instance_base_url)?.into();
      let instance_base_url: Url = instance_base_url.parse()?;

      let client_id: String = env.get_string(&client_id)?.into();

      let authorize_url = oauth::get_authorize_url(
         &instance_base_url,
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
      instance_base_url: JString<'local>,
      code: JString<'local>,
      client_id: JString<'local>,
      client_secret: JString<'local>
   ) -> JObject<'local> {
      get_token(&mut env, instance_base_url, code, client_id, client_secret)
         .unwrap_or_else(|_| {
            throw_io_exception(&mut env);
            JObject::null()
         })
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_repository_AndroidAppRepository_getToken<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      instance_base_url: JString<'local>,
      code: JString<'local>,
      client_id: JString<'local>,
      client_secret: JString<'local>
   ) -> JObject<'local> {
      get_token(&mut env, instance_base_url, code, client_id, client_secret)
         .unwrap_or_else(|_| {
            throw_io_exception(&mut env);
            JObject::null()
         })
   }

   fn get_token<'local>(
      env: &mut JNIEnv<'local>,
      instance_base_url: JString<'local>,
      code: JString<'local>,
      client_id: JString<'local>,
      client_secret: JString<'local>
   ) -> Result<JObject<'local>> {
      let instance_base_url: String = env.get_string(&instance_base_url)?.into();
      let instance_base_url: Url = instance_base_url.parse()?;

      let code: String = env.get_string(&code)?.into();
      let client_id: String = env.get_string(&client_id)?.into();
      let client_secret: String = env.get_string(&client_secret)?.into();

      let ApiToken { access_token, token_type, scope, created_at } = oauth::post_token(
         &CLIENT, &instance_base_url,
         /* grant_type = */ "authorization_code",
         /* code = */ Some(&code),
         /* client_id = */ &client_id,
         /* client_secret = */ &client_secret,
         /* redirect_uri = */ "https://probosqis.wcaokaze.com/auth/callback",
         /* scope = */ Some("read write push")
      )?;

      let token = Token {
         instance_base_url, access_token, token_type, scope,
         created_at: DateTime::from_timestamp(created_at, 0).unwrap()
      };

      Ok(token.clone_into_java(env))
   }
}
