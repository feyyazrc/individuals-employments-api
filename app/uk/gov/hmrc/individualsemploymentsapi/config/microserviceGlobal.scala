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

package uk.gov.hmrc.individualsemploymentsapi.config

import akka.actor.ActorSystem
import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import play.api.Mode.Mode
import play.api.libs.json.Json
import play.api.mvc.{RequestHeader, Result}
import play.api.{Application, Configuration, Logger, Play}
import uk.gov.hmrc.api.config.{ServiceLocatorConfig, ServiceLocatorRegistration}
import uk.gov.hmrc.api.connector.ServiceLocatorConnector
import uk.gov.hmrc.auth.core.AuthorisationException
import uk.gov.hmrc.individualsemploymentsapi.error.ErrorResponses.{ErrorInternalServer, ErrorInvalidRequest, ErrorUnauthorized}
import uk.gov.hmrc.individualsemploymentsapi.util.JsonFormatters.errorInvalidRequestFormat
import uk.gov.hmrc.individualsemploymentsapi.util.RequestHeaderUtils._
import uk.gov.hmrc.play.config.{AppName, ControllerConfig}
import uk.gov.hmrc.play.microservice.bootstrap.DefaultMicroserviceGlobal

import scala.concurrent.Future
import scala.concurrent.Future.successful
import scala.util.Try
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.microservice.filters.{AuditFilter, LoggingFilter, MicroserviceFilterSupport}

object ControllerConfiguration extends ControllerConfig {
  lazy val controllerConfigs = Play.current.configuration.underlying.as[Config]("controllers")
}

object MicroserviceAuditFilter extends AuditFilter with AppName with MicroserviceFilterSupport with ConfigSupport {
  override val auditConnector = MicroserviceAuditConnector

  override def controllerNeedsAuditing(controllerName: String) = ControllerConfiguration.paramsForController(controllerName).needsAuditing
}

object MicroserviceLoggingFilter extends LoggingFilter with MicroserviceFilterSupport {
  override def controllerNeedsLogging(controllerName: String) = ControllerConfiguration.paramsForController(controllerName).needsLogging
}


object MicroserviceGlobal extends DefaultMicroserviceGlobal with ServiceLocatorRegistration with ServiceLocatorConfig with MicroserviceFilterSupport with ConfigSupport {
  override val auditConnector = MicroserviceAuditConnector

  override def microserviceMetricsConfig(implicit app: Application): Option[Configuration] = app.configuration.getConfig("microservice.metrics")

  override val loggingFilter = MicroserviceLoggingFilter

  override val microserviceAuditFilter = MicroserviceAuditFilter

  override val authFilter = None

  override implicit val hc = HeaderCarrier()

  override val slConnector = ServiceLocatorConnector(WSHttp)

  override lazy val registrationEnabled = Play.current.configuration.getBoolean("microservice.services.service-locator.enabled").getOrElse(false)

  private lazy val unversionedContexts = Play.current.configuration.getStringSeq("versioning.unversionedContexts").getOrElse(Seq.empty[String])

  override def onRequestReceived(originalRequest: RequestHeader) = {
    val requestContext = extractUriContext(originalRequest)
    if (unversionedContexts.contains(requestContext)) {
      super.onRequestReceived(originalRequest)
    } else {
      super.onRequestReceived(getVersionedRequest(originalRequest))
    }
  }

  override def onBadRequest(request: RequestHeader, error: String): Future[Result] = {
    Try(Json.parse(error).as[ErrorInvalidRequest]).toOption match {
      case Some(errorResponse) => successful(errorResponse.toHttpResponse)
      case _ => successful(ErrorInvalidRequest("Invalid Request").toHttpResponse)
    }
  }

  override def onError(request: RequestHeader, ex: Throwable): Future[Result] = {
    ex match {
      case _: AuthorisationException => successful(ErrorUnauthorized.toHttpResponse)
      case _ =>
        Logger.error("An unexpected error occured", ex)
        successful(ErrorInternalServer.toHttpResponse)
    }
  }
}

trait ConfigSupport {
  private def current: Application = Play.current

  def playConfiguration: Configuration = current.configuration
  def mode: Mode = current.mode

  def runModeConfiguration: Configuration = playConfiguration
  def appNameConfiguration: Configuration = playConfiguration
  def actorSystem: ActorSystem = current.actorSystem
}