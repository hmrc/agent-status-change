package uk.gov.hmrc.agentstatuschange.stubs

import uk.gov.hmrc.agentmtdidentifiers.model.Arn
import uk.gov.hmrc.agentstatuschange.support.WireMockSupport
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, stubFor, urlEqualTo, _}

trait AgentStubs extends TestDataSupport{
  me: WireMockSupport =>

  def givenSuccessfullyRemoveInvitations(arn: Arn) = {
    stubFor(delete(urlEqualTo(s"/agent-client-authorisation/agent/${arn.value}/terminate"))
      .willReturn(aResponse()
        .withStatus(200)))
  }

  def givenInternalServerErrorRemoveInvitations(arn: Arn) = {
    stubFor(delete(urlEqualTo(s"/agent-client-authorisation/agent/${arn.value}/terminate"))
      .willReturn(aResponse()
        .withStatus(500)))
  }

  def givenSuccessfullyRemoveAFiRelationships(arn: Arn) = {
    stubFor(delete(urlEqualTo(s"/agent-fi-relationship/agent/${arn.value}/terminate"))
      .willReturn(aResponse()
        .withStatus(200)))
  }

  def givenInternalServerErrorIRemoveAFiRelationships(arn: Arn) = {
    stubFor(delete(urlEqualTo(s"/agent-fi-relationship/agent/${arn.value}/terminate"))
      .willReturn(aResponse()
        .withStatus(500)))
  }

  def givenSuccessfullyRemoveMapping(arn: Arn) = {
    stubFor(delete(urlEqualTo(s"/agent-mapping/agent/${arn.value}/terminate"))
      .willReturn(aResponse()
        .withStatus(200)))
  }

  def givenInternalServerErrorRemoveMapping(arn: Arn) = {
    stubFor(delete(urlEqualTo(s"/agent-mapping/agent/${arn.value}/terminate"))
      .willReturn(aResponse()
        .withStatus(500)))
  }

}
