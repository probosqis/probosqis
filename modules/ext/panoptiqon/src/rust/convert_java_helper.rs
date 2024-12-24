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

use std::ops::Deref;
use std::sync::RwLock;

use jni::JNIEnv;
use jni::objects::{GlobalRef, JMethodID, JObject, JStaticMethodID, JValueOwned};
use jni::signature::{ReturnType, TypeSignature};
use jni::sys::jvalue;

pub struct ConvertJavaHelper<'a, const ARITY: usize>(
   RwLock<ConvertJavaHelperInner<'a, ARITY>>
);

pub enum CloneIntoJava<'a> {
   ViaConstructor(&'a str),
   ViaStaticMethod(&'a str, &'a str)
}

enum ConvertJavaHelperInner<'a, const ARITY: usize> {
   SignatureStrs {
      class_fully_qualified_name: &'a str,
      clone_into_java: CloneIntoJava<'a>,
      getter_signatures: [(&'a str, &'a str); ARITY]
   },
   JvmIds {
      class: GlobalRef,
      clone_into_java: CloneIntoJavaJvmId,
      getter_ids: [(JMethodID, ReturnType); ARITY]
   }
}

enum CloneIntoJavaJvmId {
   ViaConstructor {
      constructor_id: JMethodID
   },
   ViaStaticMethod {
      method_id: JStaticMethodID,
      return_type: ReturnType
   }
}

impl<'a, const ARITY: usize> ConvertJavaHelper<'a, ARITY> {
   pub const fn new(
      class_fully_qualified_name: &'a str,
      clone_into_java: CloneIntoJava<'a>,
      getter_signatures: [(&'a str, &'a str); ARITY]
   ) -> Self {
      let inner = ConvertJavaHelperInner::SignatureStrs {
         class_fully_qualified_name,
         clone_into_java,
         getter_signatures
      };
      ConvertJavaHelper(RwLock::new(inner))
   }

   fn prepare_ids(
      &self,
      env: &mut JNIEnv,
      class_fully_qualified_name: &str,
      clone_into_java: &CloneIntoJava,
      getter_signatures: &[(&str, &str); ARITY]
   ) -> (GlobalRef, CloneIntoJavaJvmId, [(JMethodID, ReturnType); ARITY]) {
      let class = env.find_class(class_fully_qualified_name).unwrap();
      let class = env.new_global_ref(class).unwrap();

      let clone_into_java_id = match clone_into_java {
         CloneIntoJava::ViaConstructor(constructor_signature) => {
            let constructor_id = env
               .get_method_id(&class, "<init>", constructor_signature).unwrap();

            CloneIntoJavaJvmId::ViaConstructor { constructor_id }
         },
         CloneIntoJava::ViaStaticMethod(method_name, signature) => {
            let method_id = env
               .get_static_method_id(&class, method_name, signature).unwrap();

            let type_signature = TypeSignature::from_str(signature).unwrap();

            CloneIntoJavaJvmId::ViaStaticMethod {
               method_id,
               return_type: type_signature.ret
            }
         }
      };

      let getter_ids = getter_signatures.map(|(name, ty)| {
         let type_signature_str = format!("(){ty}");
         let type_signature = TypeSignature::from_str(&type_signature_str).unwrap();
         let getter_id = env.get_method_id(&class, name, &type_signature_str).unwrap();
         (getter_id, type_signature.ret)
      });

      (class, clone_into_java_id, getter_ids)
   }

   pub fn clone_into_java<'local>(
      &self,
      env: &mut JNIEnv<'local>,
      args: &[jvalue],
   ) -> JObject<'local> {
      let lock = self.0.read().unwrap();
      match lock.deref() {
         ConvertJavaHelperInner::JvmIds {
            class,
            clone_into_java,
            ..
         } => {
            match clone_into_java {
               CloneIntoJavaJvmId::ViaConstructor { constructor_id } => unsafe {
                  env.new_object_unchecked(class, *constructor_id, args).unwrap()
               },
               CloneIntoJavaJvmId::ViaStaticMethod { method_id, return_type } => unsafe {
                  env.call_static_method_unchecked(
                        class, *method_id, return_type.clone(), args
                     )
                     .unwrap().l().unwrap()
               }
            }
         }

         ConvertJavaHelperInner::SignatureStrs {
            class_fully_qualified_name,
            clone_into_java,
            getter_signatures
         } => {
            let (class, clone_into_java_id, getter_ids) = self.prepare_ids(
               env, class_fully_qualified_name, clone_into_java,
               getter_signatures
            );
            drop(lock);
            let mut lock = self.0.write().unwrap();
            *lock = ConvertJavaHelperInner::JvmIds {
               class, clone_into_java: clone_into_java_id, getter_ids
            };
            drop(lock);
            self.clone_into_java(env, args)
         }
      }
   }

   pub fn get<'local>(
      &self,
      env: &mut JNIEnv<'local>,
      instance: &JObject,
      n: usize
   ) -> JValueOwned<'local> {
      let lock = self.0.read().unwrap();
      match lock.deref() {
         ConvertJavaHelperInner::JvmIds {
            getter_ids,
            ..
         } => {
            let (getter_id, return_type) = &getter_ids[n];
            unsafe {
               env.call_method_unchecked(instance, getter_id, return_type.clone(), &[])
                  .unwrap()
            }
         }

         ConvertJavaHelperInner::SignatureStrs {
            class_fully_qualified_name,
            clone_into_java,
            getter_signatures
         } => {
            let (class, clone_into_java_id, getter_ids) = self.prepare_ids(
               env, class_fully_qualified_name, clone_into_java,
               getter_signatures
            );
            drop(lock);
            let mut lock = self.0.write().unwrap();
            *lock = ConvertJavaHelperInner::JvmIds {
               class, clone_into_java: clone_into_java_id, getter_ids
            };
            drop(lock);
            self.get(env, instance, n)
         }
      }
   }
}

