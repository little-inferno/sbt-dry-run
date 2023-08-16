package meringue

package object k8s {
  trait SecretExtractor {
    def unapply(path: String): Option[(String, String)]
  }

  object SecretExtractor {
    val vault: SecretExtractor = new SecretExtractor {
      private val pattern = "vault:(.+?(?=#))#(.+)".r

      override def unapply(path: String): Option[(String, String)] =
        path match {
          case pattern(path, value) => Some(path -> value)
          case _                    => None
        }
    }
  }
}
