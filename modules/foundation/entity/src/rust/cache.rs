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

use std::path::{Path, PathBuf};
use url::Url;
use panoptiqon::cache::CacheContent;
use crate::image_bytes::ImageBytes;

#[cfg(feature = "jvm")]
use {
   panoptiqon::jvm_types::JvmNullable,
   crate::jvm_types::JvmImage,
};

impl CacheContent for ImageBytes {
   type Key = Url;

   #[cfg(feature = "jvm")]
   type JvmType<'local> = JvmNullable<'local, JvmImage<'local>>;

   fn key(&self) -> Url {
      self.url.clone()
   }

   fn file_path(&self, dir_path: &Path) -> PathBuf {
      use percent_encoding::{utf8_percent_encode, NON_ALPHANUMERIC};

      let encoded_url: String 
         = utf8_percent_encode(self.url.as_str(), NON_ALPHANUMERIC).collect();

      dir_path.join(&encoded_url)
   }
}

#[cfg(test)]
mod test {
   use std::path::PathBuf;

   #[allow(non_snake_case)]
   #[test]
   fn ImageBytes_filePath() {
      use bytes::Bytes;
      use panoptiqon::cache::CacheContent;
      use crate::image_bytes::{ImageBytes, SerializableBytes};

      let image_bytes = ImageBytes {
         url: "https://example.com/image".parse().unwrap(),
         image_bytes: SerializableBytes(Bytes::new())
      };

      let dir = PathBuf::from("test/ImageBytes");

      assert_eq!(
         PathBuf::from("test/ImageBytes/https%3A%2F%2Fexample%2Ecom%2Fimage"),
         image_bytes.file_path(&dir)
      );
   }
}
