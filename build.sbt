import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import uk.gov.hmrc.SbtAutoBuildPlugin

val silencerVersion = "1.7.0"

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    // Semicolon-separated list of regexs matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := """uk\.gov\.hmrc\.BuildInfo;.*\.Routes;.*\.RoutesPrefix;.*Filters?;MicroserviceAuditConnector;Module;GraphiteStartUp;.*\.Reverse[^.]*""",
    ScoverageKeys.coverageMinimum := 80.00,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true,
    parallelExecution in Test := false
  )
}

lazy val compileDeps = Seq(
  ws,
  "uk.gov.hmrc" %% "bootstrap-backend-play-28" % "5.9.0",
  "uk.gov.hmrc" %% "agent-mtd-identifiers" % "0.25.0-play-27",
  "uk.gov.hmrc" %% "agent-kenshoo-monitoring" % "4.8.0-play-28",
  "com.github.blemale" %% "scaffeine" % "5.1.1",
  "com.typesafe.play" %% "play-json" % "2.9.2",
  "com.typesafe.play" %% "play-json-joda" % "2.9.2",
  "uk.gov.hmrc" %% "simple-reactivemongo" % "8.0.0-play-28"
)

def testDeps(scope: String) = Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % scope,
  "org.scalatestplus" %% "mockito-3-12" % "3.2.10.0" % scope,
  "com.github.tomakehurst" % "wiremock-jre8" % "2.26.1" % scope,
  "uk.gov.hmrc" %% "reactivemongo-test" % "4.21.0-play-27" % scope,
  "com.vladsch.flexmark" % "flexmark-all" % "0.35.10" % scope
)

lazy val root = (project in file("."))
  .settings(
    name := "agent-status-change",
    organization := "uk.gov.hmrc",
    scalaVersion := "2.12.12",
    PlayKeys.playDefaultPort := 9424,
    resolvers ++= Seq(
      Resolver.typesafeRepo("releases"),
    ),
    libraryDependencies ++= compileDeps ++ testDeps("test") ++ testDeps("it"),
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
    ),
    publishingSettings,
    scoverageSettings,
    unmanagedResourceDirectories in Compile += baseDirectory.value / "resources",
    routesImport ++= Seq("uk.gov.hmrc.agentstatuschange.binders.UrlBinders._"),
    scalacOptions ++= Seq(
      "-P:silencer:pathFilters=routes",
      "-Xfatal-warnings"
    ),
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
  .enablePlugins(PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)


inConfig(IntegrationTest)(scalafmtCoreSettings)
