package bonus.jsoniter

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import com.softwaremill.quicklens.*
import dev.lambdaspot.infrastructure.wrapper.jsoniter.*
import dev.lambdaspot.{TestBase, getFixtureOrFail}
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.iltotore.iron.jsoniter.makeCodec

import scala.util.Try

/** https://iltotore.github.io/iron/docs/overview.html */
class JsonValidationWithIronExample extends TestBase {
  private val data: String = getFixtureOrFail("bonus/musicBand.json")

  describe("Music bands catalogue parser") {

    it("should deserialize valid data") {
      data.fromJson[Band].success.value.name shouldBe "Vader"
    }

    it("should reject invalid data") {
      val invalidJson: String = data.replace("Piotr Wiwczarek", "")

      invalidJson.fromJson[Band].failure.exception.getMessage should include(
        "Band member should be of length between 1 and 50"
      )
    }

  }
}

// ### Model

final case class Band(name: String, members: List[Member], albums: List[Album])
object Band:
  given JsonValueCodec[Band] = JsonCodecMaker.make

final case class Member(name: BandMember, instrument: Instrument)
final case class Album(name: String, year: Int, recordings: List[Recording])
final case class Recording(name: String, duration: AlbumDuration, trackNumber: TrackNumber)

// ### Value classes

// Manually creating codecs for the underlying types since the automatic derivation doesn't work in Scala 3
// when the types are within case classes (see: https://github.com/plokhotnyuk/jsoniter-scala#known-issues).

type MediumLength = MinLength[1] & MaxLength[50]

opaque type BandMember = String :| (MediumLength DescribedAs "Band member should be of length between 1 and 50")
object BandMember extends RefinedTypeOps[BandMember]:
  given JsonValueCodec[BandMember] = makeCodec

opaque type Instrument = String :| MediumLength
object Instrument extends RefinedTypeOps[Instrument]:
  given JsonValueCodec[Instrument] = makeCodec

opaque type AlbumDuration = Int :| Positive
object AlbumDuration extends RefinedTypeOps[AlbumDuration]:
  given JsonValueCodec[AlbumDuration] = makeCodec

opaque type TrackNumber = Int :| Greater[0]
object TrackNumber extends RefinedTypeOps[TrackNumber]:
  given JsonValueCodec[TrackNumber] = makeCodec
