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
#![cfg(feature="jvm")]

use jni::descriptors::Desc;
use jni::JNIEnv;
use jni::objects::{JClass, JObject, JString, JThrowable};

pub trait UnwrapOrThrow {
   type Output;

   fn unwrap_or_throw_io_exception<'local>(
      self,
      env: &mut JNIEnv<'local>
   ) -> Self::Output;
}

fn throw_exception<'local, 'other_local>(
   env: &mut JNIEnv<'local>,
   class: impl Desc<'local, JClass<'other_local>>
) {
   let exception = JThrowable::from(
      env.new_object(class, "()V", &[]).unwrap()
   );
   env.throw(exception).unwrap();
}

impl<'result_local, E> UnwrapOrThrow for Result<JObject<'result_local>, E>
   where E: ToString
{
   type Output = JObject<'result_local>;

   fn unwrap_or_throw_io_exception<'local>(
      self,
      env: &mut JNIEnv<'local>
   ) -> JObject<'result_local> {
      self.unwrap_or_else(|_| {
         throw_exception(env, "java/io/IOException");
         JObject::null()
      })
   }
}

impl<'result_local, E> UnwrapOrThrow for Result<JString<'result_local>, E>
   where E: ToString
{
   type Output = JString<'result_local>;

   fn unwrap_or_throw_io_exception<'local>(
      self,
      env: &mut JNIEnv<'local>
   ) -> JString<'result_local> {
      self.unwrap_or_else(|_| {
         throw_exception(env, "java/io/IOException");
         JObject::null().into()
      })
   }
}
