package uk.gov.hmrc.agentstatuschange.connectors

import java.net.URL

import com.codahale.metrics.MetricRegistry
import com.kenshoo.play.metrics.Metrics
import javax.inject.Inject
import uk.gov.hmrc.agent.kenshoo.monitoring.HttpAPIMonitor
import uk.gov.hmrc.agentmtdidentifiers.model.{Arn, Utr}
import uk.gov.hmrc.agentstatuschange.UriPathEncoding.encodePathSegment
import uk.gov.hmrc.agentstatuschange.models.ArnAndAgencyName
import uk.gov.hmrc.agentstatuschange.wiring.AppConfig
import uk.gov.hmrc.domain.TaxIdentifier
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet, HttpPost, _}

import scala.concurrent.{ExecutionContext, Future}

sealed trait ErrorCase
case class Unsubscribed(detail: String) extends ErrorCase
case class Invalid(detail: String) extends ErrorCase

class DesConnector @Inject()(appConfig: AppConfig,
                             http: HttpPost with HttpGet,
                             metrics: Metrics)
    extends HttpAPIMonitor {

  override val kenshooRegistry: MetricRegistry = metrics.defaultRegistry

  def getArnAndAgencyNameFor(agentIdentifier: TaxIdentifier)(
      implicit hc: HeaderCarrier,
      ec: ExecutionContext): Future[Either[ErrorCase, ArnAndAgencyName]] = {

    val url = agentIdentifier match {
      case _: Arn =>
        s"/registration/personal-details/arn/${encodePathSegment(agentIdentifier.value)}"
      case _: Utr =>
        s"/registration/personal-details/utr/${encodePathSegment(agentIdentifier.value)}"
    }

    monitor(s"ConsumedAPI-GetArnAndAgencyName-GET") {
      http
        .GET[ArnAndAgencyName](new URL(appConfig.desUrl, url).toString)
        .map(record => Right(record))
    }.recover {
      case _: NotFoundException   => Left(Unsubscribed("UTR_NOT_SUBSCRIBED"))
      case _: BadRequestException => Left(Invalid("INVALID_UTR"))
      case e                      => throw new Exception(s"exception: ${e.getMessage}")
    }
  }
}
