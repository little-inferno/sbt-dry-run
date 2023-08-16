import java.nio.file.Paths

lazy val root = (project in file("."))
  .settings(
    name := "sbt-dry-run-plugin-example-app",
    scalaVersion := "2.13.10",
    scalacOptions += "-Ymacro-annotations",
    libraryDependencies += "org.typelevel" %% "cats-effect" % "3.5.1",
    libraryDependencies += "com.github.pureconfig" %% "pureconfig" % "0.17.4",
    libraryDependencies += "tf.tofu" %% "derevo-pureconfig" % "0.13.0"
  )
  .enablePlugins(DryRunK8sPlugin, DryRunPlugin)
  .settings(
    secretVariables := Map(
      "test-app/data/application-secrets/test" -> Map(
        "DB_URL" -> "http://localhost:5432",
        "DB_USER" -> "username",
        "DB_PASSWORD" -> "password"),
      "test-app/data/application-secrets/prod" -> Map(
          "DB_URL" -> "http://localhost:5432",
          "DB_USER" -> "username",
          "DB_PASSWORD" -> "password")
    ),

    valuesPaths := { (path, env) => List(path.resolve(s"k8s/values.$env.yaml")) },
    pathToVars  := "app" :: "env" :: Nil,

    variables := validateK8SSpec.value,
    appEnvs   := "dev" :: "prod" :: Nil,
    params    := { (path, env) => Map("-Dconfig.file" -> path.resolve(s"k8s/application.$env.conf").toString) },
    dryMain   := "DryMain"
  )
