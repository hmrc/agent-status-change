/*
 * Copyright 2019 HM Revenue & Customs
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

import java.net.URL

import com.codahale.metrics.MetricRegistry
import com.kenshoo.play.metrics.Metrics
import javax.inject.{Inject, Singleton}
import play.api.libs.json._
import uk.gov.hmrc.agent.kenshoo.monitoring.HttpAPIMonitor
import uk.gov.hmrc.agentmtdidentifiers.model.{Arn, Utr}
import uk.gov.hmrc.agentstatuschange.UriPathEncoding.encodePathSegment
import uk.gov.hmrc.agentstatuschange.wiring.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet, HttpPost, NotFoundException}

import scala.concurrent.{ExecutionContext, Future}

case class AgencyName(name: Option[String])

case class AgencyNameNotFound() extends Exception

object AgencyName {
  implicit val nameReads: Reads[AgencyName] =
    (JsPath \ "agencyName").readNullable[String].map(AgencyName(_))
}

@Singleton
class AgentServicesAccountConnector @Inject()(appConfig: AppConfig,
                                              http: HttpPost with HttpGet,
                                              metrics: Metrics)
    extends HttpAPIMonitor {

  override val kenshooRegistry: MetricRegistry = metrics.defaultRegistry

  def getAgencyNameByArn(arn: Arn)(
      implicit hc: HeaderCarrier,
      ec: ExecutionContext): Future[Option[String]] =
    monitor(s"ConsumedAPI-Get-AgencyNameByArn-GET") {
      http
        .GET[AgencyName](
          new URL(
            appConfig.agentServicesAccountUrl,
            s"/agent-services-account/client/agency-name/${encodePathSegment(
              arn.value)}").toString)
        .map(_.name)
    } recoverWith {
      case _: NotFoundException => Future successful None
    }

  def getAgencyNameByUtr(utr: Utr)(
      implicit hc: HeaderCarrier,
      ec: ExecutionContext): Future[Option[String]] =
    monitor(s"ConsumedAPI-Get-AgencyNameByUtr-GET") {
      http
        .GET[AgencyName](new URL(
          appConfig.agentServicesAccountUrl,
          s"/agent-services-account/client/agency-name/utr/${encodePathSegment(
            utr.value)}").toString)
        .map(_.name)
    } recoverWith {
      case _: NotFoundException => Future successful None
    }

}
