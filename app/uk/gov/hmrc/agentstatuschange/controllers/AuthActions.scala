package uk.gov.hmrc.agentstatuschange.controllers

import java.nio.charset.StandardCharsets.UTF_8
import java.util.Base64

import play.api.Logger
import play.api.mvc.Results.{Forbidden, Unauthorized}
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.agentmtdidentifiers.model.{Arn, MtdItId}
import uk.gov.hmrc.agentstatuschange.models.BasicAuthentication
import uk.gov.hmrc.auth.core.AuthProvider.{
  GovernmentGateway,
  PrivilegedApplication
}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{
  allEnrolments,
  authorisedEnrolments
}
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.matching.Regex

trait AuthActions extends AuthorisedFunctions {

  protected def withAuthorisedAsAgent[A](body: Arn => Future[Result])(
      implicit hc: HeaderCarrier,
      ec: ExecutionContext): Future[Result] =
    withEnrolledFor("HMRC-AS-AGENT", "AgentReferenceNumber") {
      case Some(arn) => body(Arn(arn))
      case None =>
        Future.failed(
          InsufficientEnrolments("AgentReferenceNumber identifier not found"))
    }

  protected def withAuthorisedAsClient[A](body: MtdItId => Future[Result])(
      implicit hc: HeaderCarrier,
      ec: ExecutionContext): Future[Result] =
    withEnrolledFor("HMRC-MTD-IT", "MTDITID") {
      case Some(mtdItID) => body(MtdItId(mtdItID))
      case None =>
        Future.failed(InsufficientEnrolments("MTDITID identifier not found"))
    }

  protected def withEnrolledFor[A](serviceName: String, identifierKey: String)(
      body: Option[String] => Future[Result])(
      implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[Result] =
    authorised(
      Enrolment(serviceName)
        and AuthProviders(GovernmentGateway))
      .retrieve(authorisedEnrolments) { enrolments =>
        val id = for {
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
    } catch { case _: Throwable => "" }

  def withBasicAuth(expectedAuth: BasicAuthentication)(body: => Future[Result])(
      implicit request: Request[_]): Future[Result] = {
    request.headers.get(HeaderNames.authorisation) match {
      case Some(basicAuthHeader(encodedAuthHeader)) =>
        decodeFromBase64(encodedAuthHeader) match {
          case decodedAuth(username, password) =>
            if (BasicAuthentication(username, password) == expectedAuth) body
            else {
              Logger.warn(
                "Authorization header found in the request but invalid username or password")
              Future successful Unauthorized
            }
          case _ =>
            Logger.warn(
              "Authorization header found in the request but its not in the expected format")
            Future successful Unauthorized
        }
      case _ =>
        Logger.warn(
          "No Authorization header found in the request for agent termination")
        Future successful Unauthorized
    }
  }

  def onlyStride(strideRole: String)(action: => Future[Result])(
      implicit hc: HeaderCarrier,
      ec: ExecutionContext): Future[Result] =
    authorised(AuthProviders(PrivilegedApplication))
      .retrieve(allEnrolments) {
        case allEnrols
            if allEnrols.enrolments.map(_.key).contains(strideRole) =>
          action
        case e =>
          Logger(getClass).warn(
            s"Unauthorized Discovered during Stride Authentication: ${e.enrolments
              .map(enrol => enrol.key)
              .mkString(",")}")
          Future successful Unauthorized
      }
      .recover {
        case e =>
          Logger(getClass).warn(
            s"Error Discovered during Stride Authentication: ${e.getMessage}")
          Forbidden
      }
}
