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
    val yamoryScriptUrl = settingKey[String]("https://yamory/script/...")
    val yamory = taskKey[Unit]("A task that is run yamory scan.")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    yamoryProjectGroupKey := "",
    yamoryApiKey := "",
    yamoryScriptUrl := "",
    yamory := yamoryTask.value
  )

  override lazy val buildSettings = Seq()

  override lazy val globalSettings = Seq()

  lazy val yamoryTask = Def.task {
    val projectGroupKey = yamoryProjectGroupKey.value
    val yamoryApiKey = autoImport.yamoryApiKey.value
    val yamoryScriptUrl = autoImport.yamoryScriptUrl.value

    require(projectGroupKey.nonEmpty, "PROJECT_GROUP_KEY is empty. set 'yamoryProjectGroupKey' setting.")
    require(yamoryApiKey.nonEmpty, "YAMORY_API_KEY is empty. set 'yamoryApiKey' setting.")
    require(yamoryScriptUrl.nonEmpty, "yamory script url is empty. set 'yamoryScriptUrl' setting.")

    val dependenciesFile = Files.createTempFile("sbt", ".txt").toFile
    val yamoryScriptFile = Files.createTempFile("sbt", ".sh").toFile
    Seq(dependenciesFile, yamoryScriptFile).foreach(_.deleteOnExit())
    try {
      val dependencies = (Compile / dependencyTree / asString).value
      val dependenciesLog = dependencies.split("\n").map("[info] " + _).mkString("\n")
      IO.write(dependenciesFile, dependenciesLog, IO.utf8)
      url(yamoryScriptUrl) #> yamoryScriptFile !
      val yamoryScriptFilePath = yamoryScriptFile.getAbsolutePath
      s"chmod +x $yamoryScriptFilePath" #&& (dependenciesFile #> Process(
        Seq("bash", "-c", yamoryScriptFilePath),
        None,
        "PROJECT_GROUP_KEY" -> projectGroupKey,
        "YAMORY_API_KEY" -> yamoryApiKey
      )) !
    } finally {
      Seq(dependenciesFile, yamoryScriptFile).foreach(_.delete())
    }
  }
}
