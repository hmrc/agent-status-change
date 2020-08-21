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

package models

import play.api.libs.json.Json
import uk.gov.hmrc.agentmtdidentifiers.model.Arn
import uk.gov.hmrc.agentstatuschange.models.ArnAndAgencyName
import uk.gov.hmrc.play.test.UnitSpec

class ArnAndAgencyNameSpec extends UnitSpec {

  "ArnAndAgencyName" should {
    "serialize to json string" in {
      Json.toJson(ArnAndAgencyName(Arn("TARN0000001"), "my agency name")) shouldBe
        Json.parse("""{"arn":"TARN0000001", "agencyName":"my agency name"}""")
    }

    "deserialize from json string" in {
      Json
        .parse(
          """{"agentReferenceNumber": "TARN0000001", "agencyDetails": {"agencyName": "my agency name"}}""")
        .as[ArnAndAgencyName] shouldBe ArnAndAgencyName(Arn("TARN0000001"),
                                                        "my agency name")
    }
  }
}
