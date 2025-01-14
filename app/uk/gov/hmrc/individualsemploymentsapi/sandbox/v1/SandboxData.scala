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

package uk.gov.hmrc.individualsemploymentsapi.sandbox.v1

import org.joda.time.LocalDate
import org.joda.time.LocalDate.parse
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.individualsemploymentsapi.domain.des.{DesAddress, DesEmployment, DesPayment}
import uk.gov.hmrc.individualsemploymentsapi.domain.{PayFrequencyCode, des}

import java.util.UUID

object SandboxData {

  val sandboxMatchIdString = "57072660-1df9-4aeb-b4ea-cd2d7f96e430"
  val sandboxMatchId = UUID.fromString(sandboxMatchIdString)

  val sandboxNinoString = "NA000799C"
  val sandboxNino = Nino(sandboxNinoString)

  object Employments {
    val acme = DesEmployment(
      Seq(
        DesPayment(new LocalDate(2016, 1, 28), 0),
        DesPayment(new LocalDate(2016, 2, 28), 0),
        DesPayment(new LocalDate(2016, 3, 28), 0),
        DesPayment(new LocalDate(2016, 4, 28), 0),
        DesPayment(new LocalDate(2016, 5, 28), 0)
      ),
      Some("Acme"),
      Some(
        DesAddress(
          line1 = Some("Acme Inc Building"),
          line2 = Some("Acme Inc Campus"),
          line3 = Some("Acme Street"),
          line4 = Some("AcmeVille"),
          line5 = Some("Acme State"),
          postalCode = Some("AI22 9LL")
        )),
      Some("123"),
      Some("AI45678"),
      Some(new LocalDate(2016, 1, 1)),
      Some(new LocalDate(2016, 6, 30)),
      Some(PayFrequencyCode.W4),
      Some(
        DesAddress(
          line1 = Some("Employee's House"),
          line2 = Some("Employee Street"),
          line3 = Some("Employee Town"),
          line4 = None,
          line5 = None,
          postalCode = Some("AA11 1AA")
        )),
      Some("payroll-id")
    )
    val disney = DesEmployment(
      Seq(
        DesPayment(new LocalDate(2017, 2, 19), 0),
        DesPayment(new LocalDate(2017, 2, 28), 0)
      ),
      Some("Disney"),
      Some(
        DesAddress(
          line1 = Some("Friars House"),
          line2 = Some("Campus Way"),
          line3 = Some("New Street"),
          line4 = Some("Sometown"),
          line5 = Some("Old County"),
          postalCode = Some("TF22 3BC")
        )),
      Some("123"),
      Some("DI45678"),
      Some(parse("2017-01-02")),
      Some(parse("2017-03-01")),
      Some(PayFrequencyCode.W2),
      Some(
        DesAddress(
          line1 = None,
          line2 = None,
          line3 = None,
          line4 = None,
          line5 = None,
          postalCode = None
        )),
      Some("another-payroll-id")
    )
  }

  object Individuals {

    val amanda = des.Individual(
      sandboxMatchId,
      sandboxNinoString,
      Seq(Employments.acme, Employments.disney)
    )

    val individuals = Seq(amanda)

    def find(matchId: UUID) = individuals.find(_.matchId == matchId)

  }

}
