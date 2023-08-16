package meringue

import sbt.{Def, Task}

object common {
  type Env           = String
  type SecretPair    = Map[String, String]
  type SecretEnvPair = Map[Env, SecretPair]
  type DefTask[X]    = Def.Initialize[Task[X]]
}
