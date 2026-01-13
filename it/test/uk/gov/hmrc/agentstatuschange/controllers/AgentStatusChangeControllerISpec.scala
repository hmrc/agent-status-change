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

package uk.gov.hmrc.agentstatuschange.controllers

import play.api.libs.json.{JsString, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.agentstatuschange.models._
import uk.gov.hmrc.agentstatuschange.services.AgentStatusChangeMongoService
import uk.gov.hmrc.agentstatuschange.stubs.{AgentStubs, DesStubs}
import uk.gov.hmrc.agentstatuschange.support.{DualSuite, MongoApp, ServerBaseISpec}
import uk.gov.hmrc.http.HeaderNames

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AgentStatusChangeControllerISpec
extends ServerBaseISpec
with MongoApp
with DesStubs
with AgentStubs {

  this: DualSuite =>

  val controller = app.injector.instanceOf(classOf[AgentStatusChangeController])

  def repo: AgentStatusChangeMongoService = app.injector.instanceOf[AgentStatusChangeMongoService]

  val utr = Utr("3110118001")
  implicit val ord: Ordering[Instant] = Ordering.by(time => time.toEpochMilli)

  "AgentStatusChangeController" when {
    "GET /status/arn/:arn" should {
      val request = FakeRequest().withHeaders(HeaderNames.authorisation -> "Bearer XYZ")
      "respond 200 with default stubbed data when no record exists" in {
        givenAuthorisedFor("{}", "{}")
        givenBusinessPartnerRecordExistsFor(
          "arn",
          utr,
          arn,
          "Bing Bing"
        )
        val result = controller.getAgentDetailsByArn(arn)(request)
        status(result) shouldBe 200
        contentAsJson(result) shouldBe Json.obj(
          "arn" -> arn,
          "agentStatus" -> (Active: AgentStatus),
          "agencyName" -> Some("Bing Bing")
        )
      }

      "respond 200 with data when a suspended record exists" in {
        await(
          repo.createRecord(
            AgentStatusChangeRecord(
              arn,
              Suspended(Reason(Some("other"), Some("eaten by tyrannosaur"))),
              Instant.parse("2019-01-01T10:15:30.00Z")
            )
          )
        )
        givenAuthorisedFor("{}", "{}")
        givenBusinessPartnerRecordExistsFor(
          "arn",
          utr,
          arn,
          "Bong Bing"
        )
        val result = controller.getAgentDetailsByArn(arn)(request)
        status(result) shouldBe 200
        contentAsJson(result) shouldBe Json.obj(
          "arn" -> arn,
          "agentStatus" -> (Suspended(
            Reason(Some("other"), Some("eaten by tyrannosaur"))
          ): AgentStatus),
          "agencyName" -> Some("Bong Bing")
        )
      }

      "respond 200 with data when a deactivated record exists" in {
        await(
          repo.createRecord(
            AgentStatusChangeRecord(
              arn,
              Deactivated(Reason(Some("other"), Some("brain in jar"))),
              Instant.parse("2019-01-01T10:15:30.00Z")
            )
          )
        )
        givenAuthorisedFor("{}", "{}")
        givenBusinessPartnerRecordExistsFor(
          "arn",
          utr,
          arn,
          "Bong Bing"
        )
        val result = controller.getAgentDetailsByArn(arn)(request)
        status(result) shouldBe 200
        contentAsJson(result) shouldBe Json.obj(
          "arn" -> arn,
          "agentStatus" -> (Deactivated(
            Reason(Some("other"), Some("brain in jar"))
          ): AgentStatus),
          "agencyName" -> Some("Bong Bing")
        )
      }

      "respond 404 with reason when record does not exist" in {
        givenAuthorisedFor("{}", "{}")
        givenBusinessPartnerRecordNotFoundFor(
          "arn",
          utr,
          arn,
          ""
        )
        val result = controller.getAgentDetailsByArn(arn)(request)
        status(result) shouldBe 404
        contentAsJson(result) shouldBe JsString("UTR_NOT_SUBSCRIBED")
      }

      "respond 400 with reason when utr is invalid" in {
        givenAuthorisedFor("{}", "{}")
        givenBusinessPartnerRecordInvalidFor(
          "arn",
          utr,
          arn,
          ""
        )
        val result = controller.getAgentDetailsByArn(arn)(request)
        status(result) shouldBe 400
        contentAsJson(result) shouldBe JsString("INVALID_UTR")
      }
    }

    "GET /status/utr/:utr" should {
      val request = FakeRequest().withHeaders(HeaderNames.authorisation -> "Bearer XYZ")
      "respond with data when active" in {
        givenAuthorisedFor("{}", "{}")
        givenBusinessPartnerRecordExistsFor(
          "utr",
          utr,
          arn,
          "Bong Bing"
        )
        val result = controller.getAgentDetailsByUtr(utr)(request)
        status(result) shouldBe 200
        contentAsJson(result) shouldBe Json.obj(
          "arn" -> arn,
          "agentStatus" -> (Active: AgentStatus),
          "agencyName" -> Some("Bong Bing")
        )
      }

      "respond with data when a suspended record exists" in {
        await(
          repo.createRecord(
            AgentStatusChangeRecord(
              arn,
              Suspended(Reason(Some("other"), Some("eaten by tyrannosaur"))),
              Instant.parse("2019-01-01T10:15:30.00Z")
            )
          )
        )
        givenAuthorisedFor("{}", "{}")
        givenBusinessPartnerRecordExistsFor(
          "utr",
          utr,
          arn,
          "Bong Bing"
        )
        val result = controller.getAgentDetailsByUtr(utr)(request)
        status(result) shouldBe 200
        contentAsJson(result) shouldBe Json.obj(
          "arn" -> arn,
          "agentStatus" -> (Suspended(
            Reason(Some("other"), Some("eaten by tyrannosaur"))
          ): AgentStatus),
          "agencyName" -> Some("Bong Bing")
        )
      }

      "respond with data when a deactivated record exists" in {
        await(
          repo.createRecord(
            AgentStatusChangeRecord(
              arn,
              Deactivated(Reason(Some("other"), Some("brain in jar"))),
              Instant.parse("2019-01-01T10:15:30.00Z")
            )
          )
        )
        givenAuthorisedFor("{}", "{}")
        givenBusinessPartnerRecordExistsFor(
          "utr",
          utr,
          arn,
          "Bong Bing"
        )
        val result = controller.getAgentDetailsByUtr(utr)(request)
        status(result) shouldBe 200
        contentAsJson(result) shouldBe Json.obj(
          "arn" -> arn,
          "agentStatus" -> (Deactivated(
            Reason(Some("other"), Some("brain in jar"))
          ): AgentStatus),
          "agencyName" -> Some("Bong Bing")
        )
      }

      "respond 404 with reason when record does not exist" in {
        givenAuthorisedFor("{}", "{}")
        givenBusinessPartnerRecordNotFoundFor(
          "utr",
          utr,
          arn,
          ""
        )
        val result = controller.getAgentDetailsByUtr(utr)(request)
        status(result) shouldBe 404
        contentAsJson(result) shouldBe JsString("UTR_NOT_SUBSCRIBED")
      }

      "respond 400 with reason when utr is invalid" in {
        givenAuthorisedFor("{}", "{}")
        givenBusinessPartnerRecordInvalidFor(
          "utr",
          utr,
          arn,
          ""
        )
        val result = controller.getAgentDetailsByUtr(utr)(request)
        status(result) shouldBe 400
        contentAsJson(result) shouldBe JsString("INVALID_UTR")
      }
    }

    "POST /status/arn/:arn" should {
      val request = FakeRequest().withHeaders(HeaderNames.authorisation -> "Bearer XYZ")
      "return 200 and create a new suspended record when a reason is provided" in {
        givenAuthorisedFor(
          "{}",
          s"""{
             |"authorisedEnrolments": [
             |  { "key":"HMRC-AS-AGENT", "identifiers": [
             |    { "key":"AgentReferenceNumber", "value": "${arn.value}" }
             |  ]}
             |]}""".stripMargin
        )
        val requestBody = Json.parse("""{
                                       |  "reason": "other",
                                       |  "extraDetails": "missed the train"
                                       |}""".stripMargin)

        val result: Future[Result] = controller.changeStatus(arn)(request.withBody(requestBody))
        status(result) shouldBe 200
        await(repo.findCurrentRecordByArn(arn.value)).get.status shouldBe Suspended(
          Reason(Some("other"), Some("missed the train"))
        )
      }
      "return 200 and create a new active record when a reason is not provided" in {
        givenAuthorisedFor(
          "{}",
          s"""{
             |"authorisedEnrolments": [
             |  { "key":"HMRC-AS-AGENT", "identifiers": [
             |    { "key":"AgentReferenceNumber", "value": "${arn.value}" }
             |  ]}
             |]}""".stripMargin
        )
        val requestBody = Json.parse("""{}""".stripMargin)

        val result: Future[Result] = controller.changeStatus(arn)(request.withBody(requestBody))
        status(result) shouldBe 200
        await(repo.findCurrentRecordByArn(arn.value)).get.status shouldBe Active
      }
    }

    "DELETE /agent/:arn/terminate" should {
      "return 200 for deleting all agent records" in {
        givenSuccessfullyRemoveInvitations(arn)
        givenSuccessfullyRemoveAFiRelationships(arn)
        givenSuccessfullyRemoveMapping(arn)
        givenSuccessfullyRemoveAgentClientRelationships(arn)

        val result =
          controller.removeAgentRecords(arn)(FakeRequest(
            "DELETE",
            "agent/:arn/terminate"
          ).withHeaders(
            HeaderNames.authorisation -> s"Basic ${basicAuth("username:password")}"
          ))

        status(result) shouldBe 200
      }

      "return 400 for invalid ARN" in {
        givenSuccessfullyRemoveInvitations(arn)
        givenSuccessfullyRemoveAFiRelationships(arn)
        givenSuccessfullyRemoveMapping(arn)
        givenSuccessfullyRemoveAgentClientRelationships(arn)

        val result =
          controller.removeAgentRecords(Arn("MARN01"))(FakeRequest(
            "DELETE",
            "agent/:arn/terminate"
          ).withHeaders(
            HeaderNames.authorisation -> s"Basic ${basicAuth("username:password")}"
          ))

        status(result) shouldBe 400
      }

      "return 500 for complete error" in {
        givenInternalServerErrorRemoveInvitations(arn)
        givenInternalServerErrorIRemoveAFiRelationships(arn)
        givenInternalServerErrorRemoveMapping(arn)

        val result =
          controller.removeAgentRecords(arn)(FakeRequest(
            "DELETE",
            "agent/:arn/terminate"
          ).withHeaders(
            HeaderNames.authorisation -> s"Basic ${basicAuth("username:password")}"
          ))

        status(result) shouldBe 500
      }

      "return 500 for partial error" in {
        givenInternalServerErrorRemoveInvitations(arn)
        givenSuccessfullyRemoveAFiRelationships(arn)
        givenInternalServerErrorRemoveMapping(arn)

        val result =
          controller.removeAgentRecords(arn)(FakeRequest(
            "DELETE",
            "agent/:arn/terminate"
          ).withHeaders(
            HeaderNames.authorisation -> s"Basic ${basicAuth("username:password")}"
          ))

        status(result) shouldBe 500
      }
    }
  }

}
