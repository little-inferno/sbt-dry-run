package meringue.vault

import sbt._
import sbt.Keys._
import sbt.internal.util.ManagedLogger

import meringue.common._
import meringue.http.HttpClient

object VaultSecretProvider {
  object keys {
    lazy val roleId   = settingKey[String]("Vault approle role id")
    lazy val secretId = settingKey[String]("Vault approle secret id")

    lazy val vaultURL        = settingKey[String]("Vault URL")
    lazy val vaultMountPoint = settingKey[String]("Vault project")
    lazy val vaultPaths      = settingKey[List[String]]("Vault project paths")

    lazy val getVaultVariables = taskKey[SecretEnvPair]("Get all variables from kv2 vault storage")

    lazy val httpClient = settingKey[HttpClient]("httpClient")
  }

  object defs {
    import keys._

    def getVaultVariables: DefTask[SecretEnvPair] =
      Def.taskDyn {
        implicit val log: ManagedLogger = streams.value.log
        val vaultURLValue               = vaultURL.value
        val roleIdValue                 = roleId.value
        val secretIdValue               = secretId.value
        val vaultMountPointValue        = vaultMountPoint.value
        val vaultPathsValue             = vaultPaths.value
        val clientValue                 = httpClient.value

        Def.task {
          val auth = clientValue.post[AuthRequest, AuthResponse](
            new URI(s"$vaultURLValue/v1/auth/approle/login"),
            AuthRequest(roleIdValue, secretIdValue)
          )

          vaultPathsValue.map { path =>
            s"$vaultMountPointValue/data/$path" -> clientValue
              .get[SecretResponse](
                new URI(s"$vaultURLValue/v1/$vaultMountPointValue/data/$path"),
                Map("X-Vault-Token" -> auth.auth.clientToken)
              )
              .data
              .data
          }.toMap
        }
      }
  }
}
