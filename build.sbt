
name := """yamory-sbt-plugin"""
version := "1.0.0-SNAPSHOT"
versionScheme := Some("early-semver")

sbtPlugin := true

addDependencyTreePlugin

// ScalaTest
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.1" % "test"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"

console / initialCommands := """import io.github.kijuky.sbt.plugins.yamory._"""

enablePlugins(ScriptedPlugin)
// set up 'scripted; sbt plugin for testing sbt plugins
scriptedLaunchOpts ++=
  Seq("-Xmx1024M", s"-Dplugin.version=${version.value}")
