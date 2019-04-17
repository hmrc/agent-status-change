package uk.gov.hmrc.agentstatuschange.controllers

import javax.inject.{ Inject, Singleton}

import play.api.libs.json.Json.toJson
import play.api.mvc._
import play.api.{ Configuration, Environment }
import uk.gov.hmrc.agentstatuschange.connectors.MicroserviceAuthConnector
import uk.gov.hmrc.agentstatuschange.models.AgentstatuschangeModel
import uk.gov.hmrc.play.bootstrap.controller.BaseController
import uk.gov.hmrc.agentmtdidentifiers.model.Utr

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AgentstatuschangeController @Inject() (
  val authConnector: MicroserviceAuthConnector,
  val env: Environment)(implicit val configuration: Configuration, ec: ExecutionContext)
  extends BaseController with AuthActions {

  def entities: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(toJson(AgentstatuschangeModel("hello world", None, None, None))))
  }

  def entitiesByUtr(utr: Utr): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(toJson(AgentstatuschangeModel(s"hello $utr", None, None, None))))
  }

}
