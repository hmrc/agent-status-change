/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.agentstatuschange.stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import uk.gov.hmrc.agentmtdidentifiers.model.Arn
import uk.gov.hmrc.agentmtdidentifiers.model.Utr
import uk.gov.hmrc.agentstatuschange.support.WireMockSupport
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock._

trait DesStubs {

  me: WireMockSupport =>

  def givenBusinessPartnerRecordExistsFor(
    idType: String,
    utr: Utr,
    arn: Arn,
    agencyName: String
  ): StubMapping = stubFor(
    get(urlEqualTo(idType match {
      case "arn" => s"/registration/personal-details/arn/${arn.value}"
      case "utr" => s"/registration/personal-details/utr/${utr.value}"
    }))
      .willReturn(aResponse()
        .withStatus(200)
        .withBody(s"""
                     |{
                     |  "businessPartnerExists" : true,
                     |  "safeId" : "XT0002899319108",
                     |  "agentReferenceNumber" : "${arn.value}",
                     |  "utr" : "${utr.value}",
                     |  "nino" : "AB593074A",
                     |  "eori" : "QL593074449043",
                     |  "crn" : "QL593074",
                     |  "isAnAgent" : true,
                     |  "isAnASAgent" : true,
                     |  "isAnIndividual" : true,
                     |  "individual" : {
                     |    "firstName" : "Aiden",
                     |    "middleName" : "Jayden",
                     |    "lastName" : "Smith",
                     |    "dateOfBirth" : "2015-09-15"
                     |  },
                     |  "isAnOrganisation" : false,
                     |  "addressDetails" : {
                     |    "addressLine1" : "2 Keswick",
                     |    "addressLine2" : "Kilmarnock",
                     |    "postalCode" : "KA09 2DP",
                     |    "countryCode" : "GB"
                     |  },
                     |  "contactDetails" : {
                     |    "phoneNumber" : "02151 130927",
                     |    "mobileNumber" : "02151 130927",
                     |    "faxNumber" : "02151 130927",
                     |    "emailAddress" : "9gfvnqnypvlg@o.uk"
                     |  },
                     |  "agencyDetails" : {
                     |    "agencyName" : "$agencyName",
                     |    "agencyAddress" : {
                     |      "addressLine1" : "2 Keswick",
                     |      "addressLine2" : "Kilmarnock",
                     |      "postalCode" : "KA09 2DP",
                     |      "countryCode" : "GB"
                     |    },
                     |    "agencyEmail" : "4@s1.me"
                     |  },
                     |  "id" : "5cb5cd7f1800006589a83ac5"
                     |}
            """.stripMargin))
  )

  def givenBusinessPartnerRecordNotFoundFor(
    idType: String,
    utr: Utr,
    arn: Arn,
    agencyName: String
  ): StubMapping = stubFor(
    get(urlEqualTo(idType match {
      case "arn" => s"/registration/personal-details/arn/${arn.value}"
      case "utr" => s"/registration/personal-details/utr/${utr.value}"
    }))
      .willReturn(aResponse()
        .withStatus(404))
  )

  def givenBusinessPartnerRecordInvalidFor(
    idType: String,
    utr: Utr,
    arn: Arn,
    agencyName: String
  ): StubMapping = stubFor(
    get(urlEqualTo(idType match {
      case "arn" => s"/registration/personal-details/arn/${arn.value}"
      case "utr" => s"/registration/personal-details/utr/${utr.value}"
    }))
      .willReturn(aResponse()
        .withStatus(400))
  )

}
