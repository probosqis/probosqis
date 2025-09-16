/*
 * Copyright 2025 wcaokaze
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

pub mod image_bytes;
mod cache;
mod url;

#[cfg(feature = "jvm")]
pub mod jvm_types;

#[cfg(feature = "jni-test")]
mod jni_tests {
   use jni::JNIEnv;
   use jni::objects::JObject;
   use crate::jvm_types::JvmImage;

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_entity_ImageConvertJniTest_imageBytes_1rust2Kt_00024createImageBytes<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmImage<'local> {
      use bytes::Bytes;
      use panoptiqon::convert_jvm::CloneIntoJvm;
      use url::Url;
      use crate::image_bytes::ImageBytes;

      let image_bytes = ImageBytes::new(
         Url::parse("https://github.com/wcaokaze.png").unwrap(),
         Bytes::copy_from_slice(&[0xca, 0xfe, 0xba, 0xbe])
      );

      image_bytes.clone_into_jvm(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_entity_ImageConvertJniTest_imageBytes_1kt2Rust_00024assert<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      image: JvmImage<'local>
   ) {
      use panoptiqon::convert_jvm::CloneFromJvm;
      use url::Url;
      use crate::image_bytes::ImageBytes;

      let image_bytes = ImageBytes::clone_from_jvm(&mut env, &image);

      assert_eq!(
         Url::parse("https://github.com/wcaokaze.png").unwrap(),
         image_bytes.url
      );

      assert_eq!(
         &[0xca, 0xfe, 0xba, 0xbe],
         image_bytes.image_bytes.0.as_ref()
      );
   }
}
