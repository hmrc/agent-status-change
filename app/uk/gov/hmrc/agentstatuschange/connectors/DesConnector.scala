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

import uk.gov.hmrc.agentmtdidentifiers.model.Arn
import uk.gov.hmrc.agentmtdidentifiers.model.Utr
import uk.gov.hmrc.agentstatuschange.UriPathEncoding.encodePathSegment
import uk.gov.hmrc.agentstatuschange.models.ArnAndAgencyName
import uk.gov.hmrc.agentstatuschange.wiring.AppConfig
import uk.gov.hmrc.domain.TaxIdentifier
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpReads
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.UpstreamErrorResponse.Upstream4xxResponse
import uk.gov.hmrc.http.client.HttpClientV2

import java.net.URL
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

sealed trait ErrorCase

case class Unsubscribed(detail: String)
extends ErrorCase

case class Invalid(detail: String)
extends ErrorCase

class DesConnector @Inject() (
  appConfig: AppConfig,
  http: HttpClientV2
) {

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

    getWithDesHeaders[ArnAndAgencyName](new URL(appConfig.desUrl, url))
      .map(record => Right(record))
  }.recover {
    case Upstream4xxResponse(ex) if ex.statusCode == 404 => Left(Unsubscribed("UTR_NOT_SUBSCRIBED"))
    case Upstream4xxResponse(ex) if ex.statusCode == 400 => Left(Invalid("INVALID_UTR"))
    case e => throw new Exception(s"exception: ${e.getMessage}")
  }

  private val Environment = "Environment"
  private val CorrelationId = "CorrelationId"
  private val Authorization = "Authorization"

  private def outboundHeaders = Seq(
    Environment -> "desEnvironment",
    CorrelationId -> UUID.randomUUID().toString,
    Authorization -> s"Bearer ${appConfig.desAuthorizationToken}"
  )

  private def getWithDesHeaders[A: HttpReads](
    url: URL
  )(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[A] = {
    http
      .get(url)
      .setHeader(outboundHeaders: _*)
      .execute[A]
  }

}
