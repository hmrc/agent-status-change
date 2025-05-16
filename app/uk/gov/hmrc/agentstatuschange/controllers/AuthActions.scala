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

package uk.gov.hmrc.agentstatuschange.controllers

import play.api.Logging

import java.nio.charset.StandardCharsets.UTF_8
import java.util.Base64
import play.api.mvc.Results.Forbidden
import play.api.mvc.Results.Unauthorized
import play.api.mvc.Request
import play.api.mvc.Result
import uk.gov.hmrc.agentmtdidentifiers.model.Arn
import uk.gov.hmrc.agentmtdidentifiers.model.MtdItId
import uk.gov.hmrc.agentstatuschange.models.BasicAuthentication
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.allEnrolments
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.authorisedEnrolments
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HeaderNames

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.matching.Regex

trait AuthActions
extends AuthorisedFunctions
with Logging {

  protected def withAuthorisedAsAgent[A](body: Arn => Future[Result])(
    implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Result] =
    withEnrolledFor("HMRC-AS-AGENT", "AgentReferenceNumber") {
      case Some(arn) => body(Arn(arn))
      case None =>
        Future.failed(
          InsufficientEnrolments("AgentReferenceNumber identifier not found")
        )
    }

  protected def withAuthorisedAsClient[A](body: MtdItId => Future[Result])(
    implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Result] =
    withEnrolledFor("HMRC-MTD-IT", "MTDITID") {
      case Some(mtdItID) => body(MtdItId(mtdItID))
      case None => Future.failed(InsufficientEnrolments("MTDITID identifier not found"))
    }

  protected def withEnrolledFor[A](
    serviceName: String,
    identifierKey: String
  )(
    body: Option[String] => Future[Result]
  )(
    implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Result] =
    authorised(
      Enrolment(serviceName)
        and AuthProviders(GovernmentGateway)
    )
      .retrieve(authorisedEnrolments) { enrolments =>
        val id =
          for {
            enrolment <- enrolments.getEnrolment(serviceName)
            identifier <- enrolment.getIdentifier(identifierKey)
          } yield identifier.value

        body(id)
      }

  val basicAuthHeader: Regex = "Basic (.+)".r
  val decodedAuth: Regex = "(.+):(.+)".r

  private def decodeFromBase64(encodedString: String): String =
    try {
      new String(Base64.getDecoder.decode(encodedString), UTF_8)
    }
    catch { case _: Throwable => "" }

  def withBasicAuth(expectedAuth: BasicAuthentication)(body: => Future[Result])(
    implicit request: Request[_]
  ): Future[Result] = {
    request.headers.get(HeaderNames.authorisation) match {
      case Some(basicAuthHeader(encodedAuthHeader)) =>
        decodeFromBase64(encodedAuthHeader) match {
          case decodedAuth(username, password) =>
            if (BasicAuthentication(username, password) == expectedAuth)
              body
            else {
              logger.warn(
                "Authorization header found in the request but invalid username or password"
              )
              Future successful Unauthorized
            }
          case _ =>
            logger.warn(
              "Authorization header found in the request but its not in the expected format"
            )
            Future successful Unauthorized
        }
      case _ =>
        logger.warn(
          "No Authorization header found in the request for agent termination"
        )
        Future successful Unauthorized
    }
  }

  def onlyStride(strideRole: String)(action: => Future[Result])(
    implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Result] = authorised(AuthProviders(PrivilegedApplication))
    .retrieve(allEnrolments) {
      case allEnrols
          if allEnrols.enrolments.map(_.key).contains(strideRole) =>
        action
      case e =>
        logger.warn(
          s"Unauthorized Discovered during Stride Authentication: ${e.enrolments
              .map(enrol => enrol.key)
              .mkString(",")}"
        )
        Future successful Unauthorized
    }
    .recover {
      case e =>
        logger.warn(
          s"Error Discovered during Stride Authentication: ${e.getMessage}"
        )
        Forbidden
    }

}
