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

package uk.gov.hmrc.individualsemploymentsapi.controller.v2

import org.joda.time.DateTime
import play.api.Logger
import play.api.mvc.{ControllerComponents, Request, RequestHeader, Result}
import uk.gov.hmrc.auth.core.{AuthorisationException, AuthorisedFunctions, Enrolment, InsufficientEnrolments}
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, InternalServerException, TooManyRequestException}
import uk.gov.hmrc.individualsemploymentsapi.audit.v2.AuditHelper
import uk.gov.hmrc.individualsemploymentsapi.error.ErrorResponses.{ErrorInternalServer, ErrorInvalidRequest, ErrorNotFound, ErrorTooManyRequests, ErrorUnauthorized, MatchNotFoundException, MissingQueryParameterException}
import uk.gov.hmrc.individualsemploymentsapi.util.Dates.toFormattedLocalDate
import uk.gov.hmrc.individualsemploymentsapi.util.UuidValidator
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.Future.successful
import scala.concurrent.{ExecutionContext, Future}

abstract class CommonController @Inject()(cc: ControllerComponents) extends BackendController(cc) {

  val logger: Logger = Logger(getClass)

  private def getQueryParam[T](name: String)(implicit request: Request[T]) =
    request.queryString.get(name).flatMap(_.headOption)

  private[controller] def urlWithInterval[T](url: String, fromDate: DateTime)(implicit request: Request[T]) = {
    val urlWithFromDate = s"$url&fromDate=${toFormattedLocalDate(fromDate)}"
    getQueryParam("toDate") map (toDate => s"$urlWithFromDate&toDate=$toDate") getOrElse urlWithFromDate
  }

  protected def withValidUuid(uuidString: String, uuidName: String)(f: UUID => Future[Result]): Future[Result] =
    if (UuidValidator.validate(uuidString)) {
      f(UUID.fromString(uuidString))
    } else {
      successful(ErrorInvalidRequest(s"$uuidName format is invalid").toHttpResponse)
    }

  private[controller] def withAudit(correlationId: Option[String], matchId: String, url: String)
                                   (implicit request: RequestHeader,
                                    auditHelper: AuditHelper): PartialFunction[Throwable, Result] = {
    case _: MatchNotFoundException   => {
      logger.warn("Controllers MatchNotFoundException encountered")
      auditHelper.auditApiFailure(correlationId, matchId, request, url, "Not Found")
      ErrorNotFound.toHttpResponse
    }
    case e: InsufficientEnrolments => {
      auditHelper.auditApiFailure(correlationId, matchId, request, url, e.getMessage)
      ErrorUnauthorized("Insufficient Enrolments").toHttpResponse
    }
    case e: AuthorisationException   => {
      auditHelper.auditApiFailure(correlationId, matchId, request, url, e.getMessage)
      ErrorUnauthorized(e.getMessage).toHttpResponse
    }
    case tmr: TooManyRequestException  => {
      logger.warn("Controllers TooManyRequestException encountered")
      auditHelper.auditApiFailure(correlationId, matchId, request, url, tmr.getMessage)
      ErrorTooManyRequests.toHttpResponse
    }
    case br: BadRequestException  => {
      auditHelper.auditApiFailure(correlationId, matchId, request, url, br.getMessage)
      ErrorInvalidRequest(br.getMessage).toHttpResponse
    }
    case e: IllegalArgumentException => {
      logger.warn("Controllers IllegalArgumentException encountered")
      auditHelper.auditApiFailure(correlationId, matchId, request, url, e.getMessage)
      ErrorInvalidRequest(e.getMessage).toHttpResponse
    }
    case e: InternalServerException => {
      logger.warn("Controllers InternalServerException encountered")
      auditHelper.auditApiFailure(correlationId, matchId, request, url, e.getMessage)
      ErrorInternalServer("Something went wrong.").toHttpResponse
    }
    case e: MissingQueryParameterException => {
      logger.warn("Controllers MissingQueryParameterException encountered")
      auditHelper.auditApiFailure(correlationId, matchId, request, url, e.getMessage)
      ErrorInvalidRequest(e.getMessage).toHttpResponse
    }
    case e: Exception => {
      logger.warn("Controllers Exception encountered")
      auditHelper.auditApiFailure(correlationId, matchId, request, url, e.getMessage)
      ErrorInternalServer("Something went wrong.").toHttpResponse
    }
  }

}

trait PrivilegedAuthentication extends AuthorisedFunctions {

  def authPredicate(scopes: Iterable[String]): Predicate =
    scopes.map(Enrolment(_): Predicate).reduce(_ or _)

  def authenticate(endpointScopes: Iterable[String],
                   matchId: String)
                  (f: Iterable[String] => Future[Result])
                  (implicit hc: HeaderCarrier,
                   request: RequestHeader,
                   auditHelper: AuditHelper,
                    ec: ExecutionContext): Future[Result] = {

    if (endpointScopes.isEmpty) throw new Exception("No scopes defined")

      authorised(authPredicate(endpointScopes)).retrieve(Retrievals.allEnrolments) {
        scopes => {
          auditHelper.auditAuthScopes(matchId, scopes.enrolments.map(e => e.key).mkString(","), request)
          f(scopes.enrolments.map(e => e.key))
        }
      }
  }

  def requiresPrivilegedAuthentication(scope: String)(body: => Future[Result])(
    implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Result] = authorised(Enrolment(scope))(body)
}

