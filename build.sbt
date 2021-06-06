
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
