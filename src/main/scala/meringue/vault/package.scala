package meringue

import io.circe.generic.extras.{Configuration, ConfiguredJsonCodec}

package object vault {
  implicit val Config: Configuration = Configuration.default.withSnakeCaseMemberNames

  @ConfiguredJsonCodec
  case class AuthRequest(roleId: String, secretId: String)

  @ConfiguredJsonCodec
  case class AuthResponse(auth: Auth)

  @ConfiguredJsonCodec
  case class Auth(clientToken: String)

  @ConfiguredJsonCodec
  case class SecretResponse(data: Data)

  @ConfiguredJsonCodec
  case class Data(data: Map[String, String])
}
