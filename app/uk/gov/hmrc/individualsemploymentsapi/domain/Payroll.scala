/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.individualsemploymentsapi.domain

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.individualsemploymentsapi.domain.des.DesEmployment
import uk.gov.hmrc.individualsemploymentsapi.util.JsonFormatters.addressJsonFormat

case class Payroll(employeeAddress: Option[Address], payrollId: Option[String])

object Payroll {
  implicit val format: Format[Payroll] = Json.format[Payroll]

  def from(employment: DesEmployment): Option[Payroll] = {
    val address = employment.employeeAddress.map(Address.from)
    val payrollId = employment.payrollId

    (address, payrollId) match {
      case (None, None) => None
      case _ => Some(Payroll(address, payrollId))
    }
  }
}