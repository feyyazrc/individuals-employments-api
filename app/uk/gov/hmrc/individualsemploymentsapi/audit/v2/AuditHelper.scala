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

package uk.gov.hmrc.individualsemploymentsapi.audit.v2

import javax.inject.Inject
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.individualsemploymentsapi.audit.v2.events.{ApiFailureEvent, ApiResponseEvent, IfApiFailureEvent, IfApiResponseEvent}
import uk.gov.hmrc.individualsemploymentsapi.audit.v2.models.{ApiAuditRequest, ApiFailureAuditRequest, ApiIfAuditRequest, ApiIfFailureAuditRequest}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.ExecutionContext

case class AuditHelper @Inject()(auditConnector: AuditConnector, httpExtendedAuditEvent: HttpExtendedAuditEvent)(
  implicit ec: ExecutionContext) {

  def auditApiResponse(apiAuditRequest: ApiAuditRequest)(implicit hc: HeaderCarrier) =
    auditConnector.sendExtendedEvent(
      new ApiResponseEvent(
        httpExtendedAuditEvent
      ).apply(
        apiAuditRequest.correlationId,
        apiAuditRequest.scopes,
        apiAuditRequest.matchId,
        apiAuditRequest.request,
        None,
        apiAuditRequest.response.toString
      )
    )

  def auditApiFailure(apiFailedAuditRequest: ApiFailureAuditRequest, msg: String)(implicit hc: HeaderCarrier) =
    auditConnector.sendExtendedEvent(
      new ApiFailureEvent(
        httpExtendedAuditEvent
      ).apply(
        apiFailedAuditRequest.correlationId,
        apiFailedAuditRequest.scopes,
        apiFailedAuditRequest.matchId,
        apiFailedAuditRequest.request,
        Some(apiFailedAuditRequest.requestUrl),
        msg
      )
    )

  def auditIfApiResponse(apiIfAuditRequest: ApiIfAuditRequest)(implicit hc: HeaderCarrier) =
    auditConnector.sendExtendedEvent(
      new IfApiResponseEvent(
        httpExtendedAuditEvent
      ).apply(
        apiIfAuditRequest.correlationId,
        apiIfAuditRequest.scopes,
        apiIfAuditRequest.matchId,
        apiIfAuditRequest.request,
        Some(apiIfAuditRequest.requestUrl),
        apiIfAuditRequest.response.toString
      )
    )

  def auditIfApiFailure(apiIfFailedAuditRequest: ApiIfFailureAuditRequest, msg: String)(implicit hc: HeaderCarrier) =
    auditConnector.sendExtendedEvent(
      new IfApiFailureEvent(
        httpExtendedAuditEvent
      ).apply(
        apiIfFailedAuditRequest.correlationId,
        apiIfFailedAuditRequest.scopes,
        apiIfFailedAuditRequest.matchId,
        apiIfFailedAuditRequest.request,
        Some(apiIfFailedAuditRequest.requestUrl),
        msg
      )
    )
}
