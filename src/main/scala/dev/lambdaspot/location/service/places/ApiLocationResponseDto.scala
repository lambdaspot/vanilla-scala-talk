package dev.lambdaspot.location.service.places

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.{JsonCodecMaker, named}
import dev.lambdaspot.infrastructure.wrapper.jsoniter.EnumeratumJsonCodecMaker
import dev.lambdaspot.location.service.*
import enumeratum.*
import enumeratum.EnumEntry.Lowercase

private[location] final case class ApiLocationResponseDto(
    meta: Meta,
    data: Data
)
private[location] object ApiLocationResponseDto {
  given JsonValueCodec[ApiLocationResponseDto] = JsonCodecMaker.make
}

private[location] final case class Meta(
    code: Int,
    message: String,
    details: Vector[String]
)

private[location] final case class Data(
    locations: Vector[Location]
)

private[location] final case class Location(
    @named("location_id") locationId: String,
    name: String,
    distance: Double,
    @named("distance_unit") distanceUnit: DistanceUnit,
    phone: String,
    address: Address,
    services: Vector[String],
    @named("business_hours") businessHours: Vector[BusinessHours]
)

private[location] final case class BusinessHours(
    @named("day_of_week") dayOfWeek: DayOfWeek,
    @named("open_time") openTime: String,
    @named("close_time") closeTime: String
)

private[location] enum DayOfWeek   {
  case Mon, Tue, Wed, Thu, Fri, Sat, Sun
}
private[location] object DayOfWeek {
  given JsonValueCodec[DayOfWeek] = JsonCodecMaker.makeWithoutDiscriminator
}

private[location] final case class Address(
    @named("postal_code") postalCode: String,
    country: String,
    city: String,
    state: Option[String],
    street: String,
    latitude: Double,
    longitude: Double
)

private[location] sealed trait DistanceUnit extends EnumEntry with Lowercase
private[location] object DistanceUnit       extends Enum[DistanceUnit] {
  case object Km extends DistanceUnit
  case object Mi extends DistanceUnit

  val values: IndexedSeq[DistanceUnit] = findValues
  given JsonValueCodec[DistanceUnit]   = EnumeratumJsonCodecMaker.make(DistanceUnit)
}
