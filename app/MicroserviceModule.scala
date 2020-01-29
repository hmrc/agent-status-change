import com.google.inject.AbstractModule
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.auth.DefaultAuthConnector
import uk.gov.hmrc.play.bootstrap.http.{DefaultHttpClient, HttpClient}

class MicroserviceModule(val environment: Environment,
                         val configuration: Configuration)
    extends AbstractModule {

  def configure(): Unit = {
    Logger(getClass).info(
      s"Starting microservice agent-status-change in mode : ${environment.mode}")

    bind(classOf[HttpClient]).to(classOf[DefaultHttpClient])
    bind(classOf[AuthConnector]).to(classOf[DefaultAuthConnector])
  }
}
