import sbt.Keys._

val echopraxiaVersion = "2.0.1"

val scala213      = "2.13.8"
val scala212      = "2.12.14"
val scalaVersions = Seq(scala212, scala213)

initialize := {
  val _        = initialize.value // run the previous initialization
  val required = "11"
  val current  = sys.props("java.specification.version")
  assert(current >= required, s"Unsupported JDK: java.specification.version $current != $required")
}

ThisBuild / organization := "com.tersesystems.echopraxia.plusscala"
ThisBuild / homepage     := Some(url("https://github.com/tersesystems/echopraxia-plusscala"))

ThisBuild / startYear := Some(2021)
ThisBuild / licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/tersesystems/echopraxia-plusscala"),
    "scm:git@github.com:tersesystems/echopraxia-plusscala.git"
  )
)

ThisBuild / versionScheme := Some("early-semver")

ThisBuild / resolvers += Resolver.mavenLocal
ThisBuild / scalaVersion       := scala212
ThisBuild / crossScalaVersions := scalaVersions
ThisBuild / scalacOptions      := scalacOptionsVersion(scalaVersion.value)

ThisBuild / Compile / scalacOptions ++= optimizeInline

ThisBuild / Test / parallelExecution := false
Global / concurrentRestrictions += Tags.limit(Tags.Test, 1)

lazy val api = (project in file("api"))
  .settings(
    name := "api",
    //
    libraryDependencies += "com.softwaremill.magnolia1_2" %% "magnolia" % "1.1.2",
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    libraryDependencies += "com.tersesystems.echopraxia" % "api"                % echopraxiaVersion,
    libraryDependencies += "org.scala-lang.modules"     %% "scala-java8-compat" % "1.0.2",
    libraryDependencies ++= compatLibraries(scalaVersion.value)
  )

lazy val logger = (project in file("logger"))
  .settings(
    name := "logger",
    //
    run / fork := true,
    //
    libraryDependencies += "com.lihaoyi" %% "sourcecode" % "0.2.8",
    //
    libraryDependencies += "com.tersesystems.echopraxia" % "logstash"  % echopraxiaVersion % Test,
    libraryDependencies += "org.scalatest"              %% "scalatest" % "3.2.12"      % Test
  )
  .dependsOn(api % "compile->compile;test->compile")

lazy val asyncLogger = (project in file("async"))
  .settings(
    name := "async-logger",
    //
    libraryDependencies += "com.lihaoyi" %% "sourcecode" % "0.2.8",
    //
    libraryDependencies += "com.tersesystems.echopraxia" % "logstash"  % echopraxiaVersion % Test,
    libraryDependencies += "org.scalatest"              %% "scalatest" % "3.2.12"      % Test
  )
  .dependsOn(api % "compile->compile;test->compile")

lazy val traceLogger = (project in file("trace"))
  .settings(
  name := "trace-logger",

  libraryDependencies += "com.tersesystems.echopraxia" % "logstash"  % echopraxiaVersion % Test,
  libraryDependencies += "org.scalatest"              %% "scalatest" % "3.2.12"      % Test
).dependsOn(api % "compile->compile;test->compile")

lazy val verboseTraceLogger = (project in file("verbose-trace"))
  .settings(
    name := "verbose-trace-logger",

    libraryDependencies += "com.lihaoyi" %% "sourcecode" % "0.2.8",
    //
    libraryDependencies += "com.tersesystems.echopraxia" % "logstash"  % echopraxiaVersion % Test,
    libraryDependencies += "org.scalatest"              %% "scalatest" % "3.2.12"      % Test
  )
  .dependsOn(api % "compile->compile;test->compile")


lazy val benchmarks = (project in file("benchmarks")).enablePlugins(JmhPlugin).settings(
  Compile / doc / sources                := Seq.empty,
  Compile / packageDoc / publishArtifact := false,
  publishArtifact                        := false,
  publish / skip                         := true,

  libraryDependencies += "com.tersesystems.echopraxia" % "logstash"  % echopraxiaVersion
).dependsOn(api, logger, asyncLogger, traceLogger, verboseTraceLogger)

lazy val root = (project in file("."))
  .settings(
    Compile / doc / sources                := Seq.empty,
    Compile / packageDoc / publishArtifact := false,
    publishArtifact                        := false,
    publish / skip                         := true
  )
  .aggregate(api, logger, asyncLogger, traceLogger, verboseTraceLogger, benchmarks)

def compatLibraries(scalaVersion: String): Seq[ModuleID] = {
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, n)) if n == 12 =>
      // only need collection compat in 2.12
      Seq("org.scala-lang.modules"     %% "scala-collection-compat" % "2.7.0")
    case other =>
      Nil
  }
}

def scalacOptionsVersion(scalaVersion: String): Seq[String] = {
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, n)) if n >= 13 =>
      Seq(
        "-unchecked",
        "-deprecation",
        "-feature",
        "-encoding",
        "UTF-8",
        "-language:implicitConversions",
        "-language:higherKinds",
        "-language:existentials",
        "-language:postfixOps",
        "-Xlint",
        "-Ywarn-dead-code",
        "-Yrangepos",
        "-Xsource:2.13",
        "-release",
        "8"
      )
    case Some((2, n)) if n == 12 =>
      Seq(
        "-unchecked",
        "-deprecation",
        "-feature",
        "-encoding",
        "UTF-8",
        "-language:implicitConversions",
        "-language:higherKinds",
        "-language:existentials",
        "-language:postfixOps",
        "-Xlint",
        "-Ywarn-dead-code",
        "-Yrangepos",
        "-Xsource:2.12",
        "-Yno-adapted-args",
        "-release",
        "8"
      )
  }
}

lazy val optimizeInline = Seq(
  "-opt:l:inline",
  "-opt-inline-from:com.tersesystems.echopraxia.plusscala.**",
  "-opt-warnings:any-inline-failed"
)
