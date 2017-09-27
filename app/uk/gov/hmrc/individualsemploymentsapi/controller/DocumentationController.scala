/*
 * Copyright 2017 HM Revenue & Customs
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


import javax.inject.{Inject, Singleton}

import play.api.Configuration
import play.api.http.HttpErrorHandler
import play.api.mvc.{Action, AnyContent, RequestHeader}
import uk.gov.hmrc.individualsemploymentsapi.views._

@Singleton
class DocumentationController @Inject()(httpErrorHandler: HttpErrorHandler, configuration: Configuration) extends uk.gov.hmrc.api.controllers.DocumentationController(httpErrorHandler) {

  private lazy val standardWhitelistedApplicationIds = configuration.getStringSeq("api.access.version-1.0.whitelistedApplicationIds").getOrElse(Seq.empty)
  private lazy val privilegedWhitelistedApplicationIds = configuration.getStringSeq("api.access.version-P1.0.whitelistedApplicationIds").getOrElse(Seq.empty)

  override def definition(): Action[AnyContent] = Action { request =>
    Ok(txt.definition(standardWhitelistedApplicationIds, privilegedWhitelistedApplicationIds)).withHeaders(CONTENT_TYPE -> JSON)
  }

  def raml(version: String, file: String) = {
    super.at(s"/public/api/conf/$version", file)
  }

}
