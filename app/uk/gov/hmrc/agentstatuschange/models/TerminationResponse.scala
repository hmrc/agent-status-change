package uk.gov.hmrc.agentstatuschange.models

import play.api.libs.json.Json

case class DeletionCounts(service: String, store: String, count: Int)

object DeletionCounts {
  implicit val formats = Json.format[DeletionCounts]
}

case class TerminationResponse(counts: Seq[DeletionCounts])

object TerminationResponse {
  implicit val formats = Json.format[TerminationResponse]
}
