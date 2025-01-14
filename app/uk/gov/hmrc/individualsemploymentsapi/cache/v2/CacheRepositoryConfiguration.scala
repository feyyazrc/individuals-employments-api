/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.individualsemploymentsapi.cache.v2

import javax.inject.Inject
import play.api.Configuration
import uk.gov.hmrc.individualsemploymentsapi.cache.{CacheRepositoryConfiguration => BaseConig}

class CacheRepositoryConfiguration @Inject()(configuration: Configuration) extends BaseConig {
  override val cacheEnabled: Boolean = configuration
    .getOptional[Boolean](
      "cacheV2.enabled"
    )
    .getOrElse(true)

  override val cacheTtl: Int = configuration
    .getOptional[Int](
      "cacheV2.ttlInSeconds"
    )
    .getOrElse(60 * 15)

  override val collName: String = configuration
    .getOptional[String](
      "cacheV2.collName"
    )
    .getOrElse("individuals-employments-v2-cache")
}
