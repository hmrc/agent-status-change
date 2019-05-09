package uk.gov.hmrc.agentstatuschange.controllers

import akka.util.Timeout
import org.scalatest.Suite
import org.scalatestplus.play.ServerProvider
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc.Result
import play.api.test.FakeRequest
import uk.gov.hmrc.agentmtdidentifiers.model.{Arn, Utr}
import uk.gov.hmrc.agentstatuschange.models.{Reason, Suspended}
import uk.gov.hmrc.agentstatuschange.stubs.{AgentServicesAccountStub, DesStubs}
import uk.gov.hmrc.agentstatuschange.support.{DualSuite, MongoApp, ServerBaseISpec}
import play.api.test.Helpers.contentAsJson

import scala.concurrent.Future
import scala.concurrent.duration.Duration

class AgentStatusChangeControllerSuspendedISpec extends ServerBaseISpec with AgentServicesAccountStub with MongoApp with DesStubs {

  this: Suite with ServerProvider with DualSuite =>

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      "microservice.services.auth.port" -> wireMockPort,
      "metrics.enabled" -> true,
      "auditing.enabled" -> true,
      "auditing.consumer.baseUri.host" -> wireMockHost,
      "auditing.consumer.baseUri.port" -> wireMockPort,
      "microservice.services.agent-services-account.host" -> wireMockHost,
      "microservice.services.agent-services-account.port" -> wireMockPort,
      "microservice.services.des.host" -> wireMockHost,
      "microservice.services.des.port" -> wireMockPort,
      "mongodb.uri" -> s"mongodb://127.0.0.1:27017/test-${this.getClass.getSimpleName}",
      "test.stubbed.status" -> "Suspended"
    ).build()

  val url = s"http://localhost:$port/agent-status-change"

  val wsClient = app.injector.instanceOf[WSClient]

  val controller = app.injector.instanceOf(classOf[AgentStatusChangeController])

  def getAgentDetailsByUtr(utr: String): WSResponse = {
    wsClient.url(s"$url/status/utr/$utr")
      .get()
      .futureValue
  }

  val arn = Arn("TARN0000001")

  val utr = Utr("3110118001")

  "AgentStatusChangeController" when {
    "GET /status/arn/:arn" should {
      implicit val timeout: Timeout = Timeout(Duration.Zero)

      "respond with data when inactive" in {
        givenAgencyNameArn(arn, "Bing Bong")
        val result = controller.getAgentDetailsByArn(arn)(FakeRequest())
        status(result) shouldBe 200
        contentAsJson(result) shouldBe Json.obj("agentStatus" -> Suspended(Reason(Some("some stubbed suspension reason"))),
          "agencyName" -> Some("Bing Bong"))
      }
    }
    "GET /status/utr/:utr" should {
      "respond with data when inactive" in {
        givenBusinessPartnerRecordExistsFor(utr, arn, "Mr Pink")
        val result = getAgentDetailsByUtr(utr.value)
        result.status shouldBe 200
        result.json shouldBe Json.obj("agentStatus" -> Suspended(Reason(Some("some stubbed suspension reason"))),
          "agencyName" -> Some("Mr Pink"))
      }
    }
    "POST /status/arn/:arn" should {
      val request = FakeRequest()

      "return 200 when the json body is valid" in {
        val requestBody = Json.parse(
          """{
            |  "reason": "other",
            |  "extraDetails": "missed the train"
            |}""".stripMargin)

        val result: Future[Result] = controller.changeStatus(arn)(request.withBody(requestBody))
        status(result) shouldBe 200
      }
    }
  }
}
