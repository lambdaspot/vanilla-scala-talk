package dev.lambdaspot.location.api

import com.amazonaws.services.lambda.runtime.Context
import com.softwaremill.macwire.wire
import dev.lambdaspot.aws.lambda.core.{ApiGatewayLambda, AwsLambdaEntryPoint}
import dev.lambdaspot.aws.lambda.events.ApiGatewayProxiedRequest
import dev.lambdaspot.common.authentication.aftership.{ApiAuthenticationModule, ApiAuthenticator, ApiKeyEncoded}
import dev.lambdaspot.common.errorhandler.aftership.AftershipErrorHandler
import dev.lambdaspot.infrastructure.SystemPropertyReader
import dev.lambdaspot.infrastructure.SystemPropertyReader.lookupConfigOrFail
import dev.lambdaspot.infrastructure.wrapper.awslambda.*
import dev.lambdaspot.infrastructure.wrapper.iron.validate
import dev.lambdaspot.location.model.{Country, CourierSlug}
import dev.lambdaspot.location.service.places.LocationRepository.LocationApiConfiguration
import dev.lambdaspot.location.service.places.{LocationRepository, LocationService}
import sttp.client3.{HttpClientSyncBackend, Identity, SttpBackend}

import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

trait LocationModule extends AwsLambdaEntryPoint with ApiAuthenticationModule { // TODO: add another example controller using this module
  override lazy val entryPoint: LocationController = wire[LocationController]
  lazy val service: LocationService                = wire[LocationService]
  lazy val errorHandler: AftershipErrorHandler     = wire[AftershipErrorHandler]
  lazy val repository: LocationRepository          = wire[LocationRepository]
  lazy val apiConfig: LocationApiConfiguration     = LocationApiConfiguration(apiUrl)
  lazy val httpBackend: SttpBackend[Identity, Any] = HttpClientSyncBackend()
  lazy val apiUrl: Try[String]                     = lookupConfigOrFail("LOCATIONS_API_URL", "Locations API URL not provided")
}

object LocationController extends LocationModule

class LocationController(service: LocationService, authenticator: ApiAuthenticator) extends ApiGatewayLambda[LocationResponseDto] {
  override def run(request: ApiGatewayProxiedRequest, context: Context): Try[LocationResponseDto] =
    for {
      courierSlug         <- request.parseQueryStringParam("courierSlug").flatMap(CourierSlug.validate(_))
      country             <- request.parseQueryStringParam("country").flatMap(Country.validate(_))
      given ApiKeyEncoded <- authenticator.obtainApiKey()
      response            <- service.fetch(courierSlug, country)
    } yield LocationResponseDto.of(response)

}
