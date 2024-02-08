lazy val root = (project in file("."))
  .settings(
    name := "sbt-yamory",
    sbtPlugin := true,
    scalacOptions ++= Seq("-Xfatal-warnings", "-Xlint"),
    addDependencyTreePlugin,
    console / initialCommands := "import io.github.kijuky.sbt.plugins.yamory._"
  )
  // test
  .settings(
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.2.18",
      "org.scalatest" %% "scalatest" % "3.2.15"
    ).map(_ % Test)
  )
  // scripted
  .enablePlugins(ScriptedPlugin)
  .settings(
    scriptedLaunchOpts ++=
      Seq("-Xmx1024M", s"-Dplugin.version=${version.value}")
  )

// publish settings
inThisBuild(
  Seq(
    organization := "io.github.kijuky",
    homepage := Some(url("https://github.com/kijuky/sbt-yamory")),
    licenses := Seq(
      "Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")
    ),
    developers := List(
      Developer(
        "kijuky",
        "Kizuki YASUE",
        "ikuzik@gmail.com",
        url("https://github.com/kijuky")
      )
    ),
    versionScheme := Some("early-semver"),
    sonatypeCredentialHost := "s01.oss.sonatype.org",
    sonatypeRepository := "https://s01.oss.sonatype.org/service/local"
  )
)
