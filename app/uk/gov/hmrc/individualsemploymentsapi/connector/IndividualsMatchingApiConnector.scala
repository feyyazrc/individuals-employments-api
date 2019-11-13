/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.individualsemploymentsapi.connector

import java.util.UUID

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.individualsemploymentsapi.config.ConfigSupport
import uk.gov.hmrc.individualsemploymentsapi.controller.CustomExceptions.MatchNotFoundException
import uk.gov.hmrc.individualsemploymentsapi.domain.NinoMatch
import uk.gov.hmrc.individualsemploymentsapi.util.JsonFormatters.ninoMatchJsonFormat
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.config.ServicesConfig

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class IndividualsMatchingApiConnector @Inject()(override val config: Configuration, http: HttpClient) extends ServicesConfig with ConfigSupport{

  private[connector] val serviceUrl = baseUrl("individuals-matching-api")

  def resolve(matchId: UUID)(implicit hc: HeaderCarrier): Future[NinoMatch] =
    http.GET[NinoMatch](s"$serviceUrl/match-record/$matchId") recover {
      case _: NotFoundException => throw MatchNotFoundException
    }

}
