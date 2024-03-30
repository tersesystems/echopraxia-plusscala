import sbt.Keys._
import commandmatrix._
import commandmatrix.extra._

val echopraxiaVersion            = "3.1.2"
val scalatestVersion             = "3.2.18"
val logbackClassicVersion        = "1.5.3"
val logstashVersion              = "7.4"
val refinedVersion               = "0.11.1"
val scalaJavaVersion             = "1.0.2"
val enumeratumVersion            = "1.7.3"
val zjsonPatchVersion            = "0.4.16"
val sourceCodeVersion            = "0.3.1"
val scalaCollectionCompatVersion = "2.11.0"

val scala3                       = "3.4.0"
val scala213                     = "2.13.13"
val scala212                     = "2.12.19"

val scalaVersions = List(scala3, scala213, scala212)
val ideScala = scala213

// https://stackoverflow.com/questions/74340971/how-to-configure-intellij-to-manage-different-scala-versions
val only1JvmScalaInIde = MatrixAction
  .ForPlatforms(VirtualAxis.jvm)
  .Configure(_.settings(ideSkipProject := (scalaVersion.value != ideScala)))

initialize := {
  val _        = initialize.value // run the previous initialization
  val required = "17"
  val current  = sys.props("java.specification.version")
  assert(current >= required, s"Unsupported JDK: java.specification.version $current != $required")
}

inThisBuild(
  Seq(
    // sbt-commandmatrix
    commands ++= CrossCommand.single(
      "test",
      matrices = Seq(root),
      dimensions = Seq(
        Dimension.scala("2.13", fullFor3 = true),
        Dimension.platform()
      )
    ),
    semanticdbEnabled := true, // enable SemanticDB
    semanticdbVersion := scalafixSemanticdb.revision, // only required for Scala 2.x
  )
)

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
ThisBuild / Compile / scalacOptions ++= optimizeInline

ThisBuild / Test / parallelExecution := false

Global / concurrentRestrictions += Tags.limit(Tags.Test, 1)
Global / excludeLintKeys += ideSkipProject

lazy val api = (projectMatrix in file("api"))
  .settings(
    name := "api",
    scalacOptions := scalacOptionsVersion(scalaVersion.value),
    // Scala 3 doesn't need scala-reflect
    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((3, _)) => Seq.empty
        case other => Seq("org.scala-lang" % "scala-reflect" % scalaVersion.value)
      }
    },
    //
    semanticdbEnabled := true, // enable SemanticDB
    //
    libraryDependencies += "com.tersesystems.echopraxia" % "api"                % echopraxiaVersion,
    libraryDependencies += "org.scala-lang.modules"     %% "scala-java8-compat" % scalaJavaVersion,
    libraryDependencies ++= compatLibraries(scalaVersion.value),
    // tests
    libraryDependencies += "eu.timepit"    %% "refined"   % refinedVersion   % Test,
    libraryDependencies += "org.scalatest" %% "scalatest" % scalatestVersion % Test,
    // use logstash for testing
    libraryDependencies += "com.tersesystems.echopraxia" % "logstash"                 % echopraxiaVersion     % Test,
    libraryDependencies += "ch.qos.logback"              % "logback-classic"          % logbackClassicVersion % Test,
    libraryDependencies += "net.logstash.logback"        % "logstash-logback-encoder" % logstashVersion       % Test
  )
  .jvmPlatform(scalaVersions = scalaVersions)

lazy val generic = (projectMatrix in file("generic"))
  .settings(
    name := "generic",
    scalacOptions := scalacOptionsVersion(scalaVersion.value),
    //
    libraryDependencies += {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((3, _)) => "com.softwaremill.magnolia1_3" %% "magnolia" % "1.3.4"
        case other => "com.softwaremill.magnolia1_2" %% "magnolia" % "1.1.8"
      }
    },
    //
    libraryDependencies += "com.tersesystems.echopraxia" % "logstash"                 % echopraxiaVersion     % Test,
    libraryDependencies += "org.scalatest"              %% "scalatest"                % scalatestVersion      % Test,
    libraryDependencies += "ch.qos.logback"              % "logback-classic"          % logbackClassicVersion % Test,
    libraryDependencies += "net.logstash.logback"        % "logstash-logback-encoder" % logstashVersion       % Test
  )
  .dependsOn(api, logger % "test")
  .jvmPlatform(scalaVersions = scalaVersions)

