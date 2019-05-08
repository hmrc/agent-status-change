package uk.gov.hmrc.agentstatuschange.models

import play.api.libs.json.{Json, OFormat}

case class AgentDetails(agentStatus: AgentStatus, agencyName: Option[String])

object AgentDetails {
  implicit val formats: OFormat[AgentDetails] = Json.format[AgentDetails]
}
