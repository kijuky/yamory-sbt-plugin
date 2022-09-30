name := "sbt-yamory"

sbtPlugin := true

scalacOptions ++= Seq("-Xfatal-warnings", "-Xlint")

addDependencyTreePlugin

console / initialCommands := "import io.github.kijuky.sbt.plugins.yamory._"

// ScalaTest
libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.14" % Test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.14" % Test

// Scripted
enablePlugins(ScriptedPlugin)
// set up 'scripted; sbt plugin for testing sbt plugins
scriptedLaunchOpts ++=
  Seq("-Xmx1024M", s"-Dplugin.version=${version.value}")

// publish settings
inThisBuild(
  Seq(
    organization := "io.github.kijuky",
    homepage := Some(url("https://github.com/kijuky/sbt-yamory")),
    licenses := Seq(
      "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
    ),
    developers := List(
      Developer(
        "kijuky",
        "Kizuki YASUE",
        "ikuzik@gmail.com",
        url("https://github.com/kijuky")
      )
    ),
    versionScheme := Some("early-semver")
  )
)

sonatypeCredentialHost := "s01.oss.sonatype.org"
