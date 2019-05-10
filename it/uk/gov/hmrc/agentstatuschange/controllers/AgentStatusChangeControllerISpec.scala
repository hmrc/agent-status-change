package uk.gov.hmrc.agentstatuschange.controllers

import akka.util.Timeout
import org.joda.time.{DateTime, LocalDate}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.contentAsJson
import uk.gov.hmrc.agentmtdidentifiers.model.{Arn, Utr}
import uk.gov.hmrc.agentstatuschange.models._
import uk.gov.hmrc.agentstatuschange.services.AgentStatusChangeMongoService
import uk.gov.hmrc.agentstatuschange.stubs.{AgentServicesAccountStub, DesStubs}
import uk.gov.hmrc.agentstatuschange.support.{DualSuite, MongoApp, ServerBaseISpec}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration

class AgentStatusChangeControllerISpec extends ServerBaseISpec with AgentServicesAccountStub with MongoApp with DesStubs {

  this: DualSuite =>

  val controller = app.injector.instanceOf(classOf[AgentStatusChangeController])
  def repo: AgentStatusChangeMongoService = app.injector.instanceOf[AgentStatusChangeMongoService]

  val arn = Arn("TARN0000001")
  val utr = Utr("3110118001")
  implicit val timeout: Timeout = Timeout(Duration.Zero)
  implicit val ord: Ordering[DateTime] =
    Ordering.by(time => time.getMillis)

  "AgentStatusChangeController" when {
    "GET /status/arn/:arn" should {
      "respond with default stubbed data when no record exists" in {
        givenAgencyNameArn(arn, "Bing Bong")
        val result = controller.getAgentDetailsByArn(arn)(FakeRequest())
        status(result) shouldBe 200
        contentAsJson(result) shouldBe Json.obj("agentStatus" -> Active,
          "agencyName" -> Some("Bing Bong"))
      }

      "respond with data when a suspended record exists" in {
        await(repo.createRecord(AgentStatusChangeRecord(arn, Suspended(Reason(Some("other"), Some("eaten by tyrannosaur"))), DateTime.parse("2019-01-01"))))
        givenAgencyNameArn(arn, "Bing Bong")
        val result = controller.getAgentDetailsByArn(arn)(FakeRequest())
        status(result) shouldBe 200
        contentAsJson(result) shouldBe Json.obj("agentStatus" -> Suspended(Reason(Some("other"), Some("eaten by tyrannosaur"))),
          "agencyName" -> Some("Bing Bong"))
      }
      "respond with data when a deactivated record exists" in {
        await(repo.createRecord(AgentStatusChangeRecord(arn, Deactivated(Reason(Some("other"), Some("brain in jar"))), DateTime.parse("2019-01-01"))))
        givenAgencyNameArn(arn, "Bing Bong")
        val result = controller.getAgentDetailsByArn(arn)(FakeRequest())
        status(result) shouldBe 200
        contentAsJson(result) shouldBe Json.obj("agentStatus" -> Deactivated(Reason(Some("other"), Some("brain in jar"))),
          "agencyName" -> Some("Bing Bong"))
      }
    }
    "GET /status/utr/:utr" should {
      "respond with data when active" in {
        givenBusinessPartnerRecordExistsFor(utr, arn, "Bong Bing")
        val result = controller.getAgentDetailsByUtr(utr)(FakeRequest())
        status(result) shouldBe 200
        contentAsJson(result) shouldBe Json.obj("agentStatus" -> Active,
          "agencyName" -> Some("Bong Bing"))
      }
      "respond with data when a suspended record exists" in {
        await(repo.createRecord(AgentStatusChangeRecord(arn, Suspended(Reason(Some("other"), Some("eaten by tyrannosaur"))), DateTime.parse("2019-01-01"))))
        givenBusinessPartnerRecordExistsFor(utr, arn, "Bong Bing")
        val result = controller.getAgentDetailsByUtr(utr)(FakeRequest())
        status(result) shouldBe 200
        contentAsJson(result) shouldBe Json.obj("agentStatus" -> Suspended(Reason(Some("other"), Some("eaten by tyrannosaur"))),
          "agencyName" -> Some("Bong Bing"))
      }
      "respond with data when a deactivated record exists" in {
        await(repo.createRecord(AgentStatusChangeRecord(arn, Deactivated(Reason(Some("other"), Some("brain in jar"))), DateTime.parse("2019-01-01"))))
        givenBusinessPartnerRecordExistsFor(utr, arn, "Bong Bing")
        val result = controller.getAgentDetailsByUtr(utr)(FakeRequest())
        status(result) shouldBe 200
        contentAsJson(result) shouldBe Json.obj("agentStatus" -> Deactivated(Reason(Some("other"), Some("brain in jar"))),
          "agencyName" -> Some("Bong Bing"))
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
  }
}
