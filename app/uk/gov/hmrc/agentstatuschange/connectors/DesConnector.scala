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

import uk.gov.hmrc.agentstatuschange.UriPathEncoding.encodePathSegment
import uk.gov.hmrc.agentstatuschange.models.{Arn, ArnAndAgencyName, Utr}
import uk.gov.hmrc.agentstatuschange.wiring.AppConfig
import uk.gov.hmrc.domain.TaxIdentifier
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.UpstreamErrorResponse.Upstream4xxResponse
import uk.gov.hmrc.http.client.HttpClientV2
import java.net.URL
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

sealed trait ErrorCase

case class Unsubscribed(detail: String)
extends ErrorCase

case class Invalid(detail: String)
extends ErrorCase

class DesConnector @Inject() (
  appConfig: AppConfig,
  http: HttpClientV2
) {

  private val Environment = "Environment"
  private val CorrelationId = "CorrelationId"
  private val Authorization = "Authorization"

  private def getCorrelationIdHeader = CorrelationId -> UUID.randomUUID().toString

  def getArnAndAgencyNameFor(agentIdentifier: TaxIdentifier)(
    implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Either[ErrorCase, ArnAndAgencyName]] = {

    val url =
      agentIdentifier match {
        case _: Arn => s"/registration/personal-details/arn/${encodePathSegment(agentIdentifier.value)}"
        case _: Utr => s"/registration/personal-details/utr/${encodePathSegment(agentIdentifier.value)}"
        case x =>
          throw new Exception(
            s"Unexpected agent identifier found: '${x.value}', unable to retrieve URL"
          )
      }

    http.get(new URL(appConfig.desUrl, url))
      .setHeader(Environment -> "desEnvironment")
      .setHeader(getCorrelationIdHeader)
      .setHeader(Authorization -> s"Bearer ${appConfig.desAuthorizationToken}")
      .execute[ArnAndAgencyName]
      .map(record => Right(record))
  }.recover {
    case Upstream4xxResponse(ex) if ex.statusCode == 404 => Left(Unsubscribed("UTR_NOT_SUBSCRIBED"))
    case Upstream4xxResponse(ex) if ex.statusCode == 400 => Left(Invalid("INVALID_UTR"))
    case e => throw new Exception(s"exception: ${e.getMessage}")
  }

}
