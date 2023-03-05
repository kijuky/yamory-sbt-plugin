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
    val yamoryOpenSystem = settingKey[Option[String]]("YAMORY_OPEN_SYSTEM")
    val yamoryDistributed = settingKey[Option[String]]("YAMORY_DISTRIBUTED")

    // for scala
    val yamorySbtScriptUrl = settingKey[String]("https://yamory/script/...")
    val yamory =
      taskKey[Unit]("A task that is run yamory scan for scala project.")

    // for scala.js using npm
    val yamoryNpmScriptUrl = settingKey[String]("https://yamory/script/...")
    val yamoryNpmManifest = settingKey[String]("path to package.json")
    val yamoryNpm =
      taskKey[Unit]("A task that is run yamory scan for scala.js project.")
  }

  import autoImport.*

  override lazy val projectSettings: Seq[Def.Setting[?]] = Seq(
    // for yamory
    yamoryProjectGroupKey := sys.env.getOrElse("PROJECT_GROUP_KEY", ""),
    yamoryApiKey := sys.env.getOrElse("YAMORY_API_KEY", ""),
    yamoryOpenSystem := sys.env.get("YAMORY_OPEN_SYSTEM"),
    yamoryDistributed := sys.env.get("YAMORY_DISTRIBUTED"),

    // for scala
    yamorySbtScriptUrl := "",
    yamory := yamoryTask.value,

    // for scala.js using npm
    yamoryNpmScriptUrl := "",
    yamoryNpmManifest := "./package.json",
    yamoryNpm := yamoryNpmTask.value
  )

  override lazy val buildSettings: Seq[Def.Setting[?]] = Nil

  override lazy val globalSettings: Seq[Def.Setting[?]] = Nil

  private lazy val yamoryTask = Def.task {
    val projectGroupKey = yamoryProjectGroupKey.value
    val yamoryApiKey = autoImport.yamoryApiKey.value
    val yamorySbtScriptUrl = autoImport.yamorySbtScriptUrl.value
    val yamoryOpenSystem = autoImport.yamoryOpenSystem.value
    val yamoryDistributed = autoImport.yamoryDistributed.value
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
        val extraEnv = Seq(
          "PROJECT_GROUP_KEY" -> projectGroupKey,
          "YAMORY_API_KEY" -> yamoryApiKey
        ) ++
          yamoryOpenSystem.map("YAMORY_OPEN_SYSTEM".->) ++
          yamoryDistributed.map("YAMORY_DISTRIBUTED".->)
        dependenciesFile #> Process(
          Seq("bash", "-c", yamorySbtScriptFilePath),
          None,
          extraEnv*
        ) !
      } finally {
        Seq(dependenciesFile, yamorySbtScriptFile).foreach(_.delete())
      }
    }
  }

  private lazy val yamoryNpmTask = Def.task {
    val projectGroupKey = yamoryProjectGroupKey.value
    val yamoryApiKey = autoImport.yamoryApiKey.value
    val yamoryNpmScriptUrl = autoImport.yamoryNpmScriptUrl.value
    val yamoryNpmManifest = autoImport.yamoryNpmManifest.value
    val yamoryOpenSystem = autoImport.yamoryOpenSystem.value
    val yamoryDistributed = autoImport.yamoryDistributed.value

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
        val extraEnv = Seq(
          "PROJECT_GROUP_KEY" -> projectGroupKey,
          "YAMORY_API_KEY" -> yamoryApiKey
        ) ++
          yamoryOpenSystem.map("YAMORY_OPEN_SYSTEM".->) ++
          yamoryDistributed.map("YAMORY_DISTRIBUTED".->)
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
          extraEnv*
        ) !
      } finally {
        yamoryNpmScriptFile.delete()
      }
    }
  }
}
