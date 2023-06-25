package dev.lambdaspot.infrastructure.wrapper.sttp

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import dev.lambdaspot.infrastructure.DEFAULT_LOCALE
import dev.lambdaspot.infrastructure.wrapper.jsoniter.*
import dev.lambdaspot.infrastructure.wrapper.scalastdlib.*
import sttp.model.{Header, StatusCode}

import scala.util.Try

trait HttpApiResponse[T] {
  def code: StatusCode
  def responseBody: Option[T]
  def headers: Seq[Header]
}

object HttpApiResponse {
  implicit class HttpApiResponseExt(val successfulResult: Try[HttpApiResponse[String]]) { // TODO convert to extension method

    def code: Try[StatusCode] = successfulResult.map(_.code)

    /** Provides vital response body, or error if empty or missing */
    def body: Try[String] =
      bodyOption
        .flatMap(_.filter(_.nonEmpty).orFail("Missing response body"))

    /** Provides optional response body (if not empty), otherwise [[None]] */
    def bodyOption: Try[Option[String]] = successfulResult
      .map(_.responseBody.filter(_.nonEmpty))

    /** Provides vital response body deserialized from JSON into object [[T]],
      * or error if empty or missing
      */
    def body[T](implicit c: JsonValueCodec[T]): Try[T] = body.flatMap(_.fromJson[T])

    /** Provides optional response body deserialized from JSON into object [[T]]
      * (if not empty), otherwise [[None]]
      */
    def bodyOption[T](implicit c: JsonValueCodec[T]): Try[Option[T]] =
      bodyOption
        .flatMap {
          _.map(_.fromJson[T]).collectAll
        }

    /** Provides header response value for given key, or error if value is
      * missing or empty
      */
    def headerValue(headerKey: String): Try[String] =
      headerValueOption(headerKey)
        .map(_.orFail(s"Missing response header. key=[$headerKey]"))
        .flatten

    /** Provides header response value for given key, or [[None]] if value is
      * missing or empty
      */
    def headerValueOption(headerKey: String): Try[Option[String]] =
      successfulResult.map {
        _.headers
          .find(_.name.toLowerCase(DEFAULT_LOCALE) == headerKey)
          .map(_.value)
          .filter(_.nonEmpty)
      }

  }

}
