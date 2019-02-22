import play.core.PlayVersion
import play.sbt.PlayImport._
import play.sbt.routes.RoutesKeys.routesImport
import sbt._

object MicroServiceBuild extends Build with MicroService {

  val appName = "individuals-employments-api"

  override lazy val playSettings: Seq[Setting[_]] = Seq(routesImport ++= Seq(
    "uk.gov.hmrc.domain._",
    "uk.gov.hmrc.individualsemploymentsapi.domain._",
    "uk.gov.hmrc.individualsemploymentsapi.Binders._")
  )

  override lazy val appDependencies: Seq[ModuleID] = compile ++ test()

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "microservice-bootstrap" % "10.4.0",
    "uk.gov.hmrc" %% "domain" % "5.3.0",
    "uk.gov.hmrc" %% "auth-client" % "2.19.0-play-25",
    "uk.gov.hmrc" %% "play-hal" % "1.8.0-play-25",
    "uk.gov.hmrc" %% "play-hmrc-api" % "3.4.0-play-25",
    "uk.gov.hmrc" %% "mongo-caching" % "5.7.0",
    "uk.gov.hmrc" %% "json-encryption" % "3.3.0"
  )

  def test(scope: String = "test,it") = Seq(
    "uk.gov.hmrc" %% "hmrctest" % "3.5.0-play-25" % scope,
    "org.scalatest" %% "scalatest" % "2.2.6" % scope,
    "org.pegdown" % "pegdown" % "1.6.0" % scope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % scope,
    "org.scalaj" %% "scalaj-http" % "1.1.6" % scope,
    "org.mockito" % "mockito-all" % "1.10.19" % scope,
    "com.github.tomakehurst" % "wiremock" % "2.21.0" % scope,
    "uk.gov.hmrc" %% "reactivemongo-test" % "2.0.0" % scope
  )

}
