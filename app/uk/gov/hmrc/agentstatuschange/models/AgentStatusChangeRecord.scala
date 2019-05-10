package uk.gov.hmrc.agentstatuschange.models

import org.joda.time.DateTime
import play.api.libs.json._
import uk.gov.hmrc.agentmtdidentifiers.model.Arn
import uk.gov.hmrc.http.controllers.RestFormats

case class AgentStatusChangeRecord(arn: Arn,
                                   status: AgentStatus,
                                   lastUpdated: DateTime)

object AgentStatusChangeRecord {
  implicit val dateWrites = RestFormats.dateTimeWrite
  implicit val dateReads = RestFormats.dateTimeRead

  implicit val format =
    Json.format[AgentStatusChangeRecord]
}
