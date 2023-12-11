/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.agentstatuschange.connectors

import com.codahale.metrics.MetricRegistry
import com.kenshoo.play.metrics.Metrics

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.json.Json
import uk.gov.hmrc.agent.kenshoo.monitoring.HttpAPIMonitor
import uk.gov.hmrc.agentmtdidentifiers.model.Arn
import uk.gov.hmrc.agentstatuschange.models.{
  TerminationErrorResponse,
  TerminationResponse
}
import uk.gov.hmrc.agentstatuschange.wiring.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.http.HttpReads.Implicits._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AgentConnector @Inject()(appConfig: AppConfig,
                               http: HttpClient,
                               metrics: Metrics)
    extends HttpAPIMonitor {

  import appConfig.{
    agentClientAuthorisationBaseUrl,
    agentClientRelationshipsBaseUrl,
    agentFiRelationshipBaseUrl,
    agentMappingBaseUrl
  }

  override val kenshooRegistry: MetricRegistry = metrics.defaultRegistry

  def acaTerminateUrl(arn: Arn): String =
    s"$agentClientAuthorisationBaseUrl/agent-client-authorisation/agent/${arn.value}/terminate"

  def afiTerminateUrl(arn: Arn): String =
    s"$agentFiRelationshipBaseUrl/agent-fi-relationship/agent/${arn.value}/terminate"

  def aMTerminateUrl(arn: Arn): String =
    s"$agentMappingBaseUrl/agent-mapping/agent/${arn.value}/terminate"

  def acrTerminateUrl(arn: Arn): String =
    s"$agentClientRelationshipsBaseUrl/agent-client-relationships/agent/${arn.value}/terminate"

  def removeAgentInvitations(arn: Arn)(implicit hc: HeaderCarrier,
                                       ec: ExecutionContext)
    : Future[Either[TerminationErrorResponse, TerminationResponse]] = {
    http
      .DELETE[HttpResponse](acaTerminateUrl(arn))
      .map { r =>
        r.status match {
          case 200 =>
            Logger(getClass).debug(
              s"Agent ${arn.value} Terminated for Agent-Client-Authorisation")
            Right(Json.parse(r.body).as[TerminationResponse])
          case x =>
            Logger(getClass).warn(
              s"Termination for agent-client-authorisation for ${arn.value} returned: $x, ${r.body}")
            Left(
              TerminationErrorResponse("agent-client-authorisation",
                                       s"Unexpected HTTP Response: $x"))
        }
      }
      .recover {
        case e =>
          Logger(getClass).warn(
            s"Termination for agent-client-authorisation for ${arn.value} returned: ${e.getMessage}")
          Left(
            TerminationErrorResponse("agent-client-authorisation",
                                     e.getMessage))
      }
  }

  def removeAFIRelationship(arn: Arn)(implicit hc: HeaderCarrier,
                                      ec: ExecutionContext)
    : Future[Either[TerminationErrorResponse, TerminationResponse]] = {
    http
      .DELETE[HttpResponse](afiTerminateUrl(arn))
      .map { r =>
        r.status match {
          case 200 =>
            Logger(getClass).debug(
              s"Agent ${arn.value} Terminated for Agent-Fi-Relationship ${arn.value}")
            Right(Json.parse(r.body).as[TerminationResponse])
          case x =>
            Logger(getClass).warn(
              s"Termination for agent-fi-relationship for ${arn.value} returned: $x, ${r.body}")
            Left(
              TerminationErrorResponse("agent-fi-relationship",
                                       s"Unexpected HTTP Response: $x"))
        }
      }
      .recover {
        case e =>
          Logger(getClass).warn(
            s"Termination for agent-fi-relationship for ${arn.value} returned: ${e.getMessage}")
          Left(TerminationErrorResponse("agent-fi-relationship", e.getMessage))
      }
  }

  def removeAgentMapping(arn: Arn)(implicit hc: HeaderCarrier,
                                   ec: ExecutionContext)
    : Future[Either[TerminationErrorResponse, TerminationResponse]] = {
    http
      .DELETE[HttpResponse](aMTerminateUrl(arn))
      .map { r =>
        r.status match {
          case 200 =>
            Logger(getClass).debug(
              s"Agent ${arn.value} Terminated for Agent-Mapping")
            Right(Json.parse(r.body).as[TerminationResponse])
          case x =>
            Logger(getClass).warn(
              s"Termination for agent-mapping for ${arn.value} returned: $x, ${r.body}")
            Left(
              TerminationErrorResponse("agent-mapping",
                                       s"Unexpected HTTP Response: $x"))
        }
      }
      .recover {
        case e =>
          Logger(getClass).warn(
            s"Termination for agent-mapping for ${arn.value} returned: ${e.getMessage}")
          Left(TerminationErrorResponse("agent-mapping", e.getMessage))
      }
  }

  def removeAgentClientRelationships(arn: Arn)(implicit hc: HeaderCarrier,
                                               ec: ExecutionContext)
    : Future[Either[TerminationErrorResponse, TerminationResponse]] = {
    http
      .DELETE[HttpResponse](acrTerminateUrl(arn))
      .map { r =>
        r.status match {
          case 200 =>
            Logger(getClass).debug(
              s"Agent ${arn.value} Terminated for Agent-Client-Relationships")
            Right(Json.parse(r.body).as[TerminationResponse])
          case x =>
            Logger(getClass).warn(
              s"Termination for agent-client-relationships for ${arn.value} returned: $x, ${r.body}")
            Left(
              TerminationErrorResponse("agent-client-relationships",
                                       s"Unexpected HTTP Response: $x"))
        }
      }
      .recover {
        case e =>
          Logger(getClass).warn(
            s"Termination for agent-client-relationships for ${arn.value} returned: ${e.getMessage}")
          Left(
            TerminationErrorResponse("agent-client-relationships",
                                     e.getMessage))
      }
  }
}
