import Dependencies._

inThisBuild(
  Seq(
    organization := "space.scalapatisserie",
    homepage := Some(url("https://github.com/scalapatisserie/sbt-meringue")),
    description := "Validate configuration of your project",
    developers := List(
      Developer(
        "little-inferno",
        "Danil Zasypkin",
        "danil@littleinferno.space",
        url("https://github.com/little-inferno")
      )
    ),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    scmInfo :=
      Some(ScmInfo(url("https://github.com/scalapatisserie/sbt-meringue"), "scm:git@github.com:scalapatisserie/sbt-meringue.git")),
    Test / publishArtifact := false,
    versionScheme := Some("semver-spec"),
    sonatypeCredentialHost := "s01.oss.sonatype.org",
    sonatypeRepository := "https://s01.oss.sonatype.org/service/local",
    sbtPluginPublishLegacyMavenStyle := true
  )
)

val `sbt-meringue` = project
  .in(file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    scalaVersion := "2.12.18",
    scalacOptions ++= List(
      "-Xlint:" + String.join(
        ",",
        "_",
        "-package-object-classes",
        "-inaccessible"
      ),
      "-Werror",
      "-opt:l:method",
      "-opt-warnings:none",
      "-Ypartial-unification"
    ),
    libraryDependencies ++= circe.core :: circe.generic :: circe.parser :: circe.yaml :: circe.extra :: Nil,
    libraryDependencies ++= compilerPlugin(kindProjector) :: compilerPlugin(betterFor) :: compilerPlugin(
      macroParadise
    ) :: Nil,
    scriptedLaunchOpts += ("-Dplugin.version=" + version.value)
  )
