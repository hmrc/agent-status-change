package uk.gov.hmrc.agentstatuschange.controllers

import akka.util.Timeout
import org.joda.time.DateTime
import play.api.libs.json.{JsString, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.contentAsJson
import uk.gov.hmrc.agentmtdidentifiers.model.{Arn, Utr}
import uk.gov.hmrc.agentstatuschange.models._
import uk.gov.hmrc.agentstatuschange.services.AgentStatusChangeMongoService
import uk.gov.hmrc.agentstatuschange.stubs.{AgentStubs, DesStubs}
import uk.gov.hmrc.agentstatuschange.support.{DualSuite, MongoApp, ServerBaseISpec}
import uk.gov.hmrc.http.SessionKeys

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration

class AgentStatusChangeControllerISpec extends ServerBaseISpec with MongoApp with DesStubs with AgentStubs {

  this: DualSuite =>

  val controller = app.injector.instanceOf(classOf[AgentStatusChangeController])

  def repo: AgentStatusChangeMongoService = app.injector.instanceOf[AgentStatusChangeMongoService]

  val utr = Utr("3110118001")
  implicit val timeout: Timeout = Timeout(Duration.Zero)
  implicit val ord: Ordering[DateTime] =
    Ordering.by(time => time.getMillis)

  "AgentStatusChangeController" when {
    "GET /status/arn/:arn" should {
      "respond 200 with default stubbed data when no record exists" in {
        givenBusinessPartnerRecordExistsFor("arn", utr, arn, "Bing Bing")
        val result = controller.getAgentDetailsByArn(arn)(FakeRequest())
        status(result) shouldBe 200
        contentAsJson(result) shouldBe Json.obj("arn" -> arn, "agentStatus" -> Active,
          "agencyName" -> Some("Bing Bing"))
      }

      "respond 200 with data when a suspended record exists" in {
        await(repo.createRecord(AgentStatusChangeRecord(arn, Suspended(Reason(Some("other"), Some("eaten by tyrannosaur"))), DateTime.parse("2019-01-01"))))
        givenBusinessPartnerRecordExistsFor("arn", utr, arn, "Bong Bing")
        val result = controller.getAgentDetailsByArn(arn)(FakeRequest())
        status(result) shouldBe 200
        contentAsJson(result) shouldBe Json.obj("arn" -> arn, "agentStatus" -> Suspended(Reason(Some("other"), Some("eaten by tyrannosaur"))),
          "agencyName" -> Some("Bong Bing"))
      }
      "respond 200 with data when a deactivated record exists" in {
        await(repo.createRecord(AgentStatusChangeRecord(arn, Deactivated(Reason(Some("other"), Some("brain in jar"))), DateTime.parse("2019-01-01"))))
        givenBusinessPartnerRecordExistsFor("arn", utr, arn, "Bong Bing")
        val result = controller.getAgentDetailsByArn(arn)(FakeRequest())
        status(result) shouldBe 200
        contentAsJson(result) shouldBe Json.obj("arn" -> arn, "agentStatus" -> Deactivated(Reason(Some("other"), Some("brain in jar"))),
          "agencyName" -> Some("Bong Bing"))
      }
      "respond 404 with reason when record does not exist" in {
        givenBusinessPartnerRecordNotFoundFor("arn", utr, arn, "")
        val result = controller.getAgentDetailsByArn(arn)(FakeRequest())
        status(result) shouldBe 404
        contentAsJson(result) shouldBe JsString("UTR_NOT_SUBSCRIBED")
      }
      "respond 400 with reason when utr is invalid" in {
        givenBusinessPartnerRecordInvalidFor("arn", utr, arn, "")
        val result = controller.getAgentDetailsByArn(arn)(FakeRequest())
        status(result) shouldBe 400
        contentAsJson(result) shouldBe JsString("INVALID_UTR")
      }
    }
    "GET /status/utr/:utr" should {
      "respond with data when active" in {
        givenBusinessPartnerRecordExistsFor("utr", utr, arn, "Bong Bing")
        val result = controller.getAgentDetailsByUtr(utr)(FakeRequest())
        status(result) shouldBe 200
        contentAsJson(result) shouldBe Json.obj("arn" -> arn, "agentStatus" -> Active,
          "agencyName" -> Some("Bong Bing"))
      }
      "respond with data when a suspended record exists" in {
        await(repo.createRecord(AgentStatusChangeRecord(arn, Suspended(Reason(Some("other"), Some("eaten by tyrannosaur"))), DateTime.parse("2019-01-01"))))
        givenBusinessPartnerRecordExistsFor("utr", utr, arn, "Bong Bing")
        val result = controller.getAgentDetailsByUtr(utr)(FakeRequest())
        status(result) shouldBe 200
        contentAsJson(result) shouldBe Json.obj("arn" -> arn, "agentStatus" -> Suspended(Reason(Some("other"), Some("eaten by tyrannosaur"))),
          "agencyName" -> Some("Bong Bing"))
      }
      "respond with data when a deactivated record exists" in {
        await(repo.createRecord(AgentStatusChangeRecord(arn, Deactivated(Reason(Some("other"), Some("brain in jar"))), DateTime.parse("2019-01-01"))))
        givenBusinessPartnerRecordExistsFor("utr", utr, arn, "Bong Bing")
        val result = controller.getAgentDetailsByUtr(utr)(FakeRequest())
        status(result) shouldBe 200
        contentAsJson(result) shouldBe Json.obj("arn" -> arn, "agentStatus" -> Deactivated(Reason(Some("other"), Some("brain in jar"))),
          "agencyName" -> Some("Bong Bing"))
      }
      "respond 404 with reason when record does not exist" in {
        givenBusinessPartnerRecordNotFoundFor("utr", utr, arn, "")
        val result = controller.getAgentDetailsByUtr(utr)(FakeRequest())
        status(result) shouldBe 404
        contentAsJson(result) shouldBe JsString("UTR_NOT_SUBSCRIBED")
      }
      "respond 400 with reason when utr is invalid" in {
        givenBusinessPartnerRecordInvalidFor("utr", utr, arn, "")
        val result = controller.getAgentDetailsByUtr(utr)(FakeRequest())
        status(result) shouldBe 400
        contentAsJson(result) shouldBe JsString("INVALID_UTR")
      }
    }
    "POST /status/arn/:arn" should {
      val request = FakeRequest()

      "return 200 and create a new suspended record when a reason is provided" in {
        val requestBody = Json.parse(
          """{
            |  "reason": "other",
            |  "extraDetails": "missed the train"
            |}""".stripMargin)

        val result: Future[Result] = controller.changeStatus(arn)(request.withBody(requestBody))
        status(result) shouldBe 200
        await(repo.findCurrentRecordByArn(arn.value)).get.status shouldBe Suspended(Reason(Some("other"), Some("missed the train")))
      }
      "return 200 and create a new active record when a reason is not provided" in {
        val requestBody = Json.parse(
          """{}""".stripMargin)

        val result: Future[Result] = controller.changeStatus(arn)(request.withBody(requestBody))
        status(result) shouldBe 200
        await(repo.findCurrentRecordByArn(arn.value)).get.status shouldBe Active
      }
    }

    "DELETE /agent/:arn/terminate" should {
      "return 200 for deleting all agent records" in {
        givenSuccessfullyRemoveInvitations(arn)
        givenSuccessfullyRemoveAFiRelationships(arn)
        givenSuccessfullyRemoveMapping(arn)

        givenOnlyStrideStub("caat", "1234")
        val result = controller.removeAgentRecords(arn)(FakeRequest("DELETE", "agent/:arn/terminate").withSession(SessionKeys.authToken -> "Bearer XYZ"))

        status(result) shouldBe 200
      }

      "return 400 for invalid ARN" in {
        givenSuccessfullyRemoveInvitations(arn)
        givenSuccessfullyRemoveAFiRelationships(arn)
        givenSuccessfullyRemoveMapping(arn)

        givenOnlyStrideStub("caat", "1234")
        val result = controller.removeAgentRecords(Arn("MARN01"))(FakeRequest("DELETE", "agent/:arn/terminate").withSession(SessionKeys.authToken -> "Bearer XYZ"))

        status(result) shouldBe 400
      }

      "return 500 for complete error" in {
        givenInternalServerErrorRemoveInvitations(arn)
        givenInternalServerErrorIRemoveAFiRelationships(arn)
        givenInternalServerErrorRemoveMapping(arn)
        givenOnlyStrideStub("caat", "1234")
        val result = controller.removeAgentRecords(arn)(FakeRequest("DELETE", "agent/:arn/terminate").withSession(SessionKeys.authToken -> "Bearer XYZ"))

        status(result) shouldBe 500
      }

      "return 500 for partial error" in {
        givenSuccessfullyRemoveInvitations(arn)
        givenSuccessfullyRemoveAFiRelationships(arn)
        givenInternalServerErrorRemoveMapping(arn)
        givenOnlyStrideStub("caat", "1234")
        val result = controller.removeAgentRecords(arn)(FakeRequest("DELETE", "agent/:arn/terminate").withSession(SessionKeys.authToken -> "Bearer XYZ"))

        status(result) shouldBe 500
      }
    }
  }
}
