/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.agentstatuschange.binders

import uk.gov.hmrc.agentmtdidentifiers.model.{Arn, Utr}
import uk.gov.hmrc.agentstatuschange.support.UnitSpec

class UrlBindersSpec extends UnitSpec {

  val utr = Utr("3110118001")
  val arn = Arn("TARN0000001")

  "Utr binder" should {
    "bind a utr from a valid string" in {
      UrlBinders.utrBinder.bind("utr", utr.value) shouldBe Right(utr)
    }
    "unbind a utr to a string" in {
      UrlBinders.utrBinder.unbind("utr", utr) shouldBe utr.value
    }
  }

  "Arn binder" should {
    "bind an arn from a valid string" in {
      UrlBinders.arnBinder.bind("arn", arn.value) shouldBe Right(arn)
    }
    "unbind an arn to a string" in {
      UrlBinders.arnBinder.unbind("arn", arn) shouldBe arn.value
    }
  }

}
