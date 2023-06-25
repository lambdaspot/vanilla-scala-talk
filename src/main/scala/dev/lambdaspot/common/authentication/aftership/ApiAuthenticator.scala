package dev.lambdaspot.common.authentication.aftership

import scala.util.Try

// It's some dummy code, to make a bit of things to have a larger structure for a demo skeleton project
class ApiAuthenticator(calculator: SignatureCalculationService) {
  def obtainApiKey(): Try[ApiKeyEncoded] = calculator.calculate
}
