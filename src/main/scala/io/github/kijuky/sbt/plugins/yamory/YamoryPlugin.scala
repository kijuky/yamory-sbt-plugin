package io.github.kijuky.sbt.plugins.yamory

import _root_.sbt.AutoPlugin
import _root_.sbt.Compile
import _root_.sbt.Def
import _root_.sbt.io.IO
import _root_.sbt.plugins.JvmPlugin
import _root_.sbt.plugins.MiniDependencyTreeKeys._
import _root_.sbt.plugins.MiniDependencyTreePlugin
import _root_.sbt.settingKey
import _root_.sbt.taskKey
import _root_.sbt.url

import java.nio.file.Files
import scala.language.postfixOps
import scala.sys.process.Process
import scala.sys.process._

object YamoryPlugin extends AutoPlugin {

  override def trigger = allRequirements

  override def requires = JvmPlugin && MiniDependencyTreePlugin

  object autoImport {
    val yamoryProjectGroupKey = settingKey[String]("PROJECT_GROUP_KEY")
    val yamoryApiKey = settingKey[String]("YAMORY_API_KEY")
    @deprecated("yamorySbtScriptUrl instead.", "1.1.0")
    val yamoryScriptUrl = settingKey[String]("https://yamory/script/...")
    val yamorySbtScriptUrl = settingKey[String]("https://yamory/script/...")
    val yamoryYarnScriptUrl = settingKey[String]("https://yamory/script/...")
    val yamoryYarnManifest = settingKey[String]("path to package.json")
    val yamory = taskKey[Unit]("A task that is run yamory scan for scala project.")
    val yamoryYarn = taskKey[Unit]("A task that is run yamory scan for scala.js project.")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    yamoryProjectGroupKey := "",
    yamoryApiKey := "",
    yamoryScriptUrl := "",
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
    val yamorySbtScriptUrl = autoImport.yamorySbtScriptUrl.value match {
      case x if x.isEmpty => autoImport.yamoryScriptUrl.value
      case x => x
    }

    require(projectGroupKey.nonEmpty, "PROJECT_GROUP_KEY is empty. set 'yamoryProjectGroupKey' setting.")
    require(yamoryApiKey.nonEmpty, "YAMORY_API_KEY is empty. set 'yamoryApiKey' setting.")
    require(yamorySbtScriptUrl.nonEmpty, "yamory sbt script url is empty. set 'yamorySbtScriptUrl' setting.")

    val dependenciesFile = Files.createTempFile("sbt", ".txt").toFile
    val yamorySbtScriptFile = Files.createTempFile("sbt", ".sh").toFile
    Seq(dependenciesFile, yamorySbtScriptFile).foreach(_.deleteOnExit())
    try {
      val dependencies = (Compile / dependencyTree / asString).value
      val dependenciesLog = dependencies.split("\n").map("[info] " + _).mkString("\n")
      IO.write(dependenciesFile, dependenciesLog, IO.utf8)
      url(yamorySbtScriptUrl) #> yamorySbtScriptFile !
      val yamorySbtScriptFilePath = yamorySbtScriptFile.getAbsolutePath
      s"chmod +x $yamorySbtScriptFilePath" #&& (dependenciesFile #> Process(
        Seq("bash", "-c", yamorySbtScriptFilePath),
        None,
        "PROJECT_GROUP_KEY" -> projectGroupKey,
        "YAMORY_API_KEY" -> yamoryApiKey
      )) !
    } finally {
      Seq(dependenciesFile, yamorySbtScriptFile).foreach(_.delete())
    }
  }

  lazy val yamoryYarnTask = Def.task {
    val projectGroupKey = yamoryProjectGroupKey.value
    val yamoryApiKey = autoImport.yamoryApiKey.value
    val yamoryYarnScriptUrl = autoImport.yamoryYarnScriptUrl.value
    val yamoryYarnManifest = autoImport.yamoryYarnManifest.value

    require(projectGroupKey.nonEmpty, "PROJECT_GROUP_KEY is empty. set 'yamoryProjectGroupKey' setting.")
    require(yamoryApiKey.nonEmpty, "YAMORY_API_KEY is empty. set 'yamoryApiKey' setting.")
    require(yamoryYarnScriptUrl.nonEmpty, "yamory yarn script url is empty. set 'yamoryYarnScriptUrl' setting.")
    require(yamoryYarnManifest.nonEmpty, "yamory yarn manifest is empty. set 'yamoryYarnManifest' setting.")

    val yamoryYarnScriptFile = Files.createTempFile("sbt", ".sh").toFile
    yamoryYarnScriptFile.deleteOnExit()
    try {
      url(yamoryYarnScriptUrl) #> yamoryYarnScriptFile !
      val yamoryYarnScriptFilePath = yamoryYarnScriptFile.getAbsolutePath
      s"chmod +x $yamoryYarnScriptFilePath" #&& Process(
        Seq("bash", "-c", yamoryYarnScriptFilePath, "--", "--manifest", yamoryYarnManifest),
        None,
        "PROJECT_GROUP_KEY" -> projectGroupKey,
        "YAMORY_API_KEY" -> yamoryApiKey
      ) !
    } finally {
      yamoryYarnScriptFile.delete()
    }
  }
}
