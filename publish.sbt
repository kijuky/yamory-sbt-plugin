ThisBuild / organization := "io.github.kijuky"
ThisBuild / organizationName := "sbt-yamory-plugin"
ThisBuild / organizationHomepage := Some(url("https://github.com/kijuky/yamory-sbt-plugin"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/kijuky/yamory-sbt-plugin"),
    "scm:git@github.com:kijuky/yamory-sbt-plugin.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id = "kijuky",
    name = "Kizuki YASUE",
    email = "ikuzik@gmail.com",
    url = url("https://github.com/kijuky")
  )
)

ThisBuild / description := "This sbt-plugin integrate yamory scan to your projects."
ThisBuild / licenses := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / homepage := Some(url("https://github.com/kijuky/yamory-sbt-plugin"))

// Remove all additional repository other than Maven Central from POM
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at s"${nexus}content/repositories/snapshots")
  else Some("releases" at s"${nexus}service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true
