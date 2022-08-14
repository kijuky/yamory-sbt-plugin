# sbt-yamory

[yamory](https://yamory.io/) for sbt.

## Usage

This plugin requires `bash`, and sbt 1.4+

plugins.sbt:

```sbt
addSbtPlugin("io.github.kijuky" % "sbt-yamory" % "4.0.2")
```

### for Scala project

build.sbt:

```sbt
yamoryProjectGroupKey := "PUT PROJECT_GROUP_KEY"
yamoryApiKey := sys.env("YAMORY_API_KEY")
yamorySbtScriptUrl := "PUT https://yamory/script/..."
```

credentials:

```.envrc:shell
export YAMORY_API_KEY="PUT YOUR YAMORY_API_KEY"
```

and run

```shell
sbt yamory
```

then scan results are recorded in yamory.

You should set `yamorySbtScriptUrl`. if it is empty, the `yamory` task do nothing.

### for [Scala.js](https://www.scala-js.org/) project

[Since yamory supports the yarn project](https://yamory.io/docs/command-scan-yarn/#gsc.tab=0),
you can scan [the node packages](https://www.npmjs.com/) used by your scala.js project.

build.sbt:

```sbt
yamoryProjectGroupKey := "PUT PROJECT_GROUP_KEY"
yamoryApiKey := sys.env("YAMORY_API_KEY")
yamoryYarnScriptUrl := "PUT https://yamory/script/..."
```

- Share `yamoryProjectGroupKey` and `yamoryApiKey`.
- If you are using [scalajs-bundler](https://github.com/scalacenter/scalajs-bundler)
  and you are using [`npmDependencies`](https://scalacenter.github.io/scalajs-bundler/reference.html#npm-dependencies),
  you need to add the following settings:
  ```
  yamoryYarnManifest := "./target/scala-2.12/scalajs-bundler/main/package.json"
  ```
  If you omit this setting, `./package.json` will be referenced.

credentials:

```.envrc:shell
export YAMORY_API_KEY="PUT YOUR YAMORY_API_KEY"
```

and run

```shell
sbt yamoryYarn
```

You should set `yamoryYarnScriptUrl`. if it is empty, the `yamoryYarn` task do nothing.

- For [a multi-project build](https://www.scala-sbt.org/1.x/docs/Multi-Project.html),
  specify the project and execute:
  ```
  sbt client/yamoryYarn
  ```

then scan results are recorded in yamory.

## for Developers

### Testing

Run `test` for regular unit tests.

Run `scripted` for [sbt script tests](http://www.scala-sbt.org/1.x/docs/Testing-sbt-plugins.html).

### Publishing

1. push tag `vX.Y.Z`
