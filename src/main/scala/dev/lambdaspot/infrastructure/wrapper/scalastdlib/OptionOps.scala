package dev.lambdaspot.infrastructure.wrapper.scalastdlib

import scala.util.{Failure, Success, Try}

extension [A](value: Option[A])
  def orFail(errorMessage: String): Try[A] = orFail(new RuntimeException(errorMessage))
  def orFail(exception: Throwable): Try[A] = value.toRight(exception).toTry

extension [T](value: Option[Try[T]])
  /** Converts the nested structure of `Option[Try[T]]` into a `Try[Option[T]]`.
    * Similar operation is also known as `.sequence` in Haskell or Scala Cats.
    */
  def collectAll: Try[Option[T]] = value.fold[Try[Option[T]]](Success(None))(_.map(Some(_)))