lazy val logger = (projectMatrix in file("logger"))
  .settings(
    name := "logger",
    scalacOptions := scalacOptionsVersion(scalaVersion.value),
    libraryDependencies += "com.lihaoyi" %% "sourcecode" % sourceCodeVersion,
    //
    libraryDependencies += "com.tersesystems.echopraxia" % "logstash"                 % echopraxiaVersion     % Test,
    libraryDependencies += "org.scalatest"              %% "scalatest"                % scalatestVersion      % Test,
    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((3, _)) => Seq.empty
        case other =>
          Seq(
           "eu.timepit"                 %% "refined"                  % refinedVersion        % Test
          )
      }
    },
    libraryDependencies += "com.beachape"               %% "enumeratum"               % enumeratumVersion     % Test,
    libraryDependencies += "ch.qos.logback"              % "logback-classic"          % logbackClassicVersion % Test,
    libraryDependencies += "net.logstash.logback"        % "logstash-logback-encoder" % logstashVersion       % Test
  )
  .dependsOn(api % "compile->compile;test->compile")
  .jvmPlatform(scalaVersions = scalaVersions)

lazy val asyncLogger = (projectMatrix in file("async"))
  .settings(
    name := "async-logger",
    scalacOptions := scalacOptionsVersion(scalaVersion.value),
    //
    libraryDependencies += "com.tersesystems.echopraxia" % "logstash"  % echopraxiaVersion % Test,
    libraryDependencies += "org.scalatest"              %% "scalatest" % scalatestVersion  % Test
  )
  .dependsOn(api % "compile->compile;test->compile")
  .jvmPlatform(scalaVersions = scalaVersions)

lazy val flowLogger = (projectMatrix in file("flow"))
  .settings(
    name := "flow-logger",
    scalacOptions := scalacOptionsVersion(scalaVersion.value),
    //
    libraryDependencies += "com.tersesystems.echopraxia" % "logstash"                 % echopraxiaVersion     % Test,
    libraryDependencies += "org.scalatest"              %% "scalatest"                % scalatestVersion      % Test,
    libraryDependencies += "ch.qos.logback"              % "logback-classic"          % logbackClassicVersion % Test,
    libraryDependencies += "net.logstash.logback"        % "logstash-logback-encoder" % logstashVersion       % Test
  )
  .dependsOn(api % "compile->compile;test->compile")
  .jvmPlatform(scalaVersions = scalaVersions)

lazy val nameOfLogger = (projectMatrix in file("nameof"))
  .settings(
    name := "nameof",
    scalacOptions := scalacOptionsVersion(scalaVersion.value),
    //
    libraryDependencies += "com.tersesystems.echopraxia" % "logstash"                 % echopraxiaVersion     % Test,
    libraryDependencies += "org.scalatest"              %% "scalatest"                % scalatestVersion      % Test,
    libraryDependencies += "ch.qos.logback"              % "logback-classic"          % logbackClassicVersion % Test,
    libraryDependencies += "net.logstash.logback"        % "logstash-logback-encoder" % logstashVersion       % Test
  )
  .dependsOn(api % "compile->compile;test->compile")
  .jvmPlatform(scalaVersions = List(scala213, scala212)) // disable scala 3 for now

// don't include dump for now
//lazy val dump = (project in file("dump"))
//  .settings(
//    name := "dump",
//    //
//    libraryDependencies += "com.tersesystems.echopraxia" % "logstash"  % echopraxiaVersion % Test,
//    libraryDependencies += "org.scalatest"              %% "scalatest" % "3.2.12"      % Test
//  ).dependsOn(api % "compile->compile;test->compile")

