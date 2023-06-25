package dev.lambdaspot.common.authentication.aftership

import com.softwaremill.macwire
import com.softwaremill.macwire.wire

// It's some dummy code, to make a bit of things to have a larger structure for a demo skeleton project
trait ApiAuthenticationModule {
  lazy val apiAuthenticator: ApiAuthenticator              = wire[ApiAuthenticator]
  private lazy val calculator: SignatureCalculationService = wire[SignatureCalculationService]
  private lazy val encoder: SignatureEncoder               = wire[SignatureEncoder]
}
