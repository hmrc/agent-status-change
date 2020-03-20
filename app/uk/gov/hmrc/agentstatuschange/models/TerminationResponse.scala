package uk.gov.hmrc.agentstatuschange.models

import play.api.libs.json.Json

case class DeletionCount(service: String, store: String, count: Int)

object DeletionCount {
  implicit val formats = Json.format[DeletionCount]
}

case class TerminationResponse(counts: Seq[DeletionCount])

object TerminationResponse {
  implicit val formats = Json.format[TerminationResponse]
}

case class TerminationErrorResponse(service: String, reason: String)

object TerminationErrorResponse {
  implicit val formats = Json.format[TerminationErrorResponse]
}
