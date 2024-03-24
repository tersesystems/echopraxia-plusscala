addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")

// https://github.com/sbt/sbt-pgp
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.2.1")

// https://github.com/sbt/sbt-release
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.4.0")

// https://github.com/xerial/sbt-sonatype
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.10.0")

// https://github.com/sbt/sbt-jmh
addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.4.7")

// https://github.com/rtimush/sbt-updates
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.4")

// https://github.com/sbt/sbt-projectmatrix
addSbtPlugin("com.eed3si9n" % "sbt-projectmatrix" % "0.10.0")

// https://github.com/indoorvivants/sbt-commandmatrix
addSbtPlugin("com.indoorvivants" % "sbt-commandmatrix" % "0.0.5")

// https://github.com/JetBrains/sbt-ide-settings
addSbtPlugin("org.jetbrains" % "sbt-ide-settings" % "1.1.0")

// Must use bloop for scala 3
addSbtPlugin("ch.epfl.scala" % "sbt-bloop" % "1.5.15")

// scalafix
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.12.0")