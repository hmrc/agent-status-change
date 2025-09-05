import play.sbt.PlayImport.ws
import sbt._

object AppDependencies {
  private val mongoVersion = "2.7.0"
  private val bootstrapVersion = "9.19.0"
  
  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30" % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"        % mongoVersion,
    "uk.gov.hmrc"       %% "agent-mtd-identifiers"     % "2.2.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-30" % mongoVersion,
    "org.scalatestplus"      %% "mockito-5-10"            % "3.2.18.0"
  ).map(_ % Test)
}
