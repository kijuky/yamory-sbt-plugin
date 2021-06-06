# yamory-sbt-plugin

[yamory](https://yamory.io/) for sbt

## Usage

This plugin requires sbt 1.4+

plugins.sbt:

```sbt
addSbtPlugin("io.github.kijuky" % "yamory-sbt-plugin" % "1.1.0")
```


build.sbt:

```sbt
yamoryProjectGroupKey := "PUT PROJECT_GROUP_KEY"
yamoryApiKey := sys.env("YAMORY_API_KEY")
yamoryScriptUrl := "PUT https://yamory/script/..."
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

### Testing

Run `test` for regular unit tests.

Run `scripted` for [sbt script tests](http://www.scala-sbt.org/1.x/docs/Testing-sbt-plugins.html).

### Publishing

1. push tag `vX.Y.Z`
