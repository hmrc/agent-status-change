package uk.gov.hmrc.agentstatuschange.models

import play.api.libs.json.{JsResult, JsValue, Json, OFormat}
import uk.gov.hmrc.agentmtdidentifiers.model.Arn
import play.api.libs.functional.syntax._

case class ArnAndAgencyName(arn: Arn, agencyName: String)

object ArnAndAgencyName {

  implicit val arnAndAgencyNameFormat: OFormat[ArnAndAgencyName] =
    new OFormat[ArnAndAgencyName] {

      override def reads(json: JsValue): JsResult[ArnAndAgencyName] = {
        ((json \ "agentReferenceNumber").validate[Arn] and
          (json \ "agencyDetails" \ "agencyName").validate[String])(
          (arn, agencyName) => ArnAndAgencyName(arn, agencyName))
      }

      override def writes(o: ArnAndAgencyName) =
        Json.obj(
          "arn" -> o.arn.value,
          "agencyName" -> o.agencyName
        )
    }
}
