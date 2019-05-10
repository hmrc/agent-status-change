package uk.gov.hmrc.agentstatuschange.models

import play.api.libs.json.{Json, OFormat}

case class Reason(reason: Option[String], extraDetails: Option[String] = None)

object Reason {
  implicit val format: OFormat[Reason] = Json.format
}
