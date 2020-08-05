/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import AppDependencies._
import com.typesafe.sbt.web.PathMapping
import com.typesafe.sbt.web.pipeline.Pipeline
import sbt.Keys._
import sbt.Tests.{Group, SubProcess}
import sbt.{Resolver, Test, inConfig, _}
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, targetJvm}
import uk.gov.hmrc.PublishingSettings._
import uk.gov.hmrc.gitstamp.GitStampPlugin._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._

import scala.language.postfixOps

name := "customs-notification-push-retry"
scalaVersion := "2.12.11"
targetJvm := "jvm-1.8"

lazy val allResolvers = resolvers ++= Seq(
  Resolver.bintrayRepo("hmrc", "releases"),
  Resolver.jcenterRepo
)

lazy val CdsComponentTest = config("component") extend Test

val testConfig = Seq(CdsComponentTest, Test)

def forkedJvmPerTestConfig(tests: Seq[TestDefinition], packages: String*): Seq[Group] =
  tests.groupBy(_.name.takeWhile(_ != '.')).filter(packageAndTests => packages contains packageAndTests._1) map {
    case (packg, theTests) =>
      Group(packg, theTests, SubProcess(ForkOptions()))
  } toSeq

lazy val testAll = TaskKey[Unit]("test-all")
lazy val allTest = Seq(testAll := (test in CdsComponentTest).dependsOn(test in Test).value)

lazy val microservice = (project in file("."))
  .enablePlugins(PlayScala)
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
  .enablePlugins(SbtDistributablesPlugin)
  .enablePlugins(SbtArtifactory)
  .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
  .configs(testConfig: _*)
  .settings(
    commonSettings,
    unitTestSettings,
    componentTestSettings,
    playPublishingSettings,
    allTest,
    scoverageSettings,
    allResolvers
  )
  .settings(majorVersion := 0)

lazy val unitTestSettings =
  inConfig(Test)(Defaults.testTasks) ++
    Seq(
      testOptions in Test := Seq(Tests.Filter(unitTestFilter)),
      unmanagedSourceDirectories in Test := Seq((baseDirectory in Test).value / "test"),
      addTestReportOption(Test, "test-reports")
    )

lazy val componentTestSettings =
  inConfig(CdsComponentTest)(Defaults.testTasks) ++
    Seq(
      testOptions in CdsComponentTest := Seq(Tests.Filter(componentTestFilter)),
      fork in CdsComponentTest := false,
      parallelExecution in CdsComponentTest := false,
      addTestReportOption(CdsComponentTest, "comp-test-reports"),
      testGrouping in CdsComponentTest := forkedJvmPerTestConfig((definedTests in Test).value, "component")
    )

lazy val commonSettings: Seq[Setting[_]] = publishingSettings ++ gitStampSettings

lazy val playPublishingSettings: Seq[sbt.Setting[_]] = Seq(credentials += SbtCredentials) ++
  publishAllArtefacts

lazy val scoverageSettings: Seq[Setting[_]] = Seq(
  coverageExcludedPackages := List(
    "<empty>",
    ".*(Reverse|Routes).*",
    "com.kenshoo.play.metrics.*",
    ".*definition.*",
    "uk.gov.hmrc.BuildInfo",
    "views.*",
    "uk.gov.hmrc.customs.notificationpushretry.config.*"
  ).mkString(";"),
  coverageMinimum := 100,
  coverageFailOnMinimum := true,
  coverageHighlighting := true,
  parallelExecution in Test := false
)

def componentTestFilter(name: String): Boolean = name startsWith "component"
def unitTestFilter(name: String): Boolean = name startsWith "unit"

scalastyleConfig := baseDirectory.value / "project" / "scalastyle-config.xml"

val compileDependencies = Seq(customsApiCommon)
val testDependencies = Seq(scalaTestPlusPlay, wireMock, mockito, customsApiCommonTests)
libraryDependencies ++= compileDependencies ++ testDependencies

unmanagedResourceDirectories in Compile += baseDirectory.value / "public"
(managedClasspath in Runtime) += (packageBin in Assets).value

// Task to create a ZIP file containing all xsds for each version, under the version directory
val zipXsds = taskKey[Pipeline.Stage]("Zips up all notification push retry XSDs")

zipXsds := { mappings: Seq[PathMapping] =>
  val targetDir = WebKeys.webTarget.value / "zip"
  val zipFiles: Iterable[java.io.File] =
    ((resourceDirectory in Assets).value / "api" / "conf")
      .listFiles
      .filter(_.isDirectory)
      .map { dir =>
        val xsdPaths = Path.allSubpaths(dir / "schemas")
        val exampleMessagesFilter = new SimpleFileFilter(_.getPath.contains("/example_messages/"))
        val exampleMessagesPaths = Path.selectSubpaths(dir / "examples", exampleMessagesFilter)
        val zipFile = targetDir / "api" / "conf" / dir.getName / "customs-notification-push-retry-schemas.zip"
        IO.zip(xsdPaths ++ exampleMessagesPaths, zipFile)
        println(s"Created zip $zipFile")
        zipFile
      }
  zipFiles.pair(Path.relativeTo(targetDir)) ++ mappings
}

pipelineStages := Seq(zipXsds)

evictionWarningOptions in update := EvictionWarningOptions.default.withWarnTransitiveEvictions(false)
