/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.agentstatuschange.support

import java.nio.charset.StandardCharsets.UTF_8
import java.util.Base64

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.Seconds
import org.scalatest.time.Span
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

abstract class ServerBaseISpec
extends BaseISpec
with GuiceOneServerPerSuite
with ScalaFutures {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      "microservice.services.auth.port" -> wireMockPort,
      "metrics.enabled" -> true,
      "auditing.enabled" -> true,
      "auditing.consumer.baseUri.host" -> wireMockHost,
      "auditing.consumer.baseUri.port" -> wireMockPort,
      "microservice.services.des.host" -> wireMockHost,
      "microservice.services.des.port" -> wireMockPort,
      "microservice.services.agent-client-authorisation.host" -> wireMockHost,
      "microservice.services.agent-client-authorisation.port" -> wireMockPort,
      "microservice.services.agent-fi-relationship.host" -> wireMockHost,
      "microservice.services.agent-fi-relationship.port" -> wireMockPort,
      "microservice.services.agent-mapping.host" -> wireMockHost,
      "microservice.services.agent-mapping.port" -> wireMockPort,
      "microservice.services.agent-client-relationships.host" -> wireMockHost,
      "microservice.services.agent-client-relationships.port" -> wireMockPort,
      "mongodb.uri" -> s"mongodb://127.0.0.1:27017/test-${this.getClass.getSimpleName}",
      "test.stubbed.status" -> "Active",
      "termination.stride.enrolment" -> "caat"
    )
    .build()

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(timeout = Span(4, Seconds), interval = Span(1, Seconds))

  def basicAuth(string: String): String = Base64.getEncoder.encodeToString(string.getBytes(UTF_8))

}
