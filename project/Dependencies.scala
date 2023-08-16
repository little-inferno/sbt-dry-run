import sbt._

object Dependencies {
  lazy val kindProjector = "org.typelevel"  %% "kind-projector"     % "0.13.2" cross CrossVersion.patch
  lazy val betterFor     = "com.olegpy"     %% "better-monadic-for" % "0.3.1"
  lazy val macroParadise = "org.scalamacros" % "paradise"           % "2.1.1" cross CrossVersion.patch

  object circe {
    lazy val core    = "io.circe" %% "circe-core"           % "0.14.1"
    lazy val generic = "io.circe" %% "circe-generic"        % "0.14.1"
    lazy val parser  = "io.circe" %% "circe-parser"         % "0.14.1"
    lazy val yaml    = "io.circe" %% "circe-yaml"           % "0.14.1"
    lazy val extra   = "io.circe" %% "circe-generic-extras" % "0.14.1"
  }
}
