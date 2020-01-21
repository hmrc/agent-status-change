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

package uk.gov.hmrc.agentstatuschange.wiring

import java.net.URL

import com.google.inject.ImplementedBy
import javax.inject.Inject
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@ImplementedBy(classOf[AppConfigImpl])
trait AppConfig {
  val appName: String
  val desUrl: URL
  val desAuthorizationToken: String
  val desEnvironment: String
  val agentClientAuthorisationBaseUrl: String
  val agentFiRelationshipBaseUrl: String
  val agentMappingBaseUrl: String
  val terminationStrideRole: String
}

class AppConfigImpl @Inject()(config: ServicesConfig) extends AppConfig {
  val appName = config.getString("appName")
  val desUrl: URL = new URL(config.baseUrl("des"))
  val desAuthorizationToken: String =
    config.getString("microservice.services.des.authorization-token")
  val desEnvironment: String =
    config.getString("microservice.services.des.environment")

  val agentClientAuthorisationBaseUrl: String =
    config.baseUrl("agent-client-authorisation")
  val agentFiRelationshipBaseUrl: String =
    config.baseUrl("agent-fi-relationship")
  val agentMappingBaseUrl: String =
    config.baseUrl("agent-mapping")

  val terminationStrideRole: String =
    config.getString("termination.stride.enrolment")
}
