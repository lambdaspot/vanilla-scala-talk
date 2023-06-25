package dev.lambdaspot.infrastructure.wrapper.sttp

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import dev.lambdaspot.infrastructure.DEFAULT_LOCALE
import dev.lambdaspot.infrastructure.wrapper.jsoniter.*
import dev.lambdaspot.infrastructure.wrapper.scalastdlib.*
import sttp.model.{Header, StatusCode}

import scala.util.{Success, Try}

final case class HttpApiSuccess[T](
    code: StatusCode,
    responseBody: Option[T],
    headers: Seq[Header]
) extends HttpApiResponse[T]
