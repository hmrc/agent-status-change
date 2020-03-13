package uk.gov.hmrc.agentstatuschange.services

import com.google.inject.Singleton
import javax.inject.Inject
import play.api.mvc.Request
import uk.gov.hmrc.agentmtdidentifiers.model.Arn
import uk.gov.hmrc.agentstatuschange.models.AgentDetails
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.DataEvent

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object AgentstatuschangeEvent extends Enumeration {
  val GetAgentDetails, TerminateMtdAgentStatusChangeRecord = Value
  type AgentstatuschangeEvent = Value
}

@Singleton
class AuditService @Inject()(val auditConnector: AuditConnector) {

  import AgentstatuschangeEvent._

  def sendGetAgentDetails(model: AgentDetails, agentReference: Arn)(
      implicit hc: HeaderCarrier,
      request: Request[Any],
      ec: ExecutionContext): Unit =
    auditEvent(
      AgentstatuschangeEvent.GetAgentDetails,
      "get-agent-details",
      Seq(
        "arn" -> agentReference.value,
        "agentStatus" -> model.agentStatus,
        "agencyName" -> model.agencyName
      )
    )

  def sendTerminateMtdAgentStatusChangeRecord(arn: Arn,
                                              status: String,
                                              credId: String,
                                              failureReason: Option[String] =
                                                None)(
      implicit hc: HeaderCarrier,
      request: Request[Any],
      ec: ExecutionContext): Future[Unit] = {

    val details = failureReason match {
      case Some(fr) =>
        Seq(
          "agentReferenceNumber" -> arn.value,
          "status" -> status,
          "credId" -> credId,
          "authProvider" -> "PrivilegedApplication",
          "failureReason" -> fr
        )
      case None =>
        Seq(
          "agentReferenceNumber" -> arn.value,
          "status" -> status,
          "credId" -> credId,
          "authProvider" -> "PrivilegedApplication"
        )
    }

    auditEvent(
      AgentstatuschangeEvent.TerminateMtdAgentStatusChangeRecord,
      "terminate-mtd-agent-status-change-record",
      details
    )
  }

  private[services] def auditEvent(event: AgentstatuschangeEvent,
                                   transactionName: String,
                                   details: Seq[(String, Any)] = Seq.empty)(
      implicit hc: HeaderCarrier,
      request: Request[Any],
      ec: ExecutionContext): Future[Unit] =
    send(createEvent(event, transactionName, details: _*))

  private[services] def createEvent(event: AgentstatuschangeEvent,
                                    transactionName: String,
                                    details: (String, Any)*)(
      implicit hc: HeaderCarrier,
      request: Request[Any]): DataEvent = {

    val detail =
      hc.toAuditDetails(details.map(pair => pair._1 -> pair._2.toString): _*)
    val tags = hc.toAuditTags(transactionName, request.path)
    DataEvent(auditSource = "agent-status-change",
              auditType = event.toString,
              tags = tags,
              detail = detail)
  }

  private[services] def send(events: DataEvent*)(
      implicit hc: HeaderCarrier,
      ec: ExecutionContext): Future[Unit] =
    Future {
      events.foreach { event =>
        Try(auditConnector.sendEvent(event))
      }
    }

}
