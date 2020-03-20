package uk.gov.hmrc.agentstatuschange.stubs

import uk.gov.hmrc.agentmtdidentifiers.model.Arn
import uk.gov.hmrc.agentstatuschange.support.WireMockSupport
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, stubFor, urlEqualTo, _}

trait AgentStubs extends TestDataSupport{
  me: WireMockSupport =>

  def givenSuccessfullyRemoveInvitations(arn: Arn) = {
    stubFor(delete(urlEqualTo(s"/agent-client-authorisation/agent/${arn.value}/terminate"))
      .willReturn(aResponse()
        .withStatus(200)
        .withBody(
          """{ "counts": [
            |  {
            |    "service": "agent-client-authorisation",
            |    "store": "agent-reference",
            |    "count": 1
            |  },
            |  {
            |    "service": "agent-client-authorisation",
            |    "store": "invitations",
            |    "count": 15
            |  }]
            |}
            |""".stripMargin)))
  }

  def givenInternalServerErrorRemoveInvitations(arn: Arn) = {
    stubFor(delete(urlEqualTo(s"/agent-client-authorisation/agent/${arn.value}/terminate"))
      .willReturn(aResponse()
        .withStatus(500)))
  }

  def givenSuccessfullyRemoveAFiRelationships(arn: Arn) = {
    stubFor(delete(urlEqualTo(s"/agent-fi-relationship/agent/${arn.value}/terminate"))
      .willReturn(aResponse()
        .withStatus(200)
        .withBody(
          """{ "counts": [
            |  {
            |    "service": "agent-fi-relationship",
            |    "store": "fi-relationship",
            |    "count": 9
            |  }]
            |}
            |""".stripMargin)))
  }

  def givenInternalServerErrorIRemoveAFiRelationships(arn: Arn) = {
    stubFor(delete(urlEqualTo(s"/agent-fi-relationship/agent/${arn.value}/terminate"))
      .willReturn(aResponse()
        .withStatus(500)))
  }

  def givenSuccessfullyRemoveMapping(arn: Arn) = {
    stubFor(delete(urlEqualTo(s"/agent-mapping/agent/${arn.value}/terminate"))
      .willReturn(aResponse()
        .withStatus(200)
        .withBody(
          """{ "counts": [
            |  {
            |    "service": "agent-mapping",
            |    "store": "all-regimes",
            |    "count": 25
            |  }]
            |}
            |""".stripMargin)))
  }

  def givenSuccessfullyRemoveAgentClientRelationships(arn: Arn) = {
    stubFor(delete(urlEqualTo(s"/agent-client-relationships/agent/${arn.value}/terminate"))
      .willReturn(aResponse()
        .withStatus(200)
        .withBody(
          """{ "counts": [
            |  {
            |    "service": "agent-client-relationships",
            |    "store": "delete-record",
            |    "count": 15
            |  },
            |  {
            |    "service": "agent-client-relationships",
            |    "store": "relationship-copy-record",
            |    "count": 12
            |  }]
            |}
            |""".stripMargin)))
  }

  def givenInternalServerErrorRemoveMapping(arn: Arn) = {
    stubFor(delete(urlEqualTo(s"/agent-mapping/agent/${arn.value}/terminate"))
      .willReturn(aResponse()
        .withStatus(500)))
  }

}
