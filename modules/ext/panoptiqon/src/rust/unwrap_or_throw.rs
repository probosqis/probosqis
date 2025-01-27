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

#![cfg(feature="jvm")]

use jni::JNIEnv;
use jni::objects::{JObject, JThrowable};
use panoptiqon::jvm_type::JvmType;
use panoptiqon::jvm_types::JvmException;

pub trait UnwrapOrThrow {
   type Err;
   type Output;

   fn unwrap_or_throw_exception<'local>(
      self,
      env: &mut JNIEnv<'local>,
      exception: impl Fn(&mut JNIEnv<'local>, &Self::Err) -> JvmException<'local>
   ) -> Self::Output;

   fn unwrap_or_throw_io_exception(self, env: &mut JNIEnv) -> Self::Output
      where Self: Sized,
            Self::Err: ToString
   {
      use panoptiqon::convert_jvm::CloneIntoJvm;

      self.unwrap_or_throw_exception(env, |env, err| {
         let message = err.to_string().clone_into_jvm(env);
         let exception = JThrowable::from(
            env.new_object(
               "java/io/IOException", "(Ljava/lang/String;)V",
               &[message.j_string().into()]
            ).unwrap()
         );

         JvmException::from_j_throwable(exception)
      })
   }
}

impl<'result_local, J, E> UnwrapOrThrow for Result<J, E>
   where J: JvmType<'result_local> + 'result_local,
         E: ToString
{
   type Err = E;
   type Output = J;

   fn unwrap_or_throw_exception<'local>(
      self,
      env: &mut JNIEnv<'local>,
      exception: impl Fn(&mut JNIEnv<'local>, &E) -> JvmException<'local>
   ) -> J {
      self.unwrap_or_else(|e| {
         let exception = exception(env, &e);
         env.throw(exception.j_throwable()).unwrap();
         unsafe { J::from_j_object(JObject::null()) }
      })
   }
}
