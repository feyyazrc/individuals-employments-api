/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.Logger
import play.api.mvc.{Action, AnyContent, ControllerComponents, Request}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.individualsemploymentsapi.controller.Environment.{PRODUCTION, SANDBOX}
import uk.gov.hmrc.individualsemploymentsapi.controller.{CommonController, PrivilegedAuthentication}
import uk.gov.hmrc.individualsemploymentsapi.domain.Employment
import uk.gov.hmrc.individualsemploymentsapi.service.{EmploymentsService, LiveEmploymentsService, SandboxEmploymentsService, ScopesService}

import scala.concurrent.ExecutionContext.Implicits.global

abstract class EmploymentsController(
  employmentsService: EmploymentsService,
  scopeService: ScopesService,
  cc: ControllerComponents)
    extends CommonController(cc) with PrivilegedAuthentication {

  val hmctsClientId: String

  def root(matchId: UUID): Action[AnyContent] = Action.async { implicit request =>
    {
      val scopes = scopeService.getEndPointScopes("individuals-employments")
      requiresPrivilegedAuthentication(scopes)
        .flatMap { authScopes =>
          throw new Exception("NOT_IMPLEMENTED")
        }
        .recover(recovery)
    }
  }

  def paye(matchId: UUID, interval: Interval): Action[AnyContent] = Action.async { implicit request =>
    {
      val scopes = scopeService.getEndPointScopes("individuals-employments-paye")
      requiresPrivilegedAuthentication(scopes)
        .flatMap { authScopes =>
          throw new Exception("NOT_IMPLEMENTED")
        }
        .recover(recovery)
    }
  }

  // Home Office and HMCTS want to use the same endpoint,
  // but HO aren't authorised to view payroll IDs or employee addresses
  // so this filters fields based on client ID
  private def filterPayrollData(employments: Seq[Employment])(implicit request: Request[AnyContent]): Seq[Employment] =
    request.headers.get("X-Client-ID") match {
      case Some(clientId) if clientId == hmctsClientId => employments
      case Some(_)                                     => employments.map(_.copy(payrollId = None, employeeAddress = None))
      case None =>
        Logger.warn("Missing X-Client-Id header")
        employments.map(_.copy(payrollId = None, employeeAddress = None))
    }

}

@Singleton
class SandboxEmploymentsController @Inject()(
  sandboxEmploymentsService: SandboxEmploymentsService,
  scopeService: ScopesService,
  val authConnector: AuthConnector,
  @Named("hmctsClientId") val hmctsClientId: String,
  cc: ControllerComponents)
    extends EmploymentsController(sandboxEmploymentsService, scopeService, cc) {

  override val environment: String = SANDBOX
}

@Singleton
class LiveEmploymentsController @Inject()(
  liveEmploymentsService: LiveEmploymentsService,
  scopeService: ScopesService,
  val authConnector: AuthConnector,
  @Named("hmctsClientId") val hmctsClientId: String,
  cc: ControllerComponents)
    extends EmploymentsController(liveEmploymentsService, scopeService, cc) {

  override val environment: String = PRODUCTION
}
