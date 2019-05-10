package uk.gov.hmrc.agentstatuschange.controllers

import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson
import play.api.mvc._
import play.api.{Configuration, Logger}
import uk.gov.hmrc.agentmtdidentifiers.model.{Arn, Utr}
import uk.gov.hmrc.agentstatuschange.connectors.{
  AgentServicesAccountConnector,
  DesConnector
}
import uk.gov.hmrc.agentstatuschange.models._
import uk.gov.hmrc.agentstatuschange.services.AgentStatusChangeMongoService
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AgentStatusChangeController @Inject()(
    override val authConnector: AuthConnector,
    agentServicesAccountConnector: AgentServicesAccountConnector,
    desConnector: DesConnector,
    agentStatusChangeMongoService: AgentStatusChangeMongoService,
    cc: ControllerComponents,
    config: Configuration)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with AuthActions {

  import agentServicesAccountConnector._
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
      for {
        agencyName <- getAgencyNameByArn(arn)
        recordOpt <- findCurrentRecordByArn(arn.value)
        statusToReturn <- recordOpt match {
          case Some(record) => Future successful record.status
          case None =>
            for {
              _ <- agentStatusChangeMongoService.createRecord(
                AgentStatusChangeRecord(arn, stubbedStatus, DateTime.now()))
            } yield stubbedStatus
        }
      } yield Ok(toJson(AgentDetails(statusToReturn, agencyName)))
  }

  def getAgentDetailsByUtr(utr: Utr): Action[AnyContent] = Action.async {
    implicit request =>
      for {
        arnAndAgencyName <- getArnAndAgencyNameFor(utr)
        recordOpt <- findCurrentRecordByArn(arnAndAgencyName.arn.value)
        statusToReturn <- recordOpt match {
          case Some(record) => Future successful record.status
          case None =>
            for {
              _ <- agentStatusChangeMongoService.createRecord(
                AgentStatusChangeRecord(arnAndAgencyName.arn,
                                        stubbedStatus,
                                        DateTime.now()))
            } yield stubbedStatus
        }
      } yield
        Ok(toJson(AgentDetails(statusToReturn, arnAndAgencyName.agencyName)))
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

}
