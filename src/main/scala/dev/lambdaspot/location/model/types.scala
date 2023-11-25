package dev.lambdaspot.location.model

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

/** Do you like the validation below? See more Iron examples in docs:
  * https://iltotore.github.io/iron/docs/reference/newtypes.html
  */

/** Courier slug, a non-empty string */
opaque type CourierSlug = String :| MinLength[1]
object CourierSlug extends RefinedTypeOps.Transparent[CourierSlug]

/** Country in ISO 3166-1 alpha 3 code */
opaque type Country = String :| (LettersUpperCase & MinLength[3] & MaxLength[3])
object Country extends RefinedTypeOps.Transparent[Country]

