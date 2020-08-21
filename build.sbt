import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import uk.gov.hmrc.SbtAutoBuildPlugin

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    // Semicolon-separated list of regexs matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := """uk\.gov\.hmrc\.BuildInfo;.*\.Routes;.*\.RoutesPrefix;.*Filters?;MicroserviceAuditConnector;Module;GraphiteStartUp;.*\.Reverse[^.]*""",
    ScoverageKeys.coverageMinimum := 75.00,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true,
    parallelExecution in Test := false
  )
}

lazy val compileDeps = Seq(
  ws,
  "uk.gov.hmrc" %% "bootstrap-backend-play-27" % "2.24.0",
  "uk.gov.hmrc" %% "auth-client" % "3.0.0-play-27",
  "uk.gov.hmrc" %% "agent-mtd-identifiers" % "0.19.0-play-27",
  "uk.gov.hmrc" %% "domain" % "5.9.0-play-27",
  "uk.gov.hmrc" %% "agent-kenshoo-monitoring" % "4.4.0",
  "com.github.blemale" %% "scaffeine" % "4.0.1",
  "com.typesafe.play" %% "play-json" % "2.7.0",
  "com.typesafe.play" %% "play-json-joda" % "2.7.0",
  "uk.gov.hmrc" %% "simple-reactivemongo" % "7.30.0-play-27"
)

def testDeps(scope: String) = Seq(
  "uk.gov.hmrc" %% "hmrctest" % "3.9.0-play-26" % scope,
  "org.scalatest" %% "scalatest" % "3.0.8" % scope,
  "org.mockito" % "mockito-core" % "3.5.2" % scope,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.3" % scope,
  "com.github.tomakehurst" % "wiremock-jre8" % "2.27.1" % scope,
  "uk.gov.hmrc" %% "reactivemongo-test" % "4.21.0-play-27" % scope
)

lazy val root = (project in file("."))
  .settings(
    name := "agent-status-change",
    organization := "uk.gov.hmrc",
    scalaVersion := "2.12.10",
    PlayKeys.playDefaultPort := 9424,
    resolvers := Seq(
      Resolver.bintrayRepo("hmrc", "releases"),
      Resolver.bintrayRepo("hmrc", "release-candidates"),
      Resolver.typesafeRepo("releases"),
      Resolver.jcenterRepo
    ),
    libraryDependencies ++= compileDeps ++ testDeps("test") ++ testDeps("it"),
    publishingSettings,
    scoverageSettings,
    unmanagedResourceDirectories in Compile += baseDirectory.value / "resources",
    routesImport ++= Seq("uk.gov.hmrc.agentstatuschange.binders.UrlBinders._"),
    scalafmtOnCompile in Compile := true,
    scalafmtOnCompile in Test := true
  )
  .configs(IntegrationTest)
  .settings(
    Keys.fork in IntegrationTest := false,
    Defaults.itSettings,
    unmanagedSourceDirectories in IntegrationTest += baseDirectory(_ / "it").value,
    parallelExecution in IntegrationTest := false,
    scalafmtOnCompile in IntegrationTest := true,
    majorVersion := 0
  )
  .enablePlugins(PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory)


inConfig(IntegrationTest)(scalafmtCoreSettings)
