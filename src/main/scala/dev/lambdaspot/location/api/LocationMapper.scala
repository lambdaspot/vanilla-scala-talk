package dev.lambdaspot.location.api

import dev.lambdaspot.location.service.places.{ApiLocationResponseDto, DayOfWeek, Location}
import io.github.arainko.ducktape.*

import scala.annotation.nowarn
import scala.util.chaining.scalaUtilChainingOps

object LocationMapper {

  def intoFrontendModel(apiResponse: ApiLocationResponseDto): LocationResponseDto =
    apiResponse.data.locations
      .map { (location: Location) =>
        location
          .into[LocationDto]
          .transform(
            Field.computed(
              _.isOpenAtWeekend,
              _ => location.businessHours.exists(hours => hours.dayOfWeek == DayOfWeek.Sat || hours.dayOfWeek == DayOfWeek.Sun)
            )
          )
      }
      .pipe(a => LocationResponseDto.apply(a))

  @nowarn("msg=never used") // this demonstrates how we would do it manually without using the Chimney or Ducktape library
  def intoFrontendModelManually(apiResponse: ApiLocationResponseDto): LocationResponseDto =
    apiResponse.data.locations
      .map { location =>
        LocationDto(
          locationId = location.locationId,
          name = location.name,
          phone = location.phone,
          address = AddressDto(
            postalCode = location.address.postalCode,
            country = location.address.country,
            city = location.address.city,
            latitude = location.address.latitude,
            longitude = location.address.longitude
          ),
          isOpenAtWeekend = location.businessHours.exists(hours => hours.dayOfWeek == DayOfWeek.Sat || hours.dayOfWeek == DayOfWeek.Sun)
        )
      }
      .pipe(LocationResponseDto.apply)
}
