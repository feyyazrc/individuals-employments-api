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

package it.uk.gov.hmrc.individualsemploymentsapi.cache.v2.services

import it.uk.gov.hmrc.individualsemploymentsapi.cache.suite.MongoSuite
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{FreeSpec, MustMatchers, OptionValues}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{Json, OFormat}
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.individualsemploymentsapi.service.v2.CacheServiceV2

import scala.concurrent.Future

class CacheServiceSpec
    extends FreeSpec with MustMatchers with ScalaFutures with OptionValues with MongoSuite with IntegrationPatience {

  implicit val hc = HeaderCarrier()

  "cache service" - {

    "must fetch an entry" in {

      val app = new GuiceApplicationBuilder().build()

      running(app) {

        val svc = app.injector.instanceOf[CacheServiceV2]

        svc
          .get("foo", Future.successful(TestClass("bar")))
          .futureValue mustEqual TestClass("bar")

      }
    }

    "must fetch an existing entry when not expired " in {

      val app = new GuiceApplicationBuilder().build()

      running(app) {

        val svc = app.injector.instanceOf[CacheServiceV2]

        svc
          .get("foo", Future.successful(TestClass("bar")))
          .futureValue mustEqual TestClass("bar")
        svc
          .get("foo", Future.successful(TestClass("miss")))
          .futureValue mustEqual TestClass("bar")
        svc
          .get("bar", Future.successful(TestClass("miss")))
          .futureValue mustEqual TestClass("miss")

      }
    }
  }
}

case class TestClass(param: String)

object TestClass {

  implicit val format: OFormat[TestClass] = Json.format[TestClass]

}
