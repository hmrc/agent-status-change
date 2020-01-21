package uk.gov.hmrc.agentstatuschange.connectors

import com.codahale.metrics.MetricRegistry
import com.kenshoo.play.metrics.Metrics
import javax.inject.{Inject, Singleton}
import play.api.Logger
import uk.gov.hmrc.agent.kenshoo.monitoring.HttpAPIMonitor
import uk.gov.hmrc.agentmtdidentifiers.model.Arn
import uk.gov.hmrc.agentstatuschange.wiring.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpDelete}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AgentConnector @Inject()(appConfig: AppConfig,
                               http: HttpDelete,
                               metrics: Metrics)
    extends HttpAPIMonitor {

  import appConfig.{
    agentClientAuthorisationBaseUrl,
    agentMappingBaseUrl,
    agentFiRelationshipBaseUrl
  }

  override val kenshooRegistry: MetricRegistry = metrics.defaultRegistry

  def acaTerminateUrl(arn: Arn): String =
    s"$agentClientAuthorisationBaseUrl/agent-client-authorisation/agent/${arn.value}/terminate"

  def afiTerminateUrl(arn: Arn): String =
    s"$agentFiRelationshipBaseUrl/agent-fi-relationship/agent/${arn.value}/terminate"

  def aMTerminateUrl(arn: Arn): String =
    s"$agentMappingBaseUrl/agent-mapping/agent/${arn.value}/terminate"

  def removeAgentInvitations(arn: Arn)(
      implicit hc: HeaderCarrier,
      ec: ExecutionContext): Future[Boolean] = {
    http.DELETE(acaTerminateUrl(arn)).map { r =>
      r.status match {
        case 200 => true
        case status =>
          Logger(getClass).warn(s"Termination for Agent-Client-Authorisation returned: $status")
          false
      }
    }
  }

  def removeAFIRelationship(arn: Arn)(implicit hc: HeaderCarrier,
                                      ec: ExecutionContext): Future[Boolean] = {
    http.DELETE(afiTerminateUrl(arn)).map { r =>
      r.status match {
        case 200 => true
        case status =>
          Logger(getClass).warn(s"Termination for Agent-Fi-Relationship returned: $status")
          false
      }
    }
  }

  def removeAgentMapping(arn: Arn)(implicit hc: HeaderCarrier,
                                   ec: ExecutionContext): Future[Boolean] = {
    http.DELETE(aMTerminateUrl(arn)).map { r =>
      r.status match {
        case 200 => true
        case status =>
          Logger(getClass).warn(s"Termination for Agent-Mapping returned: $status")
          false
      }
    }
  }
}
