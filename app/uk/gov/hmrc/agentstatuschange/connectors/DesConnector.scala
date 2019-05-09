package uk.gov.hmrc.agentstatuschange.connectors

import java.net.URL

import com.codahale.metrics.MetricRegistry
import com.kenshoo.play.metrics.Metrics
import javax.inject.Inject
import uk.gov.hmrc.agent.kenshoo.monitoring.HttpAPIMonitor
import uk.gov.hmrc.agentmtdidentifiers.model.Utr
import uk.gov.hmrc.agentstatuschange.UriPathEncoding.encodePathSegment
import uk.gov.hmrc.agentstatuschange.models.ArnAndAgencyName
import uk.gov.hmrc.agentstatuschange.wiring.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet, HttpPost}

import scala.concurrent.{ExecutionContext, Future}

class DesConnector @Inject()(appConfig: AppConfig,
                             http: HttpPost with HttpGet,
                             metrics: Metrics)
    extends HttpAPIMonitor {

  override val kenshooRegistry: MetricRegistry = metrics.defaultRegistry

  def getArnAndAgencyNameFor(utr: Utr)(
      implicit hc: HeaderCarrier,
      ec: ExecutionContext): Future[ArnAndAgencyName] = {
    monitor(s"ConsumedAPI-Get-Arn-GET") {
      http
        .GET[ArnAndAgencyName](new URL(
          appConfig.desUrl,
          s"/registration/personal-details/utr/${encodePathSegment(utr.value)}").toString)
    }.recoverWith {
      case _ => throw new Exception("no Arn for user")
    }
  }
}
