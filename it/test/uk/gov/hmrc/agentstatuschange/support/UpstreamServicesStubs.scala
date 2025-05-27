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

import java.net.URL

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import org.scalatest.concurrent.Eventually
import org.scalatest.BeforeAndAfterAll
import org.scalatest.BeforeAndAfterEach
import org.scalatest.Suite

trait UpstreamServicesStubs
extends BeforeAndAfterAll
with BeforeAndAfterEach
with Eventually {

  this: Suite =>

  val wireMockHost = "localhost"
  val wireMockPort: Int = Port.randomAvailable
  val wireMockBaseUrlAsString = s"http://$wireMockHost:$wireMockPort"
  lazy val wireMockServer =
    new WireMockServer(
      wireMockConfig().port(wireMockPort)
    )

  val fakeCredId = "fakeCredId"
  val fakeCredIdType = "GovernmentGateway"

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    configureFor(wireMockHost, wireMockPort)
    wireMockServer.start()
  }

  override protected def afterAll(): Unit = {
    wireMockServer.stop()
    super.afterAll()
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset()
    givenAuthReturnsUserDetails()
  }

  def givenAuthReturnsUserDetails(): Unit = {
    val oid: String = "556737e15500005500eaf68f"
    val wireMockBaseUrlAsString = s"http://$wireMockHost:$wireMockPort"
    val wireMockBaseUrl = new URL(wireMockBaseUrlAsString)

    stubFor(get(urlEqualTo("/auth/authority"))
      .willReturn(aResponse()
        .withStatus(200)
        .withBody(
          s"""{ "userDetailsLink":"$wireMockBaseUrl/user-details/id/$oid" }""".stripMargin
        )))

    stubFor(
      get(urlPathEqualTo(s"/user-details/id/$oid"))
        .willReturn(aResponse()
          .withStatus(200)
          .withBody(
            s"""{"authProviderId": "$fakeCredId", "authProviderIdType":"$fakeCredIdType"}""".stripMargin
          ))
    )
  }

}
