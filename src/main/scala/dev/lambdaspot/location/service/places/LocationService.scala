package dev.lambdaspot.location.service.places

import dev.lambdaspot.common.authentication.aftership.{ApiAuthenticationModule, ApiAuthenticator, ApiKeyEncoded}
import dev.lambdaspot.common.errorhandler.aftership.AftershipErrorHandler
import dev.lambdaspot.infrastructure.wrapper.jsoniter.*
import dev.lambdaspot.location.model.{Country, CourierSlug}
import dev.lambdaspot.location.service.places.{ApiLocationResponseDto, LocationRepository}

import scala.util.Try

class LocationService(repository: LocationRepository, errorHandler: AftershipErrorHandler) {

  def fetch(slug: CourierSlug, country: Country)(using apiKey: ApiKeyEncoded): Try[ApiLocationResponseDto] =
    repository
      .fetch(slug, country)
      .flatMap(errorHandler.handle(_))
      .body[ApiLocationResponseDto]
}
