package dev.lambdaspot.infrastructure.wrapper.jsoniter

import com.github.plokhotnyuk.jsoniter_scala.core.*

import scala.util.Try

extension (payload: String)
  def fromJson[T](using JsonValueCodec[T]): Try[T] =
    Try(readFromString(payload))

extension [T](obj: T)
  def toJson(using JsonValueCodec[T]): Try[String] =
    Try(writeToString(obj))
