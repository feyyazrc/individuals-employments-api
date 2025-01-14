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

package unit.uk.gov.hmrc.individualsemploymentsapi.domain.v2

import org.joda.time.LocalDate
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.individualsemploymentsapi.domain.integrationframework.IfPayment
import uk.gov.hmrc.individualsemploymentsapi.domain.v2.Payment

class PaymentSpec extends AnyFlatSpec with Matchers {

  "Employer" should "derive itself from some parameters" in {
    val someDate = Some(new LocalDate(2016, 1, 1))
    val someTaxeablePayement = Some(123.321)

    val payment = Payment(someDate, someTaxeablePayement)
    Payment(someDate, someTaxeablePayement) shouldBe payment
  }

  it should "handle the edge case where parameters are empty" in {
    val ifPayment = IfPayment(None, None, None, None, None, None)
    Payment.create(ifPayment) shouldBe None
  }
}
