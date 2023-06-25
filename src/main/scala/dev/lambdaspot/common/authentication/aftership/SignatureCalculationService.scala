package dev.lambdaspot.common.authentication.aftership

import scala.util.{Success, Try}

// It's some dummy code, to make a bit of things to have a larger structure for a demo skeleton project.
// Shows that that we have multiple classes and some Dependency Injection, etc.
class SignatureCalculationService(encoder: SignatureEncoder) {
  def calculate: Try[ApiKeyEncoded] = encoder.encode
}

class SignatureEncoder {
  def encode: Try[ApiKeyEncoded] = Success(ApiKeyEncoded("bcfba53d95454ada96b9658c4f178764"))
}

opaque type ApiKeyEncoded = String
object ApiKeyEncoded {
  def apply(value: String): ApiKeyEncoded = value
}
