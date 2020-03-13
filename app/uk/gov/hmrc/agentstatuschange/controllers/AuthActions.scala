package uk.gov.hmrc.agentstatuschange.controllers

import play.api.Logger
import play.api.mvc.Result
import play.api.mvc.Results.{Forbidden, Unauthorized}
import uk.gov.hmrc.agentmtdidentifiers.model.{Arn, MtdItId}
import uk.gov.hmrc.auth.core.AuthProvider.{
  GovernmentGateway,
  PrivilegedApplication
}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{
  allEnrolments,
  authorisedEnrolments,
  credentials
}
import uk.gov.hmrc.auth.core.retrieve.{Credentials, ~}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

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

  def onlyStride(strideRole: String)(action: => Credentials => Future[Result])(
      implicit hc: HeaderCarrier,
      ec: ExecutionContext): Future[Result] =
    authorised(AuthProviders(PrivilegedApplication))
      .retrieve(allEnrolments and credentials) {
        case allEnrols ~ Some(creds)
            if allEnrols.enrolments.map(_.key).contains(strideRole) =>
          action(creds)
        case e ~ _ =>
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
