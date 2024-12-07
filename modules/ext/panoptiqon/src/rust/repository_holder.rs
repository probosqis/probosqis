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
use std::hash::Hash;
use std::mem;
use std::ops::{Deref, DerefMut};
use std::sync::{PoisonError, RwLock, RwLockReadGuard, RwLockWriteGuard};

#[cfg(feature="jvm")]
use {
   jni::JNIEnv,
   panoptiqon::convert_java::ConvertJava,
};
use panoptiqon::repository::Repository;

pub struct RepositoryHolder<K, T, S = fn (&T) -> K>
   where S: Fn(&T) -> K
{
   lock: RwLock<LazyInitRepository<K, T, S>>
}

enum LazyInitRepository<K, T, S>
   where S: Fn(&T) -> K
{
   Repository(Repository<K, T, S>),
   None(S),
   Initializing
}

impl<K, T, S> LazyInitRepository<K, T, S>
   where S: Fn(&T) -> K
{
   fn is_initialized(&self) -> bool {
      matches!(self, LazyInitRepository::Repository(_))
   }

   #[cfg(feature="jvm")]
   fn initialize(
      &mut self,
      env: &mut JNIEnv,
   )
      where K: Hash + Eq,
            T: ConvertJava
   {
      if self.is_initialized() { return; }

      let LazyInitRepository::None(key)
         = mem::replace(self, LazyInitRepository::Initializing)
         else { return; };

      let repository = Repository::new(env, key);
      *self = LazyInitRepository::Repository(repository);
   }
}

pub struct RepositoryReadGuard<'a, K, T, S>
   where S: Fn(&T) -> K
{
   lock_guard: RwLockReadGuard<'a, LazyInitRepository<K, T, S>>
}

pub struct RepositoryWriteGuard<'a, K, T, S>
   where S: Fn(&T) -> K
{
   lock_guard: RwLockWriteGuard<'a, LazyInitRepository<K, T, S>>
}

impl<'a, K, T, S> Deref for RepositoryReadGuard<'a, K, T, S>
   where S: Fn(&T) -> K
{
   type Target = Repository<K, T, S>;
   
   fn deref(&self) -> &Repository<K, T, S> {
      if let LazyInitRepository::Repository(ref repo) = *self.lock_guard {
         return repo;
      } else {
         panic!();
      }
   }
}

impl<'a, K, T, S> Deref for RepositoryWriteGuard<'a, K, T, S>
   where S: Fn(&T) -> K
{
   type Target = Repository<K, T, S>;

   fn deref(&self) -> &Repository<K, T, S> {
      if let LazyInitRepository::Repository(ref repo) = *self.lock_guard {
         return repo;
      } else {
         panic!();
      }
   }
}

impl<'a, K, T, S> DerefMut for RepositoryWriteGuard<'a, K, T, S>
   where S: Fn(&T) -> K
{
   fn deref_mut(&mut self) -> &mut Repository<K, T, S> {
      if let LazyInitRepository::Repository(ref mut repo) = *self.lock_guard {
         return repo;
      } else {
         panic!();
      }
   }
}

impl<K, T, S> RepositoryHolder<K, T, S>
   where K: Hash + Eq,
         S: Fn(&T) -> K
{
   pub const fn new(key: S) -> Self {
      RepositoryHolder {
         lock: RwLock::new(LazyInitRepository::None(key))
      }
   }

   #[cfg(feature="jvm")]
   pub fn read(
      &self,
      env: &mut JNIEnv
   ) -> Result<RepositoryReadGuard<K, T, S>, PoisonError<()>>
      where T: ConvertJava
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

   #[cfg(feature="jvm")]
   pub fn write(
      &self,
      env: &mut JNIEnv
   ) -> Result<RepositoryWriteGuard<K, T, S>, PoisonError<()>>
      where T: ConvertJava
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
   use std::ops::Not;
   use std::sync::Mutex;
   use std::thread;
   use std::time::Duration;

   use jni::JNIEnv;
   use jni::objects::JObject;

   use super::{LazyInitRepository, RepositoryHolder};

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_ext_panoptiqon_RepositoryHolderTest_initializeRepositoryByReading(
      mut env: JNIEnv,
      _obj: JObject
   ) {
      let holder = RepositoryHolder::<(), ()>::new(|_| ());
      {
         assert!(matches!(*holder.lock.read().unwrap(), LazyInitRepository::None(_)));
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
      let holder = RepositoryHolder::<(), ()>::new(|_| ());
      {
         assert!(matches!(*holder.lock.read().unwrap(), LazyInitRepository::None(_)));
      }

      {
         let _lock = holder.write(&mut env).unwrap();
      }
      {
         assert!(matches!(*holder.lock.read().unwrap(), LazyInitRepository::Repository(_)));
      }
   }

   #[allow(non_upper_case_globals)]
   static readBlocks_repository_holder: RepositoryHolder<(), ()> = RepositoryHolder::new(|_| ());
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
