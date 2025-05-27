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

package uk.gov.hmrc.agentstatuschange.models

import play.api.libs.json._

sealed trait AgentStatus {
  val key: String =
    this match {
      case Active => "active"
      case _: Suspended => "suspended"
      case _: Deactivated => "deactivated"
    }
}

case object Active
extends AgentStatus

case class Suspended(reason: Reason)
extends AgentStatus

case class Deactivated(reason: Reason)
extends AgentStatus

object AgentStatus {
  implicit val format: Format[AgentStatus] =
    new Format[AgentStatus] {
      override def reads(json: JsValue): JsResult[AgentStatus] = {
        def readReason = (json \ "reason").as[Reason]

        val t = (json \ "type").as[String]
        t match {
          case "active" => JsSuccess(Active)
          case "suspended" => JsSuccess(Suspended(readReason))
          case "deactivated" => JsSuccess(Deactivated(readReason))
          case _ => JsError("Invalid Error")
        }
      }

      override def writes(status: AgentStatus): JsValue = {
        val jsonActive: JsObject = Json.obj("type" -> status.key)

        def jsonNonActive(reason: Reason): JsObject = Json.obj(
          "type" -> status.key,
          "reason" -> reason
        )

        status match {
          case Active => jsonActive
          case Suspended(reason) => jsonNonActive(reason)
          case Deactivated(reason) => jsonNonActive(reason)
        }
      }
    }
}
