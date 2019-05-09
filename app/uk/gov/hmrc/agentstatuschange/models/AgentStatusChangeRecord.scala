package uk.gov.hmrc.agentstatuschange.models

import org.joda.time.DateTime
import play.api.libs.json.Json
import uk.gov.hmrc.agentmtdidentifiers.model.Arn
import uk.gov.hmrc.http.controllers.RestFormats
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

case class AgentStatusChangeRecord(arn: Arn,
                                   status: AgentStatus,
                                   lastUpdated: DateTime)

object AgentStatusChangeRecord {

  implicit val dateWrites = RestFormats.dateTimeWrite
  implicit val dateReads = RestFormats.dateTimeRead
  implicit val oidFormats = ReactiveMongoFormats.objectIdFormats

  implicit val agentStatusChangeRecordFormat =
    Json.format[AgentStatusChangeRecord]
}
