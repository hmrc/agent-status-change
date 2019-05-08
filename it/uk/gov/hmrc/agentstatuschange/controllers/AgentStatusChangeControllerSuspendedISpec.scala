package uk.gov.hmrc.agentstatuschange.controllers

import org.scalatest.Suite
import org.scalatestplus.play.ServerProvider
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.agentmtdidentifiers.model.{Arn, Utr}
import uk.gov.hmrc.agentstatuschange.models.{Active, Suspended}
import uk.gov.hmrc.agentstatuschange.stubs.AgentServicesAccountStub
import uk.gov.hmrc.agentstatuschange.support.ServerBaseISpec

class AgentStatusChangeControllerSuspendedISpec extends ServerBaseISpec with AgentServicesAccountStub {

  this: Suite with ServerProvider =>

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      "microservice.services.auth.port" -> wireMockPort,
      "metrics.enabled" -> true,
      "auditing.enabled" -> true,
      "auditing.consumer.baseUri.host" -> wireMockHost,
      "auditing.consumer.baseUri.port" -> wireMockPort,
      "microservice.services.agent-services-account.host" -> wireMockHost,
      "microservice.services.agent-services-account.port" -> wireMockPort,
      "test.stubbed.status" -> "Suspended"
    ).build()

  val url = s"http://localhost:$port/agent-status-change"

  val wsClient = app.injector.instanceOf[WSClient]

  def getAgentDetailsByArn(arn: String): WSResponse = {
    wsClient.url(s"$url/status/arn/$arn")
      .get()
      .futureValue
  }

  def getAgentDetailsByUtr(utr: String): WSResponse = {
    wsClient.url(s"$url/status/utr/$utr")
      .get()
      .futureValue
  }

  val arn = Arn("TARN0000001")

  val utr = Utr("3110118001")

  "AgentStatusChangeController" when {
    "GET /status/arn/:arn" should {
      "respond with data when inactive" in {
        givenAgencyNameArn(arn, "Bing Bong")
        val result = getAgentDetailsByArn(arn.value)
        result.status shouldBe 200
        result.json shouldBe Json.obj("agentStatus" -> Suspended("some stubbed suspension reason"),
          "agencyName" -> Some("Bing Bong"))
      }
    }
    "GET /status/utr/:utr" should {
      "respond with data when inactive" in {
        givenAgencyNameUtr(utr, "Bong Bing")
        val result = getAgentDetailsByUtr(utr.value)
        result.status shouldBe 200
        result.json shouldBe Json.obj("agentStatus" -> Suspended("some stubbed suspension reason"),
          "agencyName" -> Some("Bong Bing"))
      }
    }
  }
}
