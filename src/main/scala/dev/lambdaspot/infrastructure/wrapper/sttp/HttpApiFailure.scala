package dev.lambdaspot.infrastructure.wrapper.sttp

import sttp.client3.{Identity, Response}
import sttp.model.{Header, StatusCode}

import scala.util.{Failure, Success, Try}

extension (response: Identity[Response[Either[String, String]]])
  def toTry: Try[HttpApiSuccess[String]] = response.body match {
    case Left(_) if response.code.isClientError => Failure(ClientError(response))
    case Left(_)                                => Failure(ServerError(response)) // if needed handle 3xx, 1xx separately
    case Right(value) if value.isBlank          => Success(HttpApiSuccess(response.code, None, response.headers))
    case Right(value)                           => Success(HttpApiSuccess(response.code, Some(value), response.headers))
  }

trait HttpApiFailure extends HttpApiResponse[String] { // TODO: T ?
  def code: StatusCode
  def reason: Option[String]
  def responseBody: Option[String]
  def headers: Seq[Header]
}

final case class ClientError(
    code: StatusCode,
    reason: Option[String],
    responseBody: Option[String],
    retryable: Boolean = false,
    headers: Seq[Header]
) extends RuntimeException(responseBody.orNull)
    with HttpApiFailure

object ClientError:

  def apply(response: Identity[Response[Either[String, String]]]): ClientError =
    ClientError(code = response.code, reason = None, responseBody = response.body.fold(Some(_), Some(_)), headers = response.headers)

  def apply(response: HttpApiResponse[String], code: StatusCode, reason: String, retryable: Boolean): ClientError =
    ClientError(code, Some(reason), response.responseBody, retryable, response.headers)

final case class ServerError(
    code: StatusCode,
    reason: Option[String],
    responseBody: Option[String],
    headers: Seq[Header]
) extends RuntimeException(responseBody.orNull)
    with HttpApiFailure

object ServerError:

  def apply(response: Identity[Response[Either[String, String]]]): ServerError =
    ServerError(response.code, None, response.body.fold(Some(_), Some(_)), response.headers)

