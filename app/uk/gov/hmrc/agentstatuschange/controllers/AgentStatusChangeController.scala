package uk.gov.hmrc.agentstatuschange.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json.toJson
import play.api.mvc._
import play.api.{Configuration, Logger}
import uk.gov.hmrc.agentmtdidentifiers.model.{Arn, Utr}
import uk.gov.hmrc.agentstatuschange.connectors.AgentServicesAccountConnector
import uk.gov.hmrc.agentstatuschange.models._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.ExecutionContext

@Singleton
class AgentStatusChangeController @Inject()(
    override val authConnector: AuthConnector,
    agentServicesAccountConnector: AgentServicesAccountConnector,
    cc: ControllerComponents,
    config: Configuration)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with AuthActions {

  val configStubStatus = config.getOptional[String]("test.stubbed.status")
  val stubStatus = configStubStatus.getOrElse("Active")
  Logger.info(
    s"test.stubbed.status config value is $configStubStatus, so agent status will be $stubStatus")
  val stubbedStatus = stubStatus match {
    case "Active"      => Active
    case "Suspended"   => Suspended("some stubbed suspension reason")
    case "Deactivated" => Deactivated("some stubbed deactivation reason")
  }

  def getAgentDetailsByArn(arn: Arn): Action[AnyContent] = Action.async {
    implicit request =>
      for {
        agencyName <- agentServicesAccountConnector.getAgencyNameByArn(arn)
      } yield Ok(toJson(AgentDetails(stubbedStatus, agencyName)))
  }

  def getAgentDetailsByUtr(utr: Utr) = Action.async { implicit request =>
    for {
      agencyName <- agentServicesAccountConnector.getAgencyNameByUtr(utr)
    } yield Ok(toJson(AgentDetails(stubbedStatus, agencyName)))
  }

}
