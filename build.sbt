name := "yamory-sbt-plugin"

sbtPlugin := true

addDependencyTreePlugin

// ScalaTest
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.1" % Test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % Test

console / initialCommands := "import io.github.kijuky.sbt.plugins.yamory._"

enablePlugins(ScriptedPlugin)
// set up 'scripted; sbt plugin for testing sbt plugins
scriptedLaunchOpts ++=
  Seq("-Xmx1024M", s"-Dplugin.version=${version.value}")

// publish settings

inThisBuild(Seq(
  organization := "io.github.kijuky",
  homepage := Some(url("https://github.com/kijuky/yamory-sbt-plugin")),
  licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  developers := List(
    Developer(
      "kijuky",
      "Kizuki YASUE",
      "ikuzik@gmail.com",
      url("https://github.com/kijuky")
    )
  ),
  versionScheme := Some("early-semver")
))

sonatypeCredentialHost := "s01.oss.sonatype.org"
