import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import uk.gov.hmrc.SbtAutoBuildPlugin

val silencerVersion = "1.7.7"

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    // Semicolon-separated list of regexs matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := """uk\.gov\.hmrc\.BuildInfo;.*\.Routes;.*\.RoutesPrefix;.*Filters?;MicroserviceAuditConnector;Module;GraphiteStartUp;.*\.Reverse[^.]*""",
    ScoverageKeys.coverageMinimumStmtTotal := 80.00,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true,
    Test / parallelExecution := false
  )
}

lazy val compileDeps = Seq(
  ws,
  "uk.gov.hmrc" %% "bootstrap-backend-play-28" % "7.12.0",
  "uk.gov.hmrc" %% "agent-mtd-identifiers" % "0.50.0-play-28",
  "uk.gov.hmrc" %% "agent-kenshoo-monitoring" % "4.8.0-play-28",
  "com.typesafe.play" %% "play-json" % "2.9.2",
  "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28" % "0.74.0"
)

def testDeps(scope: String) = Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % scope,
  "org.scalatestplus" %% "mockito-3-12" % "3.2.10.0" % scope,
  "com.github.tomakehurst" % "wiremock-jre8" % "2.26.1" % scope,
  "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-28" % "0.74.0" % scope,
  "com.vladsch.flexmark" % "flexmark-all" % "0.35.10" % scope
)

lazy val root = (project in file("."))
  .settings(
    name := "agent-status-change",
    organization := "uk.gov.hmrc",
    scalaVersion := "2.12.15",
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
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources",
    routesImport ++= Seq("uk.gov.hmrc.agentstatuschange.binders.UrlBinders._"),
    scalacOptions ++= Seq(
      "-P:silencer:pathFilters=routes",
      "-Xfatal-warnings"
    ),
    Compile / scalafmtOnCompile := true,
    Test / scalafmtOnCompile := true
  )
  .configs(IntegrationTest)
  .settings(
    IntegrationTest / Keys.fork := false,
    Defaults.itSettings,
    IntegrationTest / unmanagedSourceDirectories += baseDirectory(_ / "it").value,
    IntegrationTest / parallelExecution := false,
    majorVersion := 0
  )
  .enablePlugins(PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)


inConfig(IntegrationTest)(scalafmtCoreSettings)
