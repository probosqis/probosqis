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
use {
   jni::JNIEnv,
   jni::objects::JObject,
   serde::Deserialize,
   panoptiqon::convert_java::ConvertJava,
};

#[cfg(feature="jvm")]
#[derive(Debug, Eq, PartialEq, Clone, Deserialize)]
pub struct TestCache<T>(Box<T>) where T: ConvertJava;

#[cfg(feature="jvm")]
impl<T> TestCache<T> where T: ConvertJava {
   pub fn new(initial_value: T) -> TestCache<T> {
      TestCache(Box::new(initial_value))
   }
}

#[cfg(feature="jvm")]
impl<T> ConvertJava for TestCache<T>
   where T: ConvertJava
{
   fn clone_into_java<'local>(&self, env: &mut JNIEnv<'local>) -> JObject<'local> {
      let value = self.0.clone_into_java(env);

      env.new_object(
         "com/wcaokaze/probosqis/ext/panoptiqon/TestCache", "(Ljava/lang/Object;)V",
         &[(&value).into()]
      ).unwrap()
   }

   fn clone_from_java(env: &mut JNIEnv, java_object: &JObject) -> TestCache<T> {
      let value = env
         .call_method(&java_object, "getValue", "()Ljava/lang/Object;", &[])
         .unwrap().l().unwrap();

      let value = T::clone_from_java(env, &value);
      TestCache::new(value)
   }
}

#[cfg(feature="jni-test")]
mod jni_tests {
   use jni::JNIEnv;
   use jni::objects::JObject;

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_ext_panoptiqon_TestCacheTest_convertJava_1toRust_00024assert(
      mut env: JNIEnv,
      _obj: JObject,
      test_cache: JObject,
   ) {
      use panoptiqon::convert_java::ConvertJava;
      use crate::test_cache::TestCache;

      let test_cache = TestCache::clone_from_java(&mut env, &test_cache);
      assert_eq!(
         TestCache::new("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.".to_string()),
         test_cache
      );
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_ext_panoptiqon_TestCacheTest_convertJava_1fromRust_00024createTestCache<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JObject<'local> {
      use panoptiqon::convert_java::ConvertJava;
      use crate::test_cache::TestCache;

      TestCache::new("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.".to_string())
         .clone_into_java(&mut env)
   }
}