#[cfg(feature="jni-test")]
mod jni_tests {
   use std::ops::Deref;

   use jni::JNIEnv;
   use jni::objects::{JIntArray, JObject};
   use jni::sys::{JNI_FALSE, jvalue};

   use panoptiqon::convert_java::ConvertJava;
   use super::{CloneIntoJava, ConvertJavaHelper, ConvertJavaHelperInner};

   fn create_helper() -> ConvertJavaHelper<'static, 10> {
      ConvertJavaHelper::new(
         "com/wcaokaze/probosqis/ext/panoptiqon/TestEntity",
         CloneIntoJava::ViaConstructor("(ZBSIJFDCLjava/lang/String;[I)V"),
         [
            ("getZ", "Z"),
            ("getB", "B"),
            ("getS", "S"),
            ("getI", "I"),
            ("getJ", "J"),
            ("getF", "F"),
            ("getD", "D"),
            ("getC", "C"),
            ("getStr", "Ljava/lang/String;"),
            ("getArr", "[I"),
         ]
      )
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_ext_panoptiqon_ConvertJavaHelperTest_variantStrsOnInit(
      _env: JNIEnv,
      _obj: JObject
   ) {
      let helper = create_helper();
      let helper_inner= helper.0.read().unwrap();
      assert!(matches!(helper_inner.deref(), &ConvertJavaHelperInner::SignatureStrs { .. }));
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_ext_panoptiqon_ConvertJavaHelperTest_variantJvmIdsAfterCloneIntoJava(
      mut env: JNIEnv,
      _obj: JObject
   ) {
      let helper = create_helper();

      let l = env.new_string("").unwrap();
      let a = env.new_int_array(0).unwrap();

      helper.clone_into_java(&mut env, &[
         jvalue { z: JNI_FALSE },
         jvalue { b: 0 },
         jvalue { s: 0 },
         jvalue { i: 0 },
         jvalue { j: 0 },
         jvalue { f: 0.0 },
         jvalue { d: 0.0 },
         jvalue { c: 'a' as u16  },
         jvalue { l: l.into_raw() },
         jvalue { l: a.into_raw() },
      ]);

      let helper_inner= helper.0.read().unwrap();
      assert!(matches!(helper_inner.deref(), &ConvertJavaHelperInner::JvmIds { .. }));
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_ext_panoptiqon_ConvertJavaHelperTest_variantJvmIdsAfterGet_00024assert(
      mut env: JNIEnv,
      _obj: JObject,
      jvm_entity: JObject
   ) {
      let helper = create_helper();

      helper.get(&mut env, &jvm_entity, 0);

      let helper_inner= helper.0.read().unwrap();
      assert!(matches!(helper_inner.deref(), &ConvertJavaHelperInner::JvmIds { .. }));
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_ext_panoptiqon_ConvertJavaHelperTest_cloneIntoJava_00024createEntity<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JObject<'local> {
      let helper = create_helper();

      let l = env.new_string("9012345").unwrap();
      let a = env.new_int_array(5).unwrap();
      env.set_int_array_region(&a, 0, &[6, 7, 8, 9, 0]).unwrap();

      helper.clone_into_java(&mut env, &[
         jvalue { z: JNI_FALSE },
         jvalue { b: 0 },
         jvalue { s: 1 },
         jvalue { i: 2 },
         jvalue { j: 3 },
         jvalue { f: 4.5 },
         jvalue { d: 6.75 },
         jvalue { c: '8' as u16 },
         jvalue { l: l.into_raw() },
         jvalue { l: a.into_raw() },
      ])
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_ext_panoptiqon_ConvertJavaHelperTest_cloneFromJava_00024assert(
      mut env: JNIEnv,
      _obj: JObject,
      jvm_entity: JObject
   ) {
      let helper = create_helper();

      let z = helper.get(&mut env, &jvm_entity, 0).z().unwrap();
      let b = helper.get(&mut env, &jvm_entity, 1).b().unwrap();
      let s = helper.get(&mut env, &jvm_entity, 2).s().unwrap();
      let i = helper.get(&mut env, &jvm_entity, 3).i().unwrap();
      let j = helper.get(&mut env, &jvm_entity, 4).j().unwrap();
      let f = helper.get(&mut env, &jvm_entity, 5).f().unwrap();
      let d = helper.get(&mut env, &jvm_entity, 6).d().unwrap();
      let c = helper.get(&mut env, &jvm_entity, 7).c().unwrap();

      let str = helper.get(&mut env, &jvm_entity, 8).l().unwrap();
      let str = String::clone_from_java(&mut env, &str);

      let arr = helper.get(&mut env, &jvm_entity, 9).l().unwrap();
      let mut buf = [0; 5];
      env.get_int_array_region(JIntArray::from(arr), 0, &mut buf).unwrap();

      assert_eq!(false, z);
      assert_eq!(0, b);
      assert_eq!(1, s);
      assert_eq!(2, i);
      assert_eq!(3, j);
      assert_eq!(4.5, f);
      assert_eq!(6.75, d);
      assert_eq!('8' as u16, c);
      assert_eq!("9012345".to_string(), str);
      assert_eq!([6, 7, 8, 9, 0], buf);
   }
}
