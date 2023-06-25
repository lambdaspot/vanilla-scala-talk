package dev.lambdaspot.location.service.places

import dev.lambdaspot.common.authentication.aftership.ApiKeyEncoded
import dev.lambdaspot.infrastructure.wrapper.sttp.*
import dev.lambdaspot.location.model.{Country, CourierSlug}
import dev.lambdaspot.location.service.places.LocationRepository.LocationApiConfiguration
import sttp.client3.{Identity, SttpBackend, UriContext, basicRequest}
import sttp.model.Uri

import scala.util.Try

/** An example of a repository that fetches data from an external API.
  *
  * For example purposes, we use here a randomly selected API provider. It's
  * AfterShip which has awesome documentation.
  *   - Specification:
  *     https://www.aftership.com/docs/shipping/bdb851fc7dfdc-get-locations
  *   - Example call:
  *     https://sandbox-api.aftership.com/postmen/v3/locations?slug=evri&country=GBR
  */
class LocationRepository(sttpBackend: SttpBackend[Identity, Any], config: LocationApiConfiguration) {

  def fetch(slug: CourierSlug, country: Country)(using apiKey: ApiKeyEncoded): Try[HttpApiSuccess[String]] =
    buildUri(config.apiUrl, slug, country).flatMap { uri =>
      basicRequest
        .get(uri)
        .header("Content-Type", "application/json")
        .header("as-api-key", apiKey.toString)
        .send(sttpBackend)
        .toTry
    }

  private def buildUri(apiUrl: Try[String], slug: CourierSlug, country: Country): Try[Uri] =
    apiUrl.map { url =>
      uri"$url/?slug=$slug&country=$country"
    }

}

object LocationRepository {
  final case class LocationApiConfiguration(apiUrl: Try[String])
}
