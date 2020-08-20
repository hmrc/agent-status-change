/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.agentstatuschange.services

import com.google.inject.Singleton
import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.Request
import uk.gov.hmrc.agentmtdidentifiers.model.Arn
import uk.gov.hmrc.agentstatuschange.models.{
  AgentDetails,
  DeletionCount,
  TerminationErrorResponse
}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.DataEvent

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object AgentstatuschangeEvent extends Enumeration {
  val GetAgentDetails, TerminateMtdAgent = Value
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

  def sendTerminateMtdAgent(arn: Arn,
                            counts: Seq[DeletionCount],
                            basicAuthUsername: String,
                            failures: Option[Seq[TerminationErrorResponse]] =
                              None)(implicit hc: HeaderCarrier,
                                    request: Request[Any],
                                    ec: ExecutionContext): Future[Unit] = {

    val details = failures match {
      case Some(fs) =>
        Seq(
          "agentReferenceNumber" -> arn.value,
          "status" -> "Failed",
          "counts" -> Json.prettyPrint(Json.toJson(counts)),
          "basicAuthUsername" -> basicAuthUsername,
          "failures" -> Json.prettyPrint(Json.toJson(fs))
        )
      case None =>
        Seq(
          "agentReferenceNumber" -> arn.value,
          "status" -> "Success",
          "counts" -> Json.prettyPrint(Json.toJson(counts)),
          "basicAuthUsername" -> basicAuthUsername
        )
    }

    auditEvent(
      AgentstatuschangeEvent.TerminateMtdAgent,
      "terminate-mtd-agent",
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
