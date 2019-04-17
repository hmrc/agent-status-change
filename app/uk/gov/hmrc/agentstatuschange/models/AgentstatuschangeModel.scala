package uk.gov.hmrc.agentstatuschange.models

import play.api.libs.json.Json

case class AgentstatuschangeModel(
  parameter1: String,
  parameter2: Option[String],
  telephoneNumber: Option[String],
  emailAddress: Option[String])

object AgentstatuschangeModel {
  implicit val modelFormat = Json.format[AgentstatuschangeModel]
}