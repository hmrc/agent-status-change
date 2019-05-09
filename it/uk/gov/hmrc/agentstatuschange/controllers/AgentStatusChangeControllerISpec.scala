package uk.gov.hmrc.agentstatuschange.controllers

import org.scalatest.Suite
import org.scalatestplus.play.ServerProvider
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.agentmtdidentifiers.model.{Arn, Utr}
import uk.gov.hmrc.agentstatuschange.models.Active
import uk.gov.hmrc.agentstatuschange.stubs.{AgentServicesAccountStub, DesStubs}
import uk.gov.hmrc.agentstatuschange.support.{DualSuite, MongoApp, ServerBaseISpec}

class AgentStatusChangeControllerISpec extends ServerBaseISpec with AgentServicesAccountStub with MongoApp with DesStubs {

  this: Suite with ServerProvider with DualSuite =>

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
      "respond with data when active" in {
        givenAgencyNameArn(arn, "Bing Bong")
        val result = getAgentDetailsByArn(arn.value)
        result.status shouldBe 200
        result.json shouldBe Json.obj("agentStatus" -> Active,
          "agencyName" -> Some("Bing Bong"))
      }
    }
    "GET /status/utr/:utr" should {
      "respond with data when active" in {
        givenBusinessPartnerRecordExistsFor(utr, arn, "Bong Bing")
        val result = getAgentDetailsByUtr(utr.value)
        result.status shouldBe 200
        result.json shouldBe Json.obj("agentStatus" -> Active,
          "agencyName" -> Some("Bong Bing"))
      }
    }
  }
}
