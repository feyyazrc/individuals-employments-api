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

import java.util.UUID

import javax.inject.{Inject, Named, Singleton}
import org.joda.time.Interval
import play.api.hal.Hal._
import play.api.mvc.hal._
import play.api.hal.HalLink
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.individualsemploymentsapi.audit.v2.AuditHelper
import uk.gov.hmrc.individualsemploymentsapi.audit.v2.models.Identifiers
import uk.gov.hmrc.individualsemploymentsapi.controller.Environment.{PRODUCTION, SANDBOX}
import uk.gov.hmrc.individualsemploymentsapi.controller.{CommonController, PrivilegedAuthentication}
import uk.gov.hmrc.individualsemploymentsapi.service.v2.{EmploymentsService, LiveEmploymentsService, SandboxEmploymentsService, ScopesHelper, ScopesService}
import uk.gov.hmrc.individualsemploymentsapi.util.RequestHeaderUtils.extractCorrelationId

import scala.concurrent.ExecutionContext

abstract class EmploymentsController(employmentsService: EmploymentsService,
                                     scopeService: ScopesService,
                                     scopesHelper: ScopesHelper,
                                     implicit val auditHelper: AuditHelper,
                                     cc: ControllerComponents)
                                    (implicit val ec: ExecutionContext)
  extends CommonController(cc) with PrivilegedAuthentication {

  def root(matchId: UUID): Action[AnyContent] = Action.async { implicit request =>
    authenticate(scopeService.getAllScopes, matchId.toString) { authScopes =>

      val id = Identifiers(extractCorrelationId(request), matchId, "/individuals/employments")

      employmentsService.resolve(matchId) map { _ =>

        val selfLink = HalLink("self", s"/individuals/employments/?matchId=$matchId")
        val response = scopesHelper.getHalLinks(matchId, authScopes) ++ selfLink

        auditHelper.auditApiResponse(id.correlationIdVal, id.matchIdVal,
          Some(authScopes.mkString(",")), request, selfLink.toString, Json.toJson(response))

        Ok(response)

      } recover withAudit(Some(id.correlationIdVal), id.matchIdVal, id.endpoint)

    } recover withAudit(None, matchId.toString, "/individuals/employments")
  }

  def paye(matchId: UUID, interval: Interval): Action[AnyContent] = Action.async { implicit request =>
    authenticate(scopeService.getEndPointScopes("paye"), matchId.toString) { authScopes =>

      val id = Identifiers(extractCorrelationId(request), matchId, "/individuals/employments/paye")

      employmentsService.paye(matchId, interval, "paye", authScopes).map { employments =>

        val selfLink = HalLink("self", urlWithInterval(s"/individuals/employments/paye?matchId=$matchId", interval.getStart))
        val response = state(Json.obj("employments" -> Json.toJson(employments))) ++ selfLink

        auditHelper.auditApiResponse(id.correlationIdVal, id.matchIdVal, Some(authScopes.mkString(",")),
          request, selfLink.toString, Json.toJson(response))

        Ok(response)

      } recover withAudit(Some(id.correlationIdVal), id.matchIdVal, id.endpoint)

    } recover withAudit(None, matchId.toString, "/individuals/employments/paye")
  }
}

@Singleton
class SandboxEmploymentsController @Inject()(
  sandboxEmploymentsService: SandboxEmploymentsService,
  scopeService: ScopesService,
  scopesHelper: ScopesHelper,
  val authConnector: AuthConnector,
  @Named("hmctsClientId") val hmctsClientId: String,
  auditHelper: AuditHelper,
  cc: ControllerComponents)(override implicit val ec: ExecutionContext)
    extends EmploymentsController(sandboxEmploymentsService, scopeService, scopesHelper, auditHelper, cc) {

  override val environment: String = SANDBOX
}

@Singleton
class LiveEmploymentsController @Inject()(
  liveEmploymentsService: LiveEmploymentsService,
  scopeService: ScopesService,
  scopesHelper: ScopesHelper,
  val authConnector: AuthConnector,
  @Named("hmctsClientId") val hmctsClientId: String,
  auditHelper: AuditHelper,
  cc: ControllerComponents)(override implicit val ec: ExecutionContext)
    extends EmploymentsController(liveEmploymentsService, scopeService, scopesHelper, auditHelper, cc) {

  override val environment: String = PRODUCTION
}
