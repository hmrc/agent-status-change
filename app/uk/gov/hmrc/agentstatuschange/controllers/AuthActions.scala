package uk.gov.hmrc.agentstatuschange.controllers

import com.kenshoo.play.metrics.Metrics
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.mvc._
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
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthActions @Inject()(metrics: Metrics,
                            val authConnector: AuthConnector,
                            cc: ControllerComponents)
    extends BackendController(cc)
    with AuthorisedFunctions {

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

  protected type RequestWithCreds =
    Request[AnyContent] => Credentials => Future[Result]

  def onlyStride(strideRole: String)(body: RequestWithCreds)(
      implicit ec: ExecutionContext): Action[AnyContent] =
    Action.async { implicit request =>
      authorised(AuthProviders(PrivilegedApplication))
        .retrieve(allEnrolments and credentials) {
          case allEnrols ~ Some(creds)
              if allEnrols.enrolments.map(_.key).contains(strideRole) =>
            body(request)(creds)
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

}
