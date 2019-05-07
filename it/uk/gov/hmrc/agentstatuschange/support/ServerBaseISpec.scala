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
      "auditing.consumer.baseUri.port" -> wireMockPort
    ).build()

  implicit override val patienceConfig: PatienceConfig = PatienceConfig(timeout = Span(4, Seconds), interval = Span(1, Seconds))
}
