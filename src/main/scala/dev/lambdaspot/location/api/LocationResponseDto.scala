package dev.lambdaspot.location.api

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import dev.lambdaspot.location.service.places.ApiLocationResponseDto

private[api] opaque type LocationResponseDto = Vector[LocationDto]

private[api] object LocationResponseDto {
  given JsonValueCodec[LocationResponseDto]                        = JsonCodecMaker.make
  def apply(loactions: Vector[LocationDto]): LocationResponseDto   = loactions
  def of(apiResponse: ApiLocationResponseDto): LocationResponseDto = LocationMapper.intoFrontendModel(apiResponse)
}

private[api] final case class LocationDto(
    locationId: String,
    name: String,
    phone: String,
    address: AddressDto,
    isOpenAtWeekend: Boolean
)

final case class AddressDto(
    postalCode: String,
    country: String,
    city: String,
    latitude: Double,
    longitude: Double
)
