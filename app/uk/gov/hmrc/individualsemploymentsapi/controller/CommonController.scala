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

import java.util.UUID

import org.joda.time.DateTime
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.individualsemploymentsapi.error.ErrorResponses.{ErrorInvalidRequest, ErrorNotFound, MatchNotFoundException}
import uk.gov.hmrc.individualsemploymentsapi.util.Dates._
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.Future
import scala.util.{Success, Try}

trait CommonController extends BaseController {

  protected def withUuid(uuidString: String)(f: UUID => Future[Result]): Future[Result] = {
    Try(UUID.fromString(uuidString)) match {
      case Success(uuid) => f(uuid)
      case _ => Future.successful(ErrorNotFound.toHttpResponse)
    }
  }

  private def getQueryParam[T](name: String)(implicit request: Request[T]) = request.queryString.get(name).flatMap(_.headOption)

  private[controller] def urlWithInterval[T](url: String, fromDate: DateTime)(implicit request: Request[T]) = {
    val urlWithFromDate = s"$url?fromDate=${toFormattedLocalDate(fromDate)}"
    getQueryParam("toDate") map (toDate => s"$urlWithFromDate&toDate=$toDate") getOrElse urlWithFromDate
  }

  private[controller] def recovery: PartialFunction[Throwable, Result] = {
    case _: MatchNotFoundException => ErrorNotFound.toHttpResponse
    case e: IllegalArgumentException => ErrorInvalidRequest(e.getMessage).toHttpResponse
  }

}