lazy val diff = (projectMatrix in file("diff"))
  .settings(
    name := "diff",
    scalacOptions := scalacOptionsVersion(scalaVersion.value),
    //
    // https://mvnrepository.com/artifact/com.flipkart.zjsonpatch/zjsonpatch
    libraryDependencies += "com.flipkart.zjsonpatch"     % "zjsonpatch" % zjsonPatchVersion,
    libraryDependencies += "com.tersesystems.echopraxia" % "jackson"    % echopraxiaVersion,
    //
    libraryDependencies += "com.tersesystems.echopraxia" % "logstash"                 % echopraxiaVersion     % Test,
    libraryDependencies += "org.scalatest"              %% "scalatest"                % scalatestVersion      % Test,
    libraryDependencies += "ch.qos.logback"              % "logback-classic"          % logbackClassicVersion % Test,
    libraryDependencies += "net.logstash.logback"        % "logstash-logback-encoder" % logstashVersion       % Test
  )
  .dependsOn(api % "compile->compile;test->compile")
  .jvmPlatform(scalaVersions = scalaVersions)

lazy val traceLogger = (projectMatrix in file("trace"))
  .settings(
    name := "trace-logger",
    scalacOptions := scalacOptionsVersion(scalaVersion.value),
    //
    libraryDependencies += "com.lihaoyi" %% "sourcecode" % sourceCodeVersion,
    //
    libraryDependencies += "com.tersesystems.echopraxia" % "logstash"                 % echopraxiaVersion     % Test,
    libraryDependencies += "ch.qos.logback"              % "logback-classic"          % logbackClassicVersion % Test,
    libraryDependencies += "net.logstash.logback"        % "logstash-logback-encoder" % logstashVersion       % Test,
    libraryDependencies += "org.scalatest"              %% "scalatest"                % scalatestVersion      % Test
  )
  .dependsOn(api % "compile->compile;test->compile")
  .jvmPlatform(scalaVersions = scalaVersions)

lazy val benchmarks = (projectMatrix in file("benchmarks"))
  .enablePlugins(JmhPlugin)
  .settings(
    scalacOptions := scalacOptionsVersion(scalaVersion.value),
    Compile / doc / sources                             := Seq.empty,
    Compile / packageDoc / publishArtifact              := false,
    publishArtifact                                     := false,
    publish / skip                                      := true,
    libraryDependencies += "com.tersesystems.echopraxia" % "logstash" % echopraxiaVersion
  )
  .dependsOn(api, logger, asyncLogger, flowLogger, traceLogger)
  .jvmPlatform(scalaVersions = scalaVersions)

val refs = api.projectRefs ++
           generic.projectRefs ++
           logger.projectRefs ++
           asyncLogger.projectRefs ++
           nameOfLogger.projectRefs ++
           diff.projectRefs ++
           flowLogger.projectRefs ++
           traceLogger.projectRefs ++
           benchmarks.projectRefs

lazy val root = (projectMatrix in file("."))
  .settings(
    name                                   := "echopraxia-plusscala",
    Compile / doc / sources                := Seq.empty,
    Compile / packageDoc / publishArtifact := false,
    publishArtifact                        := false,
    publish / skip                         := true
  ).aggregate(refs: _*)
  .jvmPlatform(scalaVersions = scalaVersions)

def compatLibraries(scalaVersion: String): Seq[ModuleID] = {
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, n)) if n == 12 =>
      // only need collection compat in 2.12
      Seq("org.scala-lang.modules" %% "scala-collection-compat" % scalaCollectionCompatVersion)
    case other =>
      Nil
  }
}

def scalacOptionsVersion(scalaVersion: String): Seq[String] = {
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((3, _)) =>
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
        "-Wunused:all",
        "-release",
        "8",
        "-explain"
      )
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
        "-Wunused",
        "-release",
        "8",
        "-Vimplicits",
        "-Vtype-diffs",
        "-Xsource:3-cross"
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
        "-Yno-adapted-args",
        "-Ywarn-unused-import", // Scala 2.x only, required by `RemoveUnused`
        "-release",
        "8",
      )
  }
}

lazy val optimizeInline = Seq(
  "-opt:l:inline",
  "-opt-inline-from:com.tersesystems.echopraxia.plusscala.**",
  "-opt-warnings:any-inline-failed"
)
