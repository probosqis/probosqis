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

import com.wcaokaze.probosqis.ext.kotlin.Url
import com.wcaokaze.probosqis.mastodon.entity.Account
import com.wcaokaze.probosqis.mastodon.entity.Instance
import com.wcaokaze.probosqis.mastodon.entity.Poll
import com.wcaokaze.probosqis.mastodon.entity.Status
import com.wcaokaze.probosqis.mastodon.entity.Token
import com.wcaokaze.probosqis.panoptiqon.Repository

class AndroidTimelineRepository(
   private val accountCacheRepository: Repository<Account.Id, Account>,
   private val statusCacheRepository: Repository<Status.Id, Status>,
   private val noCredentialStatusCacheRepository: Repository<Status.Id, Status.NoCredential>,
   private val noCredentialPollCacheRepository: Repository<Poll.Id, Poll.NoCredential>,
   private val instanceCacheRepository: Repository<Url, Instance>
) : TimelineRepository {
   override fun getHomeTimeline(token: Token): List<Status> {
      return getHomeTimeline(
         token, accountCacheRepository, statusCacheRepository,
         noCredentialStatusCacheRepository, noCredentialPollCacheRepository, instanceCacheRepository
      )
   }

   private external fun getHomeTimeline(
      token: Token,
      accountCacheRepo: Repository<Account.Id, Account>,
      statusCacheRepo: Repository<Status.Id, Status>,
      noCredentialStatusCacheRepo: Repository<Status.Id, Status.NoCredential>,
      noCredentialPollCacheRepo: Repository<Poll.Id, Poll.NoCredential>,
      instanceCacheRepo: Repository<Url, Instance>
   ): List<Status>
}
