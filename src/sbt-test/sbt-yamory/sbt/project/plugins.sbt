System.getProperty("plugin.version") match {
  case null =>
    throw new RuntimeException(
      """|The system property 'plugin.version' is not defined.
         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin
    )
  case pluginVersion =>
    addSbtPlugin("io.github.kijuky" % "sbt-yamory" % pluginVersion)
}
