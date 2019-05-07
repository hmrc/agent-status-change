package uk.gov.hmrc.agentstatuschange.wiring
import com.google.inject.ImplementedBy
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@ImplementedBy(classOf[AppConfigImpl])
trait AppConfig {
  val appName: String
}

@Singleton
class AppConfigImpl @Inject()(config: ServicesConfig) extends AppConfig {
  override val appName: String = config.getString("appName")
}
