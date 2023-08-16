package meringue

import sbt._
import sbt.Keys._

import meringue.http.HttpClient
import meringue.k8s.{K8sVariableProvider, SecretExtractor}
import meringue.vault.VaultSecretProvider

object DryRunVaultPlugin extends AutoPlugin {
  import VaultSecretProvider._
  import VaultSecretProvider.keys._

  override def trigger = noTrigger

  val autoImport = VaultSecretProvider.keys

  override lazy val projectSettings: Seq[Def.Setting[_]] =
    Seq[Setting[_]](
      getVaultVariables := defs.getVaultVariables.value
    )

  override lazy val globalSettings: Seq[Def.Setting[_]] = Seq(
    roleId := "",
    secretId := "",
    vaultURL := "",
    vaultMountPoint := "",
    vaultPaths := Nil,
    getVaultVariables := Map.empty,
    httpClient := HttpClient.apply
  )
}

object DryRunK8sPlugin extends AutoPlugin {
  import K8sVariableProvider._
  import K8sVariableProvider.keys._

  override def trigger = noTrigger

  val autoImport = K8sVariableProvider.keys

  override lazy val projectSettings: Seq[Def.Setting[_]] =
    Seq[Setting[_]](
      validateK8SSpec := defs.validateK8SSpec.value
    )

  override lazy val globalSettings: Seq[Def.Setting[_]] = Seq(
    appEnvs := List(""),
    valuesPaths := { (_, _) => Nil },
    pathToVars := List.empty,
    secretVariables := Map.empty,
    secretExtractor := SecretExtractor.vault
  )
}

object DryRunPlugin extends AutoPlugin {
  import DryRun._
  import DryRun.keys._

  override def trigger = noTrigger

  val autoImport = DryRun.keys

  override lazy val projectSettings: Seq[Def.Setting[_]] =
    Seq[Setting[_]](
      writeLocalEnv := defs.writeLocalEnv.value
    )

  override lazy val globalSettings: Seq[Def.Setting[_]] = Seq(
    commands += Command.command("validateAllConfig")(defs.validateConfig(_, None)),
    commands += Command.single("validateProjConfig")((s, a) => defs.validateConfig(s, Some(a))),
    params := { (_, _) => Map.empty },
    variables := Map.empty,
    localEnv := "dev",
    localFile := "env.json",
    unsafeConstants := Map.empty[String, String],
    dryMain := "DryRun"
  )
}
