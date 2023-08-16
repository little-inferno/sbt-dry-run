package meringue.k8s

import sbt._
import sbt.Keys._
import sbt.internal.util.ManagedLogger

import java.nio.file.{Files, Path}

import _root_.io.circe._
import _root_.io.circe.yaml.parser.parse

import meringue.common._
import meringue.http.HttpClient

object K8sVariableProvider {
  object keys {
    lazy val appEnvs     = settingKey[List[Env]]("List of all application environments that need to check")
    lazy val valuesPaths = settingKey[(Path, Env) => List[Path]]("Get all values.yaml file for specific environment")
    lazy val pathToVars  = settingKey[List[String]]("Path to variables inside values.yaml file")

    lazy val secretExtractor = settingKey[SecretExtractor]("How get secret variable")

    lazy val secretVariables = taskKey[SecretEnvPair]("Variables from you secret storage")
    lazy val validateK8SSpec = taskKey[SecretEnvPair]("Check k8s configuration, and return real env variables")
  }

  object defs {
    import keys._

    def validateK8SSpec: DefTask[SecretEnvPair] =
      Def.taskDyn {
        implicit val log: ManagedLogger = streams.value.log
        val pathToVarsValue             = pathToVars.value
        val secretVariablesValue        = secretVariables.value
        val secretExtractorValue        = secretExtractor.value

        def getEnvPairs(pathToValues: Path): Map[String, String] =
          HttpClient.safeDecode[SecretPair](Files.readString(pathToValues)) { str =>
            parse(str).flatMap { js =>
              pathToVarsValue
                .foldLeft(Option(js)) {
                  case (Some(acc), path) => (acc \\ path).headOption
                  case _                 => None
                }
                .toRight(DecodingFailure(s"Can't find ${pathToVarsValue.mkString(" \\ ")}", js.hcursor.history))
            }
          }

        Def.task {
          (for {
            env          <- appEnvs.value
            pathToValues = valuesPaths.value.apply(rootPaths.value("BASE"), env)
          } yield {
            if (pathToValues.nonEmpty && pathToValues.exists(!_.toFile.canRead))
              log.err(s"Can't read ${pathToValues.mkString(",")}")

            val envPairs = pathToValues.flatMap(getEnvPairs(_).toList)

            env -> envPairs.map {
              case (realEnv, secretExtractorValue(path, value)) =>
                secretVariablesValue.get(path).flatMap(_.get(value)) match {
                  case Some(value) => realEnv -> value
                  case None        =>
                    throw new Exception(s"Can't find $value in $path for $env")
                }
              case (realEnv, value)                => realEnv -> value
            }.toMap
          }).toMap
        }
      }
  }
}
