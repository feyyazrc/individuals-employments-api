import play.sbt.routes.RoutesKeys
import sbt.Keys.compile
import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings}
import uk.gov.hmrc.ExternalService
import uk.gov.hmrc.ServiceManagerPlugin.Keys.itDependenciesList
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "individuals-employments-api"
val hmrc = "uk.gov.hmrc"

TwirlKeys.templateImports := Seq.empty
RoutesKeys.routesImport := Seq(
  "uk.gov.hmrc.individualsemploymentsapi.Binders._"
)

lazy val playSettings: Seq[Setting[_]] = Seq(
  routesImport ++= Seq(
    "uk.gov.hmrc.domain._",
    "uk.gov.hmrc.individualsemploymentsapi.domain._",
    "uk.gov.hmrc.individualsemploymentsapi.Binders._"))

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;" +
      ".*BuildInfo.;uk.gov.hmrc.BuildInfo;.*Routes;.*RoutesPrefix*;" +
      ".*definition*;",
    ScoverageKeys.coverageMinimum := 80,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    parallelExecution in Test := false
  )
}

lazy val appDependencies: Seq[ModuleID] = AppDependencies.compile ++ AppDependencies.test()
lazy val plugins: Seq[Plugins] = Seq.empty
lazy val externalServices =
  List(ExternalService("AUTH"), ExternalService("INDIVIDUALS_MATCHING_API"), ExternalService("DES"))
def intTestFilter(name: String): Boolean = name startsWith "it"
def unitFilter(name: String): Boolean = name startsWith "unit"
def componentFilter(name: String): Boolean = name startsWith "component"
lazy val ComponentTest = config("component") extend Test

lazy val microservice =
  Project(appName, file("."))
    .enablePlugins(Seq(
      play.sbt.PlayScala,
      SbtAutoBuildPlugin,
      SbtGitVersioning,
      SbtDistributablesPlugin,
      SbtArtifactory) ++ plugins: _*)
    .settings(playSettings: _*)
    .settings(scalaSettings: _*)
    .settings(scoverageSettings: _*)
    .settings(publishingSettings: _*)
    .settings(scalaVersion := "2.12.11")
    .settings(defaultSettings(): _*)
    .settings(
      libraryDependencies ++= appDependencies,
      testOptions in Test := Seq(Tests.Filter(unitFilter)),
      retrieveManaged := true,
      evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
    )
    .settings(unmanagedResourceDirectories in Compile += baseDirectory.value / "resources")
    .configs(IntegrationTest)
    .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
    .settings(itDependenciesList := externalServices)
    .settings(
      dependencyOverrides ++= AppDependencies.overrides,
      Keys.fork in IntegrationTest := false,
      unmanagedSourceDirectories in IntegrationTest := (baseDirectory in IntegrationTest)(base => Seq(base / "test")).value,
      testOptions in IntegrationTest := Seq(Tests.Filter(intTestFilter)),
      addTestReportOption(IntegrationTest, "int-test-reports"),
      testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value),
      parallelExecution in IntegrationTest := false
    )
    .configs(ComponentTest)
    .settings(inConfig(ComponentTest)(Defaults.testSettings): _*)
    .settings(
      testOptions in ComponentTest := Seq(Tests.Filter(componentFilter)),
      unmanagedSourceDirectories in ComponentTest := (baseDirectory in ComponentTest)(base => Seq(base / "test")).value,
      testGrouping in ComponentTest := oneForkedJvmPerTest((definedTests in ComponentTest).value),
      parallelExecution in ComponentTest := false
    )
    .settings(resolvers ++= Seq(
      Resolver.jcenterRepo
    ))
    .settings(PlayKeys.playDefaultPort := 9651)
    .settings(majorVersion := 0)

def oneForkedJvmPerTest(tests: Seq[TestDefinition]) =
  tests.map { test =>
    new Group(test.name, Seq(test), SubProcess(ForkOptions().withRunJVMOptions(Vector(s"-Dtest.name=${test.name}"))))
  }

lazy val compileAll = taskKey[Unit]("Compiles sources in all configurations.")

compileAll := {
  val a = (compile in Test).value
  val b = (compile in IntegrationTest).value
  ()
}
