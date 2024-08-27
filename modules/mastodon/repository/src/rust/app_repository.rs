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
   use jni::JNIEnv;
   use jni::objects::{JObject, JString};
   use url::Url;

   use mastodon_webapi::apps;
   use panoptiqon::convert_java::ConvertJava;

   use crate::CLIENT;

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_repository_DesktopAppRepository_postApp<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      instance_base_url: JString<'local>
   ) -> JObject<'local> {
      post_app(&mut env, instance_base_url)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_repository_AndroidAppRepository_postApp<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      instance_base_url: JString<'local>
   ) -> JObject<'local> {
      post_app(&mut env, instance_base_url)
   }

   #[cfg(feature="jvm")]
   fn post_app<'local>(
      env: &mut JNIEnv<'local>,
      instance_base_url: JString<'local>
   ) -> JObject<'local> {
      let instance_base_url: String
         = env.get_string(&instance_base_url).unwrap().into();

      let instance_base_url: Url = instance_base_url.parse().unwrap();

      let application = apps::post_apps(
         &CLIENT, &instance_base_url,
         /* client_name = */ "Probosqis",
         /* redirect_uris = */ "https://3iqura.wcaokaze.com/auth/callback",
         /* scopes = */ Some("read write push"),
         /* website = */ None
      ).unwrap();

      application.clone_into_java(env)
   }
}
