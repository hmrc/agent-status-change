package uk.gov.hmrc.agentstatuschange.services

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.Eventually
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.time.{Millis, Span}
import play.api.test.FakeRequest
import uk.gov.hmrc.agentmtdidentifiers.model.Arn
import uk.gov.hmrc.agentstatuschange.models.{Active, AgentDetails, AgentStatus}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.{Authorization, RequestId, SessionId}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.DataEvent
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext

class AuditServiceSpec extends UnitSpec with MockitoSugar with Eventually {

  override implicit val patienceConfig =
    PatienceConfig(timeout = scaled(Span(500, Millis)),
                   interval = scaled(Span(200, Millis)))

  "auditService" should {

    "send an GetAgentDetails event with the correct fields" in {
      val mockConnector = mock[AuditConnector]
      val service = new AuditService(mockConnector)

      val hc = HeaderCarrier(authorization =
                               Some(Authorization("dummy bearer token")),
                             sessionId = Some(SessionId("dummy session id")),
                             requestId = Some(RequestId("dummy request id")))

      val model = AgentDetails(Active, Some("John Smith"))

      await(
        service.sendGetAgentDetails(model, Arn("ARN0001"))(
          hc,
          FakeRequest("GET", "/path"),
          ExecutionContext.global))

      eventually {
        val captor = ArgumentCaptor.forClass(classOf[DataEvent])
        verify(mockConnector).sendEvent(captor.capture())(any[HeaderCarrier],
                                                          any[ExecutionContext])
        val sentEvent = captor.getValue.asInstanceOf[DataEvent]

        sentEvent.auditType shouldBe "GetAgentDetails"
        sentEvent.auditSource shouldBe "agent-status-change"
        sentEvent.detail("arn") shouldBe "ARN0001"
        sentEvent.detail("agentStatus") shouldBe "Active"
        sentEvent.detail("agencyName") shouldBe "John Smith"

        sentEvent.tags("transactionName") shouldBe "get-agent-details"
        sentEvent.tags("path") shouldBe "/path"
        sentEvent.tags("X-Session-ID") shouldBe "dummy session id"
        sentEvent.tags("X-Request-ID") shouldBe "dummy request id"
      }
    }
  }
}
