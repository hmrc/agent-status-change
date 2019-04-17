package uk.gov.hmrc.agentstatuschange.services

import javax.inject.Inject

import com.google.inject.Singleton
import play.api.mvc.Request
import uk.gov.hmrc.agentmtdidentifiers.model.Arn
import uk.gov.hmrc.agentstatuschange.models.AgentstatuschangeModel
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.DataEvent
import scala.concurrent.ExecutionContext

import scala.concurrent.Future
import scala.util.Try

object AgentstatuschangeEvent extends Enumeration {
  val AgentstatuschangeSomethingHappened = Value
  type AgentstatuschangeEvent = Value
}

@Singleton
class AuditService @Inject() (val auditConnector: AuditConnector) {

  import AgentstatuschangeEvent._

  def sendAgentstatuschangeSomethingHappened(model: AgentstatuschangeModel, agentReference: Arn)(implicit hc: HeaderCarrier, request: Request[Any], ec: ExecutionContext): Unit = {

    auditEvent(AgentstatuschangeEvent.AgentstatuschangeSomethingHappened, "agent-status-change-something-happened",
      Seq(
        "agentReference" -> agentReference.value,
        "parameter1" -> model.parameter1,
        "telephoneNumber" -> model.telephoneNumber.getOrElse(""),
        "emailAddress" -> model.emailAddress.getOrElse("")))
  }

  private[services] def auditEvent(event: AgentstatuschangeEvent, transactionName: String, details: Seq[(String, Any)] = Seq.empty)(implicit hc: HeaderCarrier, request: Request[Any], ec: ExecutionContext): Future[Unit] = {
    send(createEvent(event, transactionName, details: _*))
  }

  private[services] def createEvent(event: AgentstatuschangeEvent, transactionName: String, details: (String, Any)*)(implicit hc: HeaderCarrier, request: Request[Any], ec: ExecutionContext): DataEvent = {

    val detail = hc.toAuditDetails(details.map(pair => pair._1 -> pair._2.toString): _*)
    val tags = hc.toAuditTags(transactionName, request.path)
    DataEvent(
      auditSource = "agent-status-change",
      auditType = event.toString,
      tags = tags,
      detail = detail)
  }

  private[services] def send(events: DataEvent*)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    Future {
      events.foreach { event =>
        Try(auditConnector.sendEvent(event))
      }
    }
  }

}
