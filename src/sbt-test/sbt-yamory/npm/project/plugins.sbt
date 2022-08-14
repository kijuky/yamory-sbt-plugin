System.getProperty("plugin.version") match {
  case null =>
    throw new RuntimeException(
      """|The system property 'plugin.version' is not defined.
         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin
    )
  case pluginVersion =>
    addSbtPlugin("io.github.kijuky" % "sbt-yamory" % pluginVersion)
}

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.10.1")
addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.20.0")
