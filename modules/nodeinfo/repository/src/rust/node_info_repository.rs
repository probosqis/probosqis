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

#[cfg(feature="jvm")]
mod jvm {
   use jni::JNIEnv;
   use jni::objects::JObject;
   use nodeinfo_entity::jvm_types::JvmFediverseSoftware;
   use nodeinfo_webapi::api::node_info;
   use nodeinfo_webapi::entity::resource_descriptor::ResourceDescriptor;
   use panoptiqon::jvm_types::JvmString;

   const REL_MAPPING: [(&str, &str); 4] = [
      ("http://nodeinfo.diaspora.software/ns/schema/1.0", "1.0"),
      ("http://nodeinfo.diaspora.software/ns/schema/1.1", "1.1"),
      ("http://nodeinfo.diaspora.software/ns/schema/2.0", "2.0"),
      ("http://nodeinfo.diaspora.software/ns/schema/2.1", "2.1"),
   ];

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_nodeinfo_repository_DesktopNodeInfoRepository_getServerSoftware<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      server_url: JvmString<'local>
   ) -> JvmFediverseSoftware<'local> {
      use ext_panoptiqon::unwrap_or_throw::UnwrapOrThrow;

      get_node_info(&mut env, server_url)
         .unwrap_or_throw_io_exception(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_nodeinfo_repository_AndroidNodeInfoRepository_getServerSoftware<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      server_url: JvmString<'local>
   ) -> JvmFediverseSoftware<'local> {
      use ext_panoptiqon::unwrap_or_throw::UnwrapOrThrow;

      get_node_info(&mut env, server_url)
         .unwrap_or_throw_io_exception(&mut env)
   }

   fn get_node_info<'local>(
      env: &mut JNIEnv<'local>,
      server_url: JvmString<'local>
   ) -> anyhow::Result<JvmFediverseSoftware<'local>> {
      use chrono::Utc;
      use ext_reqwest::CLIENT;
      use anyhow::anyhow;
      use mastodon_entity::instance::Instance;
      use nodeinfo_entity::fediverse_software::FediverseSoftware;
      use nodeinfo_webapi::entity::node_info::{NodeInfo, Software};
      use panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm};
      use url::Url;

      let server_url = String::clone_from_jvm(env, &server_url);
      let server_url: Url = server_url.parse()?;

      let resource_descriptor
         = node_info::get_node_info_resource_descriptor(&CLIENT, &server_url)?;

      let (version, node_info_url) = get_node_info_url(&resource_descriptor)
         .ok_or(anyhow!("cannot detect NodeInfo URL"))?;

      let NodeInfo {
         software: Software { mut name, version }
      } = node_info::get_node_info(&CLIENT, node_info_url, version)?;

      name.make_ascii_lowercase();

      let fediverse_software = if name == "mastodon" {
         FediverseSoftware::Mastodon {
            instance: Instance {
               url: server_url,
               version,
               version_checked_time: Utc::now(),
            },
         }
      } else {
         FediverseSoftware::Unsupported {
            name,
            version,
         }
      };

      let jvm_instance = fediverse_software.clone_into_jvm(env);
      Ok(jvm_instance)
   }

   fn get_node_info_url(
      resource_descriptor: &ResourceDescriptor
   ) -> Option<(&str, &str)> {
      REL_MAPPING.iter().rev()
         .flat_map(|&(rel, ver)|
            resource_descriptor.links.iter()
               .find(|link| link.rel == rel)
               .map(|link| (ver, link.href.as_str()))
         )
         .next()
   }

   #[cfg(test)]
   mod test {
      use nodeinfo_webapi::entity::resource_descriptor::{Link, ResourceDescriptor};

      #[test]
      fn get_node_info_url_empty_resource_descriptor() {
         let resource_descriptor = ResourceDescriptor {
            links: vec![]
         };

         assert_eq!(
            None,
            super::get_node_info_url(&resource_descriptor)
         );
      }

      #[test]
      fn get_node_info_url_not_found() {
         let resource_descriptor = ResourceDescriptor {
            links: vec![
               Link {
                  rel: "http://nodeinfo.diaspora.software/ns/schema/1.2".to_string(),
                  href: "https://example.com/".to_string()
               }
            ]
         };

         assert_eq!(
            None,
            super::get_node_info_url(&resource_descriptor)
         );
      }

      #[test]
      fn get_node_info_url() {
         let resource_descriptor = ResourceDescriptor {
            links: vec![
               Link {
                  rel: "http://nodeinfo.diaspora.software/ns/schema/1.0".to_string(),
                  href: "https://example.com/".to_string()
               }
            ]
         };

         assert_eq!(
            Some(("https://example.com/", "1.0")),
            super::get_node_info_url(&resource_descriptor)
         );
      }

      #[test]
      fn get_node_info_url_get_later_version() {
         let resource_descriptor = ResourceDescriptor {
            links: vec![
               Link {
                  rel: "http://nodeinfo.diaspora.software/ns/schema/1.0".to_string(),
                  href: "https://example.com/1.0".to_string()
               },
               Link {
                  rel: "http://nodeinfo.diaspora.software/ns/schema/1.1".to_string(),
                  href: "https://example.com/1.1".to_string()
               }
            ]
         };

         assert_eq!(
            Some(("https://example.com/1.1", "1.1")),
            super::get_node_info_url(&resource_descriptor)
         );

         let resource_descriptor = ResourceDescriptor {
            links: vec![
               Link {
                  rel: "http://nodeinfo.diaspora.software/ns/schema/2.0".to_string(),
                  href: "https://example.com/2.0".to_string()
               },
               Link {
                  rel: "http://nodeinfo.diaspora.software/ns/schema/1.1".to_string(),
                  href: "https://example.com/1.1".to_string()
               }
            ]
         };

         assert_eq!(
            Some(("https://example.com/2.0", "2.0")),
            super::get_node_info_url(&resource_descriptor)
         );
      }

      #[test]
      fn get_node_info_url_get_available_one() {
         let resource_descriptor = ResourceDescriptor {
            links: vec![
               Link {
                  rel: "http://nodeinfo.diaspora.software/ns/schema/3.0".to_string(),
                  href: "https://example.com/3.0".to_string()
               },
               Link {
                  rel: "http://nodeinfo.diaspora.software/ns/schema/1.1".to_string(),
                  href: "https://example.com/1.1".to_string()
               }
            ]
         };

         assert_eq!(
            Some(("https://example.com/1.1", "1.1")),
            super::get_node_info_url(&resource_descriptor)
         );
      }
   }
}
