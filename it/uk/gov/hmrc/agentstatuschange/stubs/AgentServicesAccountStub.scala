package uk.gov.hmrc.agentstatuschange.stubs

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, stubFor, urlEqualTo, _}
import uk.gov.hmrc.agentmtdidentifiers.model.{Arn, Utr}
import uk.gov.hmrc.agentstatuschange.support.WireMockSupport

trait AgentServicesAccountStub {
  me: WireMockSupport =>

  def givenAgencyNameArn(arn: Arn, agencyName: String): Unit =
    stubFor(
      get(urlEqualTo(s"/agent-services-account/client/agency-name/${arn.value}"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withBody(s"""{"agencyName": "$agencyName"}""")
        ))

  def givenAgencyNameArnNotFound(arn: Arn): Unit =
    stubFor(
      get(urlEqualTo(s"/agent-services-account/client/agency-name/${arn.value}"))
        .willReturn(
          aResponse()
            .withStatus(404)
        ))

  def givenAgencyNameUtr(utr: Utr, agencyName: String): Unit =
    stubFor(
      get(urlEqualTo(s"/agent-services-account/client/agency-name/utr/${utr.value}"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withBody(s"""{"agencyName": "$agencyName"}""")
        ))

  def givenAgencyNameUtrNotFound(utr: Utr): Unit =
    stubFor(
      get(urlEqualTo(s"/agent-services-account/client/agency-name/utr/${utr.value}"))
        .willReturn(
          aResponse()
            .withStatus(404)
        ))

}
