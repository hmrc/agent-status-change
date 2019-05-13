package uk.gov.hmrc.agentstatuschange.models

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.agentmtdidentifiers.model.Arn

case class AgentDetails(arn: Arn, agentStatus: AgentStatus, agencyName: String)

object AgentDetails {
  implicit val formats: OFormat[AgentDetails] = Json.format
}
