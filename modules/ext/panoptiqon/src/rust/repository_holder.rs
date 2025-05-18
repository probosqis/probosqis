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
use std::ops::{Deref, DerefMut};
use std::sync::{PoisonError, RwLock, RwLockReadGuard, RwLockWriteGuard};

#[cfg(feature="jvm")]
use {
   jni::JNIEnv,
   panoptiqon::convert_jvm::{CloneIntoJvm, CloneIntoJvmHelper},
};
use panoptiqon::cache::CacheContent;
use panoptiqon::repository::Repository;

pub struct RepositoryHolder<T: CacheContent> {
   lock: RwLock<LazyInitRepository<T>>
}

enum LazyInitRepository<T: CacheContent> {
   Repository(Repository<T>),
   None
}

impl<T: CacheContent> LazyInitRepository<T> {
   fn is_initialized(&self) -> bool {
      matches!(self, LazyInitRepository::Repository(_))
   }

   #[cfg(not(feature="jvm"))]
   fn initialize(&mut self) {
      if self.is_initialized() { return; }

      let repository = Repository::new();
      *self = LazyInitRepository::Repository(repository);
   }

   #[cfg(feature="jvm")]
   fn initialize<'local>(
      &mut self,
      env: &mut JNIEnv<'local>,
   )
      where T: CloneIntoJvm<'local, T::JvmType<'local>> + CloneIntoJvmHelper
   {
      if self.is_initialized() { return; }

      let repository = Repository::new(env);
      *self = LazyInitRepository::Repository(repository);
   }
}

pub struct RepositoryReadGuard<'a, T: CacheContent> {
   lock_guard: RwLockReadGuard<'a, LazyInitRepository<T>>
}

pub struct RepositoryWriteGuard<'a, T: CacheContent> {
   lock_guard: RwLockWriteGuard<'a, LazyInitRepository<T>>
}

impl<'a, T: CacheContent> Deref for RepositoryReadGuard<'a, T> {
   type Target = Repository<T>;
   
   fn deref(&self) -> &Repository<T> {
      if let LazyInitRepository::Repository(ref repo) = *self.lock_guard {
         return repo;
      } else {
         panic!();
      }
   }
}

impl<'a, T: CacheContent> Deref for RepositoryWriteGuard<'a, T> {
   type Target = Repository<T>;

   fn deref(&self) -> &Repository<T> {
      if let LazyInitRepository::Repository(ref repo) = *self.lock_guard {
         return repo;
      } else {
         panic!();
      }
   }
}

impl<'a, T: CacheContent> DerefMut for RepositoryWriteGuard<'a, T> {
   fn deref_mut(&mut self) -> &mut Repository<T> {
      if let LazyInitRepository::Repository(ref mut repo) = *self.lock_guard {
         return repo;
      } else {
         panic!();
      }
   }
}

impl<T: CacheContent> RepositoryHolder<T> {
   pub const fn new() -> Self {
      RepositoryHolder {
         lock: RwLock::new(LazyInitRepository::None)
      }
   }

   #[cfg(not(feature="jvm"))]
   pub fn read(&self) -> Result<RepositoryReadGuard<T>, PoisonError<()>> {
      let lock = self.lock.read().map_err(|_| PoisonError::new(()))?;

      if lock.is_initialized() {
         let repository_guard = RepositoryReadGuard {
            lock_guard: lock
         };
         return Ok(repository_guard);
      }

      drop(lock);

      let mut lock = self.lock.write().map_err(|_| PoisonError::new(()))?;
      lock.initialize();
      drop(lock);

      self.read()
   }

   #[cfg(feature="jvm")]
   pub fn read<'local>(
      &self,
      env: &mut JNIEnv<'local>
   ) -> Result<RepositoryReadGuard<T>, PoisonError<()>>
      where T: CloneIntoJvm<'local, T::JvmType<'local>> + CloneIntoJvmHelper
   {
      let lock = self.lock.read().map_err(|_| PoisonError::new(()))?;

      if lock.is_initialized() {
         let repository_guard = RepositoryReadGuard {
            lock_guard: lock
         };
         return Ok(repository_guard);
      }

      drop(lock);

      let mut lock = self.lock.write().map_err(|_| PoisonError::new(()))?;
      lock.initialize(env);
      drop(lock);

      self.read(env)
   }

   #[cfg(not(feature="jvm"))]
   pub fn write(&self) -> Result<RepositoryWriteGuard<T>, PoisonError<()>> {
      let mut lock = self.lock.write().map_err(|_| PoisonError::new(()))?;

      if !lock.is_initialized() {
         lock.initialize();
      }

      let repository_guard = RepositoryWriteGuard {
         lock_guard: lock
      };

      Ok(repository_guard)
   }

   #[cfg(feature="jvm")]
   pub fn write<'local>(
      &self,
      env: &mut JNIEnv<'local>
   ) -> Result<RepositoryWriteGuard<T>, PoisonError<()>>
      where T: CloneIntoJvm<'local, T::JvmType<'local>> + CloneIntoJvmHelper
   {
      let mut lock = self.lock.write().map_err(|_| PoisonError::new(()))?;

      if !lock.is_initialized() {
         lock.initialize(env);
      }

      let repository_guard = RepositoryWriteGuard {
         lock_guard: lock
      };

      Ok(repository_guard)
   }
}

#[cfg(feature="jni-test")]
mod jni_tests {
   use std::sync::Mutex;
   use jni::JNIEnv;
   use jni::objects::JObject;
   use panoptiqon::cache::CacheContent;
   use panoptiqon::convert_jvm::CloneIntoJvm;
   use panoptiqon::jvm_type;
   use super::{LazyInitRepository, RepositoryHolder};

