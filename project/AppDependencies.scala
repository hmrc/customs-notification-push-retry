import sbt._

object AppDependencies {

  private val scalatestplusVersion = "3.1.3"
  private val mockitoVersion = "3.3.3"
  private val wireMockVersion = "2.26.3"
  private val customsApiCommonVersion = "1.50.0"
  private val circuitBreakerVersion = "3.3.0"
  private val testScope = "test,component"

  val scalaTestPlusPlay = "org.scalatestplus.play" %% "scalatestplus-play" % scalatestplusVersion % testScope

  val mockito =  "org.mockito" % "mockito-core" % mockitoVersion % testScope

  val wireMock = "com.github.tomakehurst" % "wiremock-jre8" % wireMockVersion % testScope

  val customsApiCommon = "uk.gov.hmrc" %% "customs-api-common" % customsApiCommonVersion withSources()

  val customsApiCommonTests = "uk.gov.hmrc" %% "customs-api-common" % customsApiCommonVersion % testScope classifier "tests"

  val circuitBreaker = "uk.gov.hmrc" %% "reactive-circuit-breaker" % circuitBreakerVersion

}
