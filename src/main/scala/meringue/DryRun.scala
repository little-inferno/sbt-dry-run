package meringue

import sbt._
import sbt.Keys._

import java.nio.file.{Files, Path}

import meringue.common._

case object DryRun {
  case object keys {
    lazy val variables = taskKey[SecretEnvPair]("Variables that should be substitute to config")

    lazy val localFile = settingKey[String]("Path to file with secrets")
    lazy val localEnv  = settingKey[String]("Environment that should be use to generate secrets file")

    lazy val writeLocalEnv = taskKey[Unit]("Write secrets to custom config file")

    lazy val params = settingKey[(Path, String) => Map[String, String]]("Java params that shuld be pass to dry run")

    lazy val unsafeConstants = settingKey[SecretPair]("Variables that set manually and not exist in storages")

    lazy val dryMain = settingKey[String]("Dry run main")
  }

  case object defs {

    import keys._

    def validateConfig(state: State, projectName: Option[String]): State = {
      val extracted = Project.extract(state)

      extracted
        .structure
        .allProjectPairs
        .collect {
          case (project, ref) if project.autoPlugins.exists(_.label == "meringue.DryRunPlugin") & projectName.forall(_ == ref.project) =>
            ref
        }
        .foldLeft(state) { (state, project) =>
          val rootPath                = state.setting(project / rootPaths)("BASE")
          val unsafeConstantVarsValue = state.setting(project / unsafeConstants)
          val (_, envVariables)       = extracted.runTask(project / variables, state)
          val dryRunValue             = state.setting(project / dryMain)

          envVariables.foldLeft(state) {
            case (state, (env, variables)) =>
              val options = state.setting(project / params)
                .apply(rootPath, env)
                .map { case (k, v) => s"$k=$v" }
                .toList

              val modifedState = Project.extract(state).appendWithSession(
                Seq(
                  project / Compile / fork := true,
                  project / Compile / envVars := (variables ++ unsafeConstantVarsValue),
                  project / Compile / javaOptions ++= options
                ), state
              )

              val (res, _) = Project.extract(modifedState).runInputTask(project / Compile / runMain, s" $dryRunValue", modifedState)

              res
          }
        }
    }

    def writeLocalEnv: DefTask[Unit] =
      Def.taskDyn {
        import _root_.io.circe.syntax._

        val envs = variables.value(localEnv.value)

        Def.task {
          val pathToEnvs = (Compile / classDirectory).value.toPath.resolve(localFile.value)

          val _ = Files.writeString(pathToEnvs, envs.asJson.spaces2)
        }
      }
  }
}
