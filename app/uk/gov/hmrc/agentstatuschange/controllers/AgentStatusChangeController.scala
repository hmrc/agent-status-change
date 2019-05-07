package uk.gov.hmrc.agentstatuschange.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json.toJson
import play.api.mvc._
import uk.gov.hmrc.agentmtdidentifiers.model.Utr
import uk.gov.hmrc.agentstatuschange.models.AgentStatusChangeModel
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AgentStatusChangeController @Inject()(override val authConnector: AuthConnector, cc: ControllerComponents)(
  implicit ec: ExecutionContext)
    extends BackendController(cc) with AuthActions {

  def entities: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(toJson(AgentStatusChangeModel("hello world", None, None, None))))
  }

  def entitiesByUtr(utr: Utr): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(toJson(AgentStatusChangeModel(s"hello $utr", None, None, None))))
  }

}
