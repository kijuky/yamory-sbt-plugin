version := "1.0.0"
scalaVersion := "2.13.9"

yamoryProjectGroupKey := "dummy"
yamoryApiKey := "dummy"
yamoryNpmScriptUrl := "file:./yamory.sh"
yamoryNpmManifest := "./target/scala-2.12/scalajs-bundler/main/package.json"

enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
scalaJSUseMainModuleInitializer := true
