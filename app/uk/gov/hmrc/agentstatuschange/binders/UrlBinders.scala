package uk.gov.hmrc.agentstatuschange.binders

import uk.gov.hmrc.agentmtdidentifiers.model.{Arn, Utr}

object UrlBinders {
  implicit val utrBinder = new SimpleObjectBinder[Utr](Utr.apply, _.value)
  implicit val arnBinder = new SimpleObjectBinder[Arn](Arn.apply, _.value)

}
