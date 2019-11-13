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

package uk.gov.hmrc.individualsemploymentsapi.error

import javax.inject.Inject
import play.api.Configuration
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND}
import play.api.libs.json.Json
import play.api.mvc.Results.{BadRequest, NotFound, Status}
import play.api.mvc.{RequestHeader, Result}
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.http.{ErrorResponse, JsonErrorHandler}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CustomErrorHandler @Inject()(configuration: Configuration, auditConnector: AuditConnector)
  extends JsonErrorHandler(configuration, auditConnector) {

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {

    implicit val headerCarrier = HeaderCarrierConverter.fromHeadersAndSessionAndRequest(request.headers, request = Some(request))

    statusCode match {
      case NOT_FOUND =>
        auditConnector.sendEvent(dataEvent("NOT_FOUND", "The resource can not be found", request))
        Future.successful(
          NotFound(Json.toJson(ErrorResponse(NOT_FOUND, "The resource can not be found", requested = Some(request.path)))))
      case BAD_REQUEST =>
        auditConnector.sendEvent(dataEvent("ServerValidationError", "Request bad format exception", request))
        Future.successful(BadRequest(Json.toJson(ErrorResponse(BAD_REQUEST, message))))
      case _ =>
        auditConnector.sendEvent(dataEvent("ClientError", s"A client error occurred, status: $statusCode", request))
        Future.successful(Status(statusCode)(Json.toJson(ErrorResponse(statusCode, message))))
    }
  }

}
