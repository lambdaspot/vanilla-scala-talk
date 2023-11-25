package dev.lambdaspot.infrastructure.wrapper

import io.github.iltotore.iron.{Constraint, RefinedTypeOps}

import scala.util.{Failure, Success, Try}

object iron:
  extension [A, C, T](ops: RefinedTypeOps[A, C, T])
    /** Refine the given value at runtime, resulting in an [[Try]].
      *
      * @param constraint
      *   the constraint to test with the value to refine.
      * @return
      *   a [[Success]] containing this value as [[T]] or a [[Failure]] with
      *   [[IllegalArgumentException]] containing the constraint message.
      * @see
      *   [[fromIronType]], [[option]], [[applyUnsafe]], [[either]].
      */
    inline def validate(value: A)(using constraint: Constraint[A, C]): Try[T] = {
      Either
        .cond(constraint.test(value), value.asInstanceOf[T], constraint.message)
        .fold(
          e => Failure(new IllegalArgumentException(e)),
          Success(_)
        )
    }
