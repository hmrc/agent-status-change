package models

import play.api.libs.json.Json
import uk.gov.hmrc.agentstatuschange.models.{DeletionCount, TerminationResponse}
import uk.gov.hmrc.play.test.UnitSpec

class TerminationResponseSpec extends UnitSpec {

  "TerminationResponse" should {
    "deserialize from json" in {
      Json
        .parse("""
            |{
            |  "counts": [
            |    {
            |      "service": "example-service",
            |      "store": "example-store",
            |      "count": 123
            |    }
            |  ]
            |}""".stripMargin)
        .as[TerminationResponse] shouldBe TerminationResponse(
        Seq(
          DeletionCount("example-service", "example-store", 123)
        ))
    }
  }

}
