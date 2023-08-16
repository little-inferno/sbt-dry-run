import java.io.File
import java.net.URI

import cats.effect._
import com.typesafe.config.ConfigFactory
import derevo.derive
import derevo.pureconfig.pureconfigReader
import pureconfig.ConfigSource

@derive(pureconfigReader)
case class AppConfig(database: DatabaseConfig)

@derive(pureconfigReader)
case class DatabaseConfig(
    url: URI,
    username: String,
    password: String
)

object DryMain extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    for {
      cfg <- IO.fromOption(
        sys.props.get("config.file")
          .map(new File(_))
          .map(ConfigFactory.parseFile)
      )(new Throwable("Can't find config"))
      res <- ConfigSource.fromConfig(cfg.resolve().getConfig("app")).load[AppConfig] match {
        case Left(value) => IO.delay(println(value)).as(ExitCode.Error)
        case Right(_)    => IO.delay(println("Application config valid")).as(ExitCode.Success)
      }
    } yield res
}
