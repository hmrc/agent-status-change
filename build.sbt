import uk.gov.hmrc.SbtAutoBuildPlugin

lazy val root = (project in file("."))
  .settings(
    name := "agent-status-change",
    organization := "uk.gov.hmrc",
    scalaVersion := "2.12.15",
    majorVersion := 1,
    PlayKeys.playDefaultPort := 9424,
    resolvers ++= Seq(Resolver.typesafeRepo("releases")),
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    routesImport ++= Seq("uk.gov.hmrc.agentstatuschange.binders.UrlBinders._"),
    scalacOptions ++= Seq(
      "-Xfatal-warnings",
      "-Xlint:-missing-interpolator,_",
      "-Yno-adapted-args",
      //"-Ywarn-value-discard",
      "-Ywarn-dead-code",
      "-deprecation",
      "-feature",
      "-unchecked",
      "-language:implicitConversions",
      "-Wconf:src=target/.*:s", // silence warnings from compiled files
      "-Wconf:src=routes/.*:s", // silence warnings from routes files
      "-Wconf:src=*html:w",
    ),
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources",
    Compile / scalafmtOnCompile := true,
    Test / scalafmtOnCompile := true
  )
  .settings(
    Test / parallelExecution := false,
    CodeCoverageSettings.scoverageSettings
  )
  .configs(IntegrationTest)
  .settings(
    IntegrationTest / Keys.fork := false,
    Defaults.itSettings,
    IntegrationTest / unmanagedSourceDirectories += baseDirectory(_ / "it").value,
    IntegrationTest / parallelExecution := false
  )
  .enablePlugins(PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)


inConfig(IntegrationTest)(scalafmtCoreSettings)
