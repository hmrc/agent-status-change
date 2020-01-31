package uk.gov.hmrc.agentstatuschange.support

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

abstract class ServerBaseISpec extends BaseISpec with GuiceOneServerPerSuite with ScalaFutures {

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
    ).build()

  implicit override val patienceConfig: PatienceConfig = PatienceConfig(timeout = Span(4, Seconds), interval = Span(1, Seconds))
}
