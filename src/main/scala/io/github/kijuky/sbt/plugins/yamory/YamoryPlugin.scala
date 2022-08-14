package io.github.kijuky.sbt.plugins.yamory

import sbt.*
import sbt.Keys.*
import sbt.io.IO
import sbt.plugins.*
import sbt.plugins.MiniDependencyTreeKeys.*

import java.nio.file.*
import scala.language.*
import scala.sys.process.*

object YamoryPlugin extends AutoPlugin {

  override def trigger = allRequirements

  override def requires = JvmPlugin && MiniDependencyTreePlugin

  object autoImport {
    // for yamory
    val yamoryProjectGroupKey = settingKey[String]("PROJECT_GROUP_KEY")
    val yamoryApiKey = settingKey[String]("YAMORY_API_KEY")

    // for scala
    val yamorySbtScriptUrl = settingKey[String]("https://yamory/script/...")
    val yamory =
      taskKey[Unit]("A task that is run yamory scan for scala project.")

    // for scala.js using npm
    val yamoryNpmScriptUrl = settingKey[String]("https://yamory/script/...")
    val yamoryNpmManifest = settingKey[String]("path to package.json")
    val yamoryNpm =
      taskKey[Unit]("A task that is run yamory scan for scala.js project.")

    // for scala.js using yarn
    @deprecated("instead yamoryNpmScriptUrl", "4.1.0")
    val yamoryYarnScriptUrl = settingKey[String]("https://yamory/script/...")
    @deprecated("instead yamoryNpmManifest", "4.1.0")
    val yamoryYarnManifest = settingKey[String]("path to package.json")
    @deprecated("instead yamoryNpm", "4.1.0")
    val yamoryYarn =
      taskKey[Unit]("A task that is run yamory scan for scala.js project.")
  }

  import autoImport.*

  override lazy val projectSettings = Seq(
    // for yamory
    yamoryProjectGroupKey := sys.env.getOrElse("PROJECT_GROUP_KEY", ""),
    yamoryApiKey := sys.env.getOrElse("YAMORY_API_KEY", ""),

    // for scala
    yamorySbtScriptUrl := "",
    yamory := yamoryTask.value,

    // for scala.js using npm
    yamoryNpmScriptUrl := "",
    yamoryNpmManifest := "./package.json",
    yamoryNpm := yamoryNpmTask.value,

    // for scala.js using yarn
    yamoryYarnScriptUrl := "",
    yamoryYarnManifest := "./package.json",
    yamoryYarn := yamoryYarnTask.value
  )

  override lazy val buildSettings = Seq()

  override lazy val globalSettings = Seq()

  lazy val yamoryTask = Def.task {
    val projectGroupKey = yamoryProjectGroupKey.value
    val yamoryApiKey = autoImport.yamoryApiKey.value
    val yamorySbtScriptUrl = autoImport.yamorySbtScriptUrl.value
    val oldAsciiGraphWidth = (Compile / asciiGraphWidth).value
    Compile / asciiGraphWidth := 10000
    val dependencies = (Compile / dependencyTree / asString).value
    Compile / asciiGraphWidth := oldAsciiGraphWidth

    require(
      projectGroupKey.nonEmpty,
      "PROJECT_GROUP_KEY is empty. set 'yamoryProjectGroupKey' setting."
    )
    require(
      yamoryApiKey.nonEmpty,
      "YAMORY_API_KEY is empty. set 'yamoryApiKey' setting."
    )

    if (yamorySbtScriptUrl.nonEmpty) {
      val dependenciesFile = Files.createTempFile("sbt", ".txt").toFile
      val yamorySbtScriptFile = Files.createTempFile("sbt", ".sh").toFile
      Seq(dependenciesFile, yamorySbtScriptFile).foreach(_.deleteOnExit())
      try {
        val dependenciesLog =
          dependencies.split("\n").map("[info] ".+).mkString("\n")
        IO.write(dependenciesFile, dependenciesLog, IO.utf8)
        url(yamorySbtScriptUrl) #> yamorySbtScriptFile !
        val yamorySbtScriptFilePath = yamorySbtScriptFile.getAbsolutePath
        assume(
          yamorySbtScriptFile.setExecutable(true),
          s"$yamorySbtScriptFile is not executable."
        )
        dependenciesFile #> Process(
          Seq("bash", "-c", yamorySbtScriptFilePath),
          None,
          "PROJECT_GROUP_KEY" -> projectGroupKey,
          "YAMORY_API_KEY" -> yamoryApiKey
        ) !
      } finally {
        Seq(dependenciesFile, yamorySbtScriptFile).foreach(_.delete())
      }
    }
  }

  lazy val yamoryNpmTask = Def.task {
    val projectGroupKey = yamoryProjectGroupKey.value
    val yamoryApiKey = autoImport.yamoryApiKey.value
    val yamoryNpmScriptUrl = autoImport.yamoryNpmScriptUrl.value
    val yamoryNpmManifest = autoImport.yamoryNpmManifest.value

    require(
      projectGroupKey.nonEmpty,
      "PROJECT_GROUP_KEY is empty. set 'yamoryProjectGroupKey' setting."
    )
    require(
      yamoryApiKey.nonEmpty,
      "YAMORY_API_KEY is empty. set 'yamoryApiKey' setting."
    )
    require(
      yamoryNpmManifest.nonEmpty,
      "yamory npm manifest is empty. set 'yamoryNpmManifest' setting."
    )

    if (yamoryNpmScriptUrl.nonEmpty) {
      val yamoryNpmScriptFile = Files.createTempFile("sbt", ".sh").toFile
      yamoryNpmScriptFile.deleteOnExit()
      try {
        url(yamoryNpmScriptUrl) #> yamoryNpmScriptFile !
        val yamoryNpmScriptFilePath = yamoryNpmScriptFile.getAbsolutePath
        assume(
          yamoryNpmScriptFile.setExecutable(true),
          s"$yamoryNpmScriptFile is not executable."
        )
        Process(
          Seq(
            "bash",
            "-c",
            yamoryNpmScriptFilePath,
            "--",
            "--manifest",
            yamoryNpmManifest
          ),
          None,
          "PROJECT_GROUP_KEY" -> projectGroupKey,
          "YAMORY_API_KEY" -> yamoryApiKey
        ) !
      } finally {
        yamoryNpmScriptFile.delete()
      }
    }
  }

  lazy val yamoryYarnTask = Def.task {
    val projectGroupKey = yamoryProjectGroupKey.value
    val yamoryApiKey = autoImport.yamoryApiKey.value
    val yamoryYarnScriptUrl = autoImport.yamoryYarnScriptUrl.value
    val yamoryYarnManifest = autoImport.yamoryYarnManifest.value

    require(
      projectGroupKey.nonEmpty,
      "PROJECT_GROUP_KEY is empty. set 'yamoryProjectGroupKey' setting."
    )
    require(
      yamoryApiKey.nonEmpty,
      "YAMORY_API_KEY is empty. set 'yamoryApiKey' setting."
    )
    require(
      yamoryYarnManifest.nonEmpty,
      "yamory yarn manifest is empty. set 'yamoryYarnManifest' setting."
    )

    if (yamoryYarnScriptUrl.nonEmpty) {
      val yamoryYarnScriptFile = Files.createTempFile("sbt", ".sh").toFile
      yamoryYarnScriptFile.deleteOnExit()
      try {
        url(yamoryYarnScriptUrl) #> yamoryYarnScriptFile !
        val yamoryYarnScriptFilePath = yamoryYarnScriptFile.getAbsolutePath
        assume(
          yamoryYarnScriptFile.setExecutable(true),
          s"$yamoryYarnScriptFile is not executable."
        )
        Process(
          Seq(
            "bash",
            "-c",
            yamoryYarnScriptFilePath,
            "--",
            "--manifest",
            yamoryYarnManifest
          ),
          None,
          "PROJECT_GROUP_KEY" -> projectGroupKey,
          "YAMORY_API_KEY" -> yamoryApiKey
        ) !
      } finally {
        yamoryYarnScriptFile.delete()
      }
    }
  }
}
