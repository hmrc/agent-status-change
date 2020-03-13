package uk.gov.hmrc.agentstatuschange.support

import java.net.URL

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}
import uk.gov.hmrc.play.it.Port

trait UpstreamServicesStubs extends BeforeAndAfterAll
  with BeforeAndAfterEach with Eventually {

  this: Suite =>

  val wireMockHost = "localhost"
  val wireMockPort: Int = Port.randomAvailable
  val wireMockBaseUrlAsString = s"http://$wireMockHost:$wireMockPort"
  lazy val wireMockServer = new WireMockServer(wireMockConfig().port(wireMockPort))

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
        .withBody(s"""{ "userDetailsLink":"$wireMockBaseUrl/user-details/id/$oid" }""".stripMargin)))

    stubFor(get(urlPathEqualTo(s"/user-details/id/$oid"))
      .willReturn(aResponse()
        .withStatus(200)
        .withBody(s"""{"authProviderId": "$fakeCredId", "authProviderIdType":"$fakeCredIdType"}""".stripMargin)))
  }

  private def similarToJson(value: String) = equalToJson(value.stripMargin, true, true)
}

