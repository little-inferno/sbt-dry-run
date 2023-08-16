package meringue.http

import sbt.internal.util.ManagedLogger

import java.io.IOException
import java.net.{ProxySelector, SocketAddress, URI}
import java.net.http.{HttpClient as JHttpClient, HttpRequest, HttpResponse}
import java.net.http.HttpResponse.BodyHandlers

import scala.reflect.runtime.universe._

import _root_.io.circe.{Error => JsonError, _}
import _root_.io.circe.parser.parse
import _root_.io.circe.syntax._
import io.circe.Decoder
import io.circe.generic.JsonCodec

class HttpClient(httpClient: JHttpClient) {
  def get[Out: Decoder: TypeTag](uri: URI, headers: Map[String, String] = Map.empty)(implicit log: ManagedLogger): Out = {
    val request = HttpRequest.newBuilder(uri).GET()
    headers.foreach { case (k, v) => request.header(k, v) }

    decodeResponse(httpClient.send(request.build(), BodyHandlers.ofString()))
  }

  def post[In: Encoder, Out: Decoder: TypeTag](uri: URI, req: In, headers: Map[String, String] = Map.empty)(implicit log: ManagedLogger): Out = {
    val request = HttpRequest.newBuilder(uri).POST(HttpRequest.BodyPublishers.ofString(req.asJson.noSpaces))
    headers.foreach { case (k, v) => request.header(k, v) }

    decodeResponse(httpClient.send(request.build(), BodyHandlers.ofString()))
  }

  private def decodeResponse[T: Decoder: TypeTag](rawResponse: HttpResponse[String])(implicit log: ManagedLogger): T =
    if (rawResponse.statusCode() != 200) {
      log.err(HttpClient.safeDecode[HttpClient.Error](rawResponse.body())().errors.mkString("\n"))
      throw new Exception(s"Can't get data from vault")
    } else {
      HttpClient.safeDecode[T](rawResponse.body())()
    }
}

object HttpClient {
  @JsonCodec
  case class Error(errors: List[String])

  def apply: HttpClient = apply(identity)

  def apply(builder: JHttpClient.Builder => JHttpClient.Builder): HttpClient = {
    val clientBuilder = JHttpClient
      .newBuilder()
      .proxy(new ProxySelector {
        override def select(uri: URI): java.util.List[java.net.Proxy] = java.util.Collections.emptyList()

        override def connectFailed(uri: URI, sa: SocketAddress, ioe: IOException): Unit = ProxySelector.getDefault.connectFailed(uri, sa, ioe)
      })

    new HttpClient(builder(clientBuilder).build())
  }

  def safeDecode[T: Decoder: TypeTag](str: String)(parser: String => Either[JsonError, Json] = parse): T =
    parser(str).flatMap(_.as[T]) match {
      case Left(value)  =>
        throw new Exception(s"Can't decode $value to ${typeTag[T].tpe.typeSymbol.name.toString}")
      case Right(value) => value
    }
}
