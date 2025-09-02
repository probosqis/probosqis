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

package com.wcaokaze.probosqis.mastodon.repository

import com.wcaokaze.probosqis.entity.Image
import com.wcaokaze.probosqis.ext.kotlin.Url
import com.wcaokaze.probosqis.mastodon.entity.Account
import com.wcaokaze.probosqis.mastodon.entity.Application
import com.wcaokaze.probosqis.mastodon.entity.CredentialAccount
import com.wcaokaze.probosqis.mastodon.entity.Instance
import com.wcaokaze.probosqis.mastodon.entity.Poll
import com.wcaokaze.probosqis.mastodon.entity.Status
import com.wcaokaze.probosqis.panoptiqon.Repository

class CacheRepositories(
   val instance: Repository<Url, Instance>,
   val application: Repository<Application.Id, Application>,
   val account: Repository<Account.Id, Account>,
   val credentialAccount: Repository<Account.Id, CredentialAccount>,
   val accountIcon: Repository<Url, Image>,
   val status: Repository<Status.Id, Status>,
   val noCredentialStatus: Repository<Status.Id, Status.NoCredential>,
   val noCredentialPoll: Repository<Poll.Id, Poll.NoCredential>,
)

external fun createCacheRepositories(dataDirPath: String): CacheRepositories
