
version := "1.0.0"
scalaVersion := "2.12.1"

yamoryProjectGroupKey := "dummy"
yamoryApiKey := "dummy"
yamoryYarnScriptUrl := "file:./yamory.sh"
yamoryYarnManifest := "./target/scala-2.12/scalajs-bundler/main/package.json"

enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
scalaJSUseMainModuleInitializer := true
useYarn := true
