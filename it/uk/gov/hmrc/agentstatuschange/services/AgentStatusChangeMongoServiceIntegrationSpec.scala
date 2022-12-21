package uk.gov.hmrc.agentstatuschange.services

import java.time.{Instant, LocalDateTime}
import javax.inject.Singleton
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.agentmtdidentifiers.model.{Arn, Utr}
import uk.gov.hmrc.agentstatuschange.models._
import uk.gov.hmrc.agentstatuschange.support.{DualSuite, MongoApp, UpstreamServicesStubs}
import uk.gov.hmrc.agentstatuschange.support.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class AgentStatusChangeMongoServiceIntegrationSpec extends UnitSpec
   with GuiceOneServerPerSuite with MongoApp with UpstreamServicesStubs {
  me: DualSuite =>

  def repo: AgentStatusChangeMongoService = app.injector.instanceOf[AgentStatusChangeMongoService]

  override implicit lazy val app: Application = appBuilder.build()

  val arn: Arn = Arn("TARN0000001")
  val utr = Utr("3110118001")
  val dateTime = Instant.parse("2019-01-01T10:15:30.00Z")

  val testResponseDate: String = LocalDateTime.now.toString
  val activeAgent: AgentStatusChangeRecord = AgentStatusChangeRecord(arn, Active, dateTime)
  val suspendedAgent: AgentStatusChangeRecord = AgentStatusChangeRecord(arn, Suspended(Reason(Some("other"), Some("lost in space"))), dateTime)
  val terminatedAgent: AgentStatusChangeRecord = AgentStatusChangeRecord(arn, Deactivated(Reason(Some("other"), Some("eaten by parasites"))), dateTime)

  implicit val ord: Ordering[Instant] =
    Ordering.by(time => time.toEpochMilli)

  protected def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.auth.port" -> wireMockPort,
        "auditing.consumer.baseUri.port" -> wireMockPort,
        "mongodb.uri" -> s"mongodb://127.0.0.1:27017/test-${this.getClass.getSimpleName}")

  "AgentStatusChangeMongoService" should {
    "findCurrentRecordByArn" should {
      "return empty when there is nothing in the database" in {
        val result = await(repo.findCurrentRecordByArn(arn.value))

        result shouldBe empty
      }

      "return entry when it exists in the database" in {
        await(repo.createRecord(AgentStatusChangeRecord(arn, Suspended(Reason(Some("other"), Some("lost in space"))), dateTime)))
        val result = await(repo.findCurrentRecordByArn(arn.value))

        result.get.arn shouldBe arn
        result.get.status shouldBe Suspended(Reason(Some("other"), Some("lost in space")))
      }
    }
  }
}