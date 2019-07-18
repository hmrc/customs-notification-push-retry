import sbt._

object AppDependencies {

  private val hmrcTestVersion = "3.9.0-play-26"
  private val scalaTestVersion = "3.0.8"
  private val scalatestplusVersion = "3.1.2"
  private val mockitoVersion = "3.0.0"
  private val wireMockVersion = "2.23.2"
  private val customsApiCommonVersion = "1.42.0-SNAPSHOT"
  private val circuitBreakerVersion = "3.3.0"
  private val testScope = "test,it"

  val hmrcTest = "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % testScope

  val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion % testScope

  val scalaTestPlusPlay = "org.scalatestplus.play" %% "scalatestplus-play" % scalatestplusVersion % testScope

  val mockito =  "org.mockito" % "mockito-core" % mockitoVersion % testScope

  val wireMock = "com.github.tomakehurst" % "wiremock-jre8" % wireMockVersion % testScope

  val customsApiCommon = "uk.gov.hmrc" %% "customs-api-common" % customsApiCommonVersion withSources()

  val customsApiCommonTests = "uk.gov.hmrc" %% "customs-api-common" % customsApiCommonVersion % testScope classifier "tests"

  val circuitBreaker = "uk.gov.hmrc" %% "reactive-circuit-breaker" % circuitBreakerVersion

}
