package uk.gov.hmrc.agentstatuschange.controllers

import org.scalatest.Suite
import org.scalatestplus.play.ServerProvider
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.agentstatuschange.support.ServerBaseISpec

class AgentStatusChangeControllerISpec extends ServerBaseISpec {

  this: Suite with ServerProvider =>

  val url = s"http://localhost:$port/agent-status-change"

  val wsClient = app.injector.instanceOf[WSClient]

  def entity(): WSResponse = {
    wsClient.url(s"$url/entities")
      .get()
      .futureValue
  }

  "AgentstatuschangeController" when {

    "GET /entities" should {
      "respond with some data" in {
        val result = entity()
        result.status shouldBe 200
        result.json shouldBe Json.obj("parameter1" -> "hello world")
      }
    }
  }
}
