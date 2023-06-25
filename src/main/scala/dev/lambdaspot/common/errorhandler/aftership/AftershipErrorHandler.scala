package dev.lambdaspot.common.errorhandler.aftership

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import dev.lambdaspot.infrastructure.wrapper.jsoniter.*
import dev.lambdaspot.infrastructure.wrapper.sttp.{ClientError, HttpApiResponse}
import dev.lambdaspot.location.service.places.Data
import sttp.model.StatusCode

import scala.util.{Failure, Success, Try}

class AftershipErrorHandler {

  def handle(response: HttpApiResponse[String]): Try[HttpApiResponse[String]] =
    Option
      .when(response.code.isSuccess)(response.responseBody)
      .flatten
      .fold(Success(response)) {
        _.fromJson[ErrorResponseDto]
          .map(_.meta)
          .flatMap(classify(response, _))
      }

  /** An example mapping of error codes for error responses with status 200 to
    * be classified as client errors.
    * https://www.aftership.com/docs/shipping/quickstart/request-errors
    */
  private def classify(response: HttpApiResponse[String], meta: Meta): Try[HttpApiResponse[String]] =
    meta.code match {
      case 200                              => Success(response)
      case 4100 | 4101                      => Failure(ClientError(response, StatusCode.InternalServerError, meta.summary, meta.retryable))
      case 4104 | 4153                      => Failure(ClientError(response, StatusCode.BadRequest, meta.summary, meta.retryable))
      case 4157 | 4159 | 4161 | 4155 | 4500 => Failure(ClientError(response, StatusCode.UnprocessableEntity, meta.summary, meta.retryable))
      case _                                => Failure(ClientError(response, StatusCode.InternalServerError, meta.summary, meta.retryable))
      // and so on, according to needs, depending on own interpretation...
    }
}

private[aftership] final case class ErrorResponseDto(
    meta: Meta
)
private[aftership] object ErrorResponseDto {
  given JsonValueCodec[ErrorResponseDto] = JsonCodecMaker.make
}

private[aftership] final case class Meta(
    code: Int,
    message: String,
    details: Vector[String],
    retryable: Boolean = false
) {
  def summary = s"$code: $message"
}
