package uk.gov.hmrc.agentstatuschange.controllers

import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson
import play.api.mvc._
import play.api.{Configuration, Logger}
import uk.gov.hmrc.agentmtdidentifiers.model.{Arn, Utr}
import uk.gov.hmrc.agentstatuschange.connectors.{
  AgentConnector,
  DesConnector,
  Invalid,
  Unsubscribed
}
import uk.gov.hmrc.agentstatuschange.models._
import uk.gov.hmrc.agentstatuschange.services.{
  AgentStatusChangeMongoService,
  AuditService
}
import uk.gov.hmrc.agentstatuschange.wiring.AppConfig
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.domain.TaxIdentifier
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AgentStatusChangeController @Inject()(
    override val authConnector: AuthConnector,
    auditService: AuditService,
    agentConnector: AgentConnector,
    desConnector: DesConnector,
    agentStatusChangeMongoService: AgentStatusChangeMongoService,
    cc: ControllerComponents,
    appConfig: AppConfig,
    config: Configuration)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with AuthActions {

  import agentStatusChangeMongoService._
  import desConnector._

  val configStubStatus = config.getOptional[String]("test.stubbed.status")
  val stubStatus = configStubStatus.getOrElse("Active")
  Logger.info(
    s"test.stubbed.status config value is $configStubStatus, so agent status will be $stubStatus")
  val stubbedStatus: AgentStatus = stubStatus match {
    case "Active" => Active
    case "Suspended" =>
      Suspended(Reason(Some("some stubbed suspension reason")))
    case "Deactivated" =>
      Deactivated(Reason(Some("some stubbed deactivation reason")))
  }

  def getAgentDetailsByArn(arn: Arn): Action[AnyContent] = Action.async {
    implicit request =>
      getAgentDetails(arn)
  }

  def getAgentDetailsByUtr(utr: Utr): Action[AnyContent] = Action.async {
    implicit request =>
      getAgentDetails(utr)
  }

  def getAgentDetails[T <: TaxIdentifier](agentId: T)(
      implicit hc: HeaderCarrier) = {
    for {
      arnAndAgencyName <- getArnAndAgencyNameFor(agentId)
      result <- arnAndAgencyName match {
        case Right(arnAndName) =>
          for {
            recordOpt <- findCurrentRecordByArn(arnAndName.arn.value)
            statusToReturn <- recordOpt match {
              case Some(record) => Future successful record.status
              case None =>
                for {
                  _ <- agentStatusChangeMongoService.createRecord(
                    AgentStatusChangeRecord(arnAndName.arn,
                                            stubbedStatus,
                                            DateTime.now()))
                } yield stubbedStatus
            }
          } yield
            Ok(
              toJson(
                AgentDetails(arnAndName.arn,
                             statusToReturn,
                             arnAndName.agencyName)))
        case Left(err) =>
          err match {
            case Unsubscribed(detail) =>
              Future successful NotFound(toJson(detail))
            case Invalid(detail) =>
              Future successful BadRequest(toJson(detail))
          }
      }
    } yield result
  }

  def changeStatus(arn: Arn): Action[JsValue] =
    Action.async(parse.json) { implicit request =>
      withJsonBody[Reason] { reason =>
        reason.reason match {
          case Some(_) =>
            for {
              _ <- agentStatusChangeMongoService.createRecord(
                AgentStatusChangeRecord(arn, Suspended(reason), DateTime.now()))
            } yield Ok
          case None =>
            for {
              _ <- agentStatusChangeMongoService.createRecord(
                AgentStatusChangeRecord(arn, Active, DateTime.now()))
            } yield Ok
        }
      }
    }

  def removeAgentRecords(arn: Arn): Action[AnyContent] = Action.async {
    implicit request =>
      withBasicAuth(appConfig.expectedAuth) {
        if (Arn.isValid(arn.value)) {

          val terminationCalls: Seq[
            Future[Either[TerminationErrorResponse, TerminationResponse]]] =
            Seq(
              agentConnector.removeAgentInvitations(arn),
              agentConnector.removeAFIRelationship(arn),
              agentConnector.removeAgentMapping(arn),
              agentConnector.removeAgentClientRelationships(arn)
            )

          val terminationResponses: Future[
            Seq[Either[TerminationErrorResponse, TerminationResponse]]] =
            Future.sequence(terminationCalls)

          for {
            responses <- terminationResponses
            counts = responses
              .filter(_.isRight)
              .map(_.right.get.counts)
              .flatten
            errors = responses.filter(_.isLeft).map(_.left.get)
            maybeErrors = if (errors.isEmpty) None else Some(errors)
          } yield {
            auditService.sendTerminateMtdAgent(arn,
                                               counts,
                                               appConfig.expectedAuth.username,
                                               maybeErrors)
            if (errors.isEmpty) {
              Ok
            } else {
              InternalServerError
            }
          }
        } else
          Future successful BadRequest
      }
  }

}
