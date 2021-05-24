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

package uk.gov.hmrc.individualsemploymentsapi.service.v2

import play.api.mvc.RequestHeader
import uk.gov.hmrc.api.controllers.ErrorGenericBadRequest
import uk.gov.hmrc.individualsemploymentsapi.error.ErrorResponses.MissingQueryParameterException

import javax.inject.Inject

case class VerifyResult(hasAllParameters: Boolean, requiredFields: List[String])

class ScopeFilterVerificationService @Inject()(scopesService: ScopesService, scopesHelper: ScopesHelper) {

  val filterParameterMappings = Map(
    "M" -> "employerRef"
  )

  def verify(scopes: List[String], endpoint: String, rh: RequestHeader): VerifyResult = {
    val validFilters = scopesService.getValidFilterKeys(scopes, List(endpoint))
    val requiredParameters = validFilters.flatMap(f => filterParameterMappings.get(f)).toList
    val hasAllParameters = requiredParameters.isEmpty || !requiredParameters.map(p => rh.queryString.get(p)).exists(_.isEmpty)
    if (!hasAllParameters) throw new MissingQueryParameterException(s"${requiredParameters.head} is required for the scopes you have been assigned")
    VerifyResult(hasAllParameters, requiredParameters)
  }

   def getQueryStringForDefinedScopes(scopes: List[String], endpoint: String, rh: RequestHeader): String = {
     val verifyResult = verify(scopes, endpoint, rh)
     println(verifyResult)
     if (verifyResult.hasAllParameters && verifyResult.requiredFields.isEmpty) {
        scopesHelper.getQueryStringFor(scopes, endpoint)
     }
     else if (verifyResult.hasAllParameters && verifyResult.requiredFields.contains("employerRef")) {
       val extractedEmployerRef = rh.queryString.get("employerRef").map(x => x.head).get
      scopesHelper.getQueryStringWithParameterisedFilters(scopes, endpoint, extractedEmployerRef)
     }
     else {
       throw new MissingQueryParameterException(s"${verifyResult.requiredFields.head} is required for the scopes you have been assigned")
     }
   }
}