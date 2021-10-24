package io.github.kijuky.sbt.plugins.yamory

import sbt._
import sbt.io.IO
import sbt.plugins._
import sbt.plugins.MiniDependencyTreeKeys._

import java.nio.file._
import scala.language._
import scala.sys.process._

object YamoryPlugin extends AutoPlugin {

  override def trigger = allRequirements

  override def requires = JvmPlugin && MiniDependencyTreePlugin

  object autoImport {
    val yamoryProjectGroupKey = settingKey[String]("PROJECT_GROUP_KEY")
    val yamoryApiKey = settingKey[String]("YAMORY_API_KEY")
    val yamorySbtScriptUrl = settingKey[String]("https://yamory/script/...")
    val yamoryYarnScriptUrl = settingKey[String]("https://yamory/script/...")
    val yamoryYarnManifest = settingKey[String]("path to package.json")
    val yamory =
      taskKey[Unit]("A task that is run yamory scan for scala project.")
    val yamoryYarn =
      taskKey[Unit]("A task that is run yamory scan for scala.js project.")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    yamoryProjectGroupKey := "",
    yamoryApiKey := "",
    yamorySbtScriptUrl := "",
    yamoryYarnScriptUrl := "",
    yamoryYarnManifest := "./package.json",
    yamory := yamoryTask.value,
    yamoryYarn := yamoryYarnTask.value
  )

  override lazy val buildSettings = Seq()

  override lazy val globalSettings = Seq()

  lazy val yamoryTask = Def.task {
    val projectGroupKey = yamoryProjectGroupKey.value
    val yamoryApiKey = autoImport.yamoryApiKey.value
    val yamorySbtScriptUrl = autoImport.yamorySbtScriptUrl.value
    val dependencies = (Compile / dependencyTree / asString).value

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
