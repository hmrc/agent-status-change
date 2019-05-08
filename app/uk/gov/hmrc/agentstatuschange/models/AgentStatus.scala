/*
 * Copyright 2019 HM Revenue & Customs
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
  val key = this match {
    case Active         => "active"
    case _: Suspended   => "suspended"
    case _: Deactivated => "deactivated"
  }
}

case object Active extends AgentStatus
case class Suspended(reason: String) extends AgentStatus
case class Deactivated(reason: String) extends AgentStatus

object AgentStatus {
  implicit val format = new Format[AgentStatus] {
    override def reads(json: JsValue): JsResult[AgentStatus] = {
      def readReason = (json \ "reason").as[String]

      val t = (json \ "type").as[String]
      t match {
        case "active"      => JsSuccess(Active)
        case "suspended"   => JsSuccess(Suspended(readReason))
        case "deactivated" => JsSuccess(Deactivated(readReason))
        case _             => JsError("Invalid Error")
      }
    }

    override def writes(status: AgentStatus): JsValue = {
      val json: JsObject = Json.obj("type" -> status.key)

      status match {
        case Active              => json
        case Suspended(reason)   => json + ("reason" -> JsString(reason))
        case Deactivated(reason) => json + ("reason" -> JsString(reason))
      }
    }
  }
}