   jvm_type! {
      JvmContent,
   }

   struct Content;

   impl CacheContent for Content {
      type Key = ();
      type JvmType<'local> = JvmContent<'local>;

      fn key(&self) {}
   }

   impl<'local> CloneIntoJvm<'local, JvmContent<'local>> for Content {
      fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmContent<'local> {
         use panoptiqon::jvm_type::JvmType;

         let dummy_j_object = ().clone_into_jvm(env);
         JvmContent(dummy_j_object.into_j_object())
      }
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_ext_panoptiqon_RepositoryHolderTest_initializeRepositoryByReading(
      mut env: JNIEnv,
      _obj: JObject
   ) {
      let holder = RepositoryHolder::<Content>::new();
      {
         assert!(matches!(*holder.lock.read().unwrap(), LazyInitRepository::None));
      }

      {
         let _lock = holder.read(&mut env).unwrap();
      }
      {
         assert!(matches!(*holder.lock.read().unwrap(), LazyInitRepository::Repository(_)));
      }
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_ext_panoptiqon_RepositoryHolderTest_initializeRepositoryByWriting(
      mut env: JNIEnv,
      _obj: JObject
   ) {
      let holder = RepositoryHolder::<Content>::new();
      {
         assert!(matches!(*holder.lock.read().unwrap(), LazyInitRepository::None));
      }

      {
         let _lock = holder.write(&mut env).unwrap();
      }
      {
         assert!(matches!(*holder.lock.read().unwrap(), LazyInitRepository::Repository(_)));
      }
   }

   #[allow(non_upper_case_globals)]
   static readBlocks_repository_holder: RepositoryHolder<Content> = RepositoryHolder::new();
   struct ReadBlocksState {
      thread1_read_repository: bool,
      thread1_write_repository: bool,
      thread2_read_repository: bool
   }
   #[allow(non_upper_case_globals)]
   static read_blocks_state: Mutex<ReadBlocksState> = Mutex::new(
      ReadBlocksState {
         thread1_read_repository: false,
         thread1_write_repository: false,
         thread2_read_repository: false
      }
   );

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_ext_panoptiqon_RepositoryHolderTest_readWriteLock_00024thread1(
      mut env: JNIEnv,
      _obj: JObject
   ) {
      use std::thread;
      use std::time::Duration;

      // readロックを取る
      let repository_lock = readBlocks_repository_holder.read(&mut env).unwrap();
      {
         read_blocks_state.lock().unwrap().thread1_read_repository = true;
      }

      // thread2がreadロックを取るのを待機
      loop {
         thread::sleep(Duration::from_millis(1));
         if read_blocks_state.lock().unwrap().thread2_read_repository { break; }
      }

      // readロックを解放
      drop(repository_lock);
      {
         read_blocks_state.lock().unwrap().thread1_read_repository = false;
      }

      // writeロックを取る。thread2がreadロックを解放するまでブロックする
      let repository_lock = readBlocks_repository_holder.write(&mut env).unwrap();
      {
         read_blocks_state.lock().unwrap().thread1_write_repository = true;
      }

      // この直後thread2はreadロックを取ろうとするが、取れていないことを確認
      for _ in 0..10 {
         thread::sleep(Duration::from_millis(1));
         assert_eq!(false, read_blocks_state.lock().unwrap().thread2_read_repository);
      }

      // writeロックを解放
      drop(repository_lock);
      {
         read_blocks_state.lock().unwrap().thread1_write_repository = false;
      }

      // thread2がreadロックを取れたことを確認
      loop {
         thread::sleep(Duration::from_millis(1));
         if read_blocks_state.lock().unwrap().thread2_read_repository { break; }
      }
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_ext_panoptiqon_RepositoryHolderTest_readWriteLock_00024thread2(
      mut env: JNIEnv,
      _obj: JObject
   ) {
      use std::ops::Not;
      use std::thread;
      use std::time::Duration;

      // 一旦thread1がreadロックを取るのを待機
      loop {
         thread::sleep(Duration::from_millis(1));
         if read_blocks_state.lock().unwrap().thread1_read_repository { break; }
      }

      // readロックを取る
      let repository_lock = readBlocks_repository_holder.read(&mut env).unwrap();
      {
         read_blocks_state.lock().unwrap().thread2_read_repository = true;
      }

      // thread1がreadロックを解放するのを待機
      loop {
         thread::sleep(Duration::from_millis(1));
         if read_blocks_state.lock().unwrap().thread1_read_repository.not() { break; }
      }

      // この直後thread1はwriteロックを取ろうとするが、取れていないことを確認
      for _ in 0..10 {
         thread::sleep(Duration::from_millis(1));
         assert_eq!(false, read_blocks_state.lock().unwrap().thread1_write_repository);
      }

      // readロックを解放
      drop(repository_lock);
      {
         read_blocks_state.lock().unwrap().thread2_read_repository = false;
      }

      // thread1がwriteロックを取るのを待機
      loop {
         thread::sleep(Duration::from_millis(1));
         if read_blocks_state.lock().unwrap().thread1_write_repository { break; }
      }

      // 再度readロックを取ろうとする。thread1がwriteロックを解放するまでブロックする
      let _repository_lock = readBlocks_repository_holder.read(&mut env).unwrap();
      {
         read_blocks_state.lock().unwrap().thread2_read_repository = true;
      }
   }
}
