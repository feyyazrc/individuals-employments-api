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

package uk.gov.hmrc.individualsemploymentsapi.controller

import controllers.Assets
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.http.HttpErrorHandler
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.individualsemploymentsapi.views._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

@Singleton
class DocumentationController @Inject()(
  cc: ControllerComponents,
  assets: Assets,
  errorHandler: HttpErrorHandler,
  config: Configuration)
    extends BackendController(cc) {
  private lazy val privilegedWhitelistedApplicationIds =
    config.getOptional[Seq[String]]("api.access.version-P1.0.whitelistedApplicationIds").getOrElse(Seq.empty)

  def definition(): Action[AnyContent] = Action { request =>
    Ok(txt.definition(privilegedWhitelistedApplicationIds)).withHeaders(CONTENT_TYPE -> JSON)
  }
  def documentation(
    version: String,
    endpointName: String
  ): Action[AnyContent] =
    assets.at(s"/public/api/documentation/$version", s"${endpointName.replaceAll(" ", "-")}.xml")

  def raml(version: String, file: String) =
    assets.at(s"/public/api/conf/$version", file)

}
