package uk.gov.hmrc.agentstatuschange.controllers

import play.api.mvc.Result
import play.api.mvc.Results._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisationException, InsufficientEnrolments}
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, SessionKeys}
import uk.gov.hmrc.agentstatuschange.support.AppBaseISpec

import scala.concurrent.{ExecutionContext, Future}

class AuthActionsISpec extends AppBaseISpec {

  object TestController extends AuthActions {

    override def authConnector: AuthConnector = app.injector.instanceOf[AuthConnector]

    implicit val hc = HeaderCarrier(authorization = Some(Authorization("Bearer XYZ")))
    implicit val request = FakeRequest().withSession(SessionKeys.authToken -> "Bearer XYZ")
    implicit val ec = ExecutionContext.global

    def withAuthorisedAsAgent[A]: Result = {
      await(super.withAuthorisedAsAgent { arn => Future.successful(Ok(arn.value)) })
    }

    def withAuthorisedAsClient[A]: Result = {
      await(super.withAuthorisedAsClient { mtdItTd => Future.successful(Ok(mtdItTd.value)) })
    }

    def withOnlyStride(strideRole: String) = {
      await(super.onlyStride(strideRole) { Future.successful(Ok) })
    }

  }

  "withAuthorisedAsAgent" should {

    "call body with arn when valid agent" in {
      givenAuthorisedFor(
        "{}",
        s"""{
           |"authorisedEnrolments": [
           |  { "key":"HMRC-AS-AGENT", "identifiers": [
           |    { "key":"AgentReferenceNumber", "value": "fooArn" }
           |  ]}
           |]}""".stripMargin)
      val result = TestController.withAuthorisedAsAgent
      status(result) shouldBe 200
      bodyOf(result) shouldBe "fooArn"
    }

    "throw AutorisationException when user not logged in" in {
      givenUnauthorisedWith("MissingBearerToken")
      an[AuthorisationException] shouldBe thrownBy {
        TestController.withAuthorisedAsAgent
      }
    }

    "throw InsufficientEnrolments when agent not enrolled for service" in {
      givenAuthorisedFor(
        "{}",
        s"""{
           |"authorisedEnrolments": [
           |  { "key":"HMRC-MTD-IT", "identifiers": [
           |    { "key":"MTDITID", "value": "fooMtdItId" }
           |  ]}
           |]}""".stripMargin)
      an[InsufficientEnrolments] shouldBe thrownBy {
        TestController.withAuthorisedAsAgent
      }
    }

    "throw InsufficientEnrolments when expected agent's identifier missing" in {
      givenAuthorisedFor(
        "{}",
        s"""{
           |"authorisedEnrolments": [
           |  { "key":"HMRC-AS-AGENT", "identifiers": [
           |    { "key":"BAR", "value": "fooArn" }
           |  ]}
           |]}""".stripMargin)
      an[InsufficientEnrolments] shouldBe thrownBy {
        TestController.withAuthorisedAsAgent
      }
    }
  }

  "withAuthorisedAsClient" should {

    "call body with mtditid when valid mtd client" in {
      givenAuthorisedFor(
        "{}",
        s"""{
           |"authorisedEnrolments": [
           |  { "key":"HMRC-MTD-IT", "identifiers": [
           |    { "key":"MTDITID", "value": "fooMtdItId" }
           |  ]}
           |]}""".stripMargin)

      val result = TestController.withAuthorisedAsClient
      status(result) shouldBe 200
      bodyOf(result) shouldBe "fooMtdItId"
    }

    "throw InsufficientEnrolments when client not enrolled for service" in {
      givenAuthorisedFor(
        "{}",
        s"""{
           |"authorisedEnrolments": [
           |  { "key":"HMRC-AS-AGENT", "identifiers": [
           |    { "key":"AgentReferenceNumber", "value": "fooArn" }
           |  ]}
           |]}""".stripMargin)
      an[InsufficientEnrolments] shouldBe thrownBy {
        TestController.withAuthorisedAsClient
      }
    }

    "throw InsufficientEnrolments when expected client's identifier missing" in {
      givenAuthorisedFor(
        "{}",
        s"""{
           |"authorisedEnrolments": [
           |  { "key":"HMRC-MTD-IT", "identifiers": [
           |    { "key":"BAR", "value": "fooMtdItId" }
           |  ]}
           |]}""".stripMargin)
      an[InsufficientEnrolments] shouldBe thrownBy {
        TestController.withAuthorisedAsClient
      }
    }
  }

  "onlyStride" should {
    "return 200 for successful stride login" in {
      givenOnlyStrideStub("caat", "123ABC")

      val result = TestController.withOnlyStride("caat")
      status(result) shouldBe 200
    }

    "return 401 for incorrect stride login" in {
      givenOnlyStrideStub("maintain-agent-relationships", "123ABC")

      val result = TestController.withOnlyStride("caat")
      status(result) shouldBe 401
    }

    "return 403 if non-stride login" in {

      val result = TestController.withOnlyStride("caat")
      status(result) shouldBe 403
    }
  }

}
