package dev.lambdaspot.infrastructure.wrapper.jsoniter

import com.github.plokhotnyuk.jsoniter_scala.core.{JsonCodec, JsonReader, JsonWriter}
import enumeratum.values.*
import enumeratum.{Enum, EnumEntry}

import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

/** EnumeratumJsonCodecMaker - an Enumeratum integration for Jsoniter.<br>We
  * generate a codec in a similar way to a typical Jsoniter one.<br> Note that
  * in this case we use `EnumeratumJsonCodecMaker` instead of
  * `JsonCodecMaker`.<br> See `EnumeratumCodecTest` and
  * `EnumeratumCodecValueEnumTest` for more examples.<br><br> Example: <p>
  * {{{
  * import enumeratum._
  *
  * sealed abstract class Language extends EnumEntry
  * object Language extends Enum[Language] {
  *
  *   case object EN extends Language
  *   case object DE extends Language
  *   case object ES extends Language
  *   case object PT extends Language
  *   case object FR extends Language
  *
  *   implicit val codec: JsonValueCodec[Language] =
  *       EnumeratumJsonCodecMaker.make(Language)
  *   val values: IndexedSeq[Language] = findValues
  * }
  * }}}
  * All Enumeratum types are supported: `EnumEntry`, `IntEnumEntry`,
  * `LongEnumEntry`, `ShortEnumEntry`, `StringEnumEntry`, `ByteEnumEntry`,
  * `CharEnumEntry`. <br> See `EnumTest` and `ValueEnumTest` for examples.
  */
object EnumeratumJsonCodecMaker {
  def make[E <: EnumEntry](subject: Enum[E]): JsonCodec[E]             = new EnumeratumCodec[E](subject)
  def make[E <: IntEnumEntry](subject: IntEnum[E]): JsonCodec[E]       = new IntEnumeratumCodec[E](subject)
  def make[E <: LongEnumEntry](subject: LongEnum[E]): JsonCodec[E]     = new LongEnumeratumCodec[E](subject)
  def make[E <: ShortEnumEntry](subject: ShortEnum[E]): JsonCodec[E]   = new ShortEnumeratumCodec[E](subject)
  def make[E <: StringEnumEntry](subject: StringEnum[E]): JsonCodec[E] = new StringEnumeratumCodec[E](subject)
  def make[E <: ByteEnumEntry](subject: ByteEnum[E]): JsonCodec[E]     = new ByteEnumeratumCodec[E](subject)
  def make[E <: CharEnumEntry](subject: CharEnum[E]): JsonCodec[E]     = new CharEnumeratumCodec[E](subject)
}

// warnings suppressed, as it's a low level code in 3rd party library manner
@SuppressWarnings(Array("org.wartremover.warts.Null", "org.wartremover.warts.AsInstanceOf"))
object EnumeratumCodecBase {
  def nullValue[E]: E = null.asInstanceOf[E]

  def decodeValue[T, E <: ValueEnumEntry[T]](
      enumeration: ValueEnum[T, E],
      readValueFunction: () => T,
      in: JsonReader,
      default: E
  ): E = {
    if (in.isNextToken('n')) {
      in.rollbackToken()
      in.readNullOrError(default, "expected number value or null")
    } else {
      in.rollbackToken()
      findValueOrThrowError(enumeration, in, readValueFunction())
    }
  }

  def findValueOrThrowError[T, E <: ValueEnumEntry[T]](enumeration: ValueEnum[T, E], in: JsonReader, v: T): E =
    Try(enumeration.withValue(v))
      .fold(e => in.enumValueError(e.getMessage), s => s)

  def encodeValue[E <: ValueEnumEntry[_]](e: E, out: JsonWriter, writeValueFunction: () => Unit): Unit =
    if (e != null) writeValueFunction() else out.writeNull()

}

final private[infrastructure] class EnumeratumCodec[E <: EnumEntry](enumeration: Enum[E]) extends JsonCodec[E] {
  override def decodeKey(in: JsonReader): E =
    in.readKeyAsString()
      .pipe(k => matchingEnumOrError(in, k))

  override def encodeKey(enumeration: E, out: JsonWriter): Unit =
    enumEntryName(enumeration).pipe(out.writeKey)

  private def enumEntryName(enumeration: E) =
    if (enumeration != null) enumeration.entryName else null

  override def decodeValue(in: JsonReader, default: E): E =
    in.readString(null) match {
      case null => nullValue
      case v    => matchingEnumOrError(in, v)
    }

  private def matchingEnumOrError(in: JsonReader, value: String): E =
    Try(enumeration.withName(value))
      .fold(e => in.enumValueError(e.getMessage), s => s)

  override def encodeValue(enumeration: E, out: JsonWriter): Unit =
    enumEntryName(enumeration).pipe(out.writeVal)

  override def nullValue: E = EnumeratumCodecBase.nullValue
}

final private[infrastructure] class IntEnumeratumCodec[E <: IntEnumEntry](enumeration: IntEnum[E]) extends JsonCodec[E] {
  override def decodeKey(in: JsonReader): E =
    in.readKeyAsInt()
      .pipe(EnumeratumCodecBase.findValueOrThrowError(enumeration, in, _))

  override def encodeKey(e: E, out: JsonWriter): Unit = out.writeKey(e.value)

  override def decodeValue(in: JsonReader, default: E): E =
    EnumeratumCodecBase.decodeValue(enumeration, () => in.readInt(), in, default)

  override def encodeValue(e: E, out: JsonWriter): Unit =
    EnumeratumCodecBase.encodeValue(e, out, () => out.writeVal(e.value))

  override def nullValue: E = EnumeratumCodecBase.nullValue
}

final private[infrastructure] class LongEnumeratumCodec[E <: LongEnumEntry](enumeration: LongEnum[E]) extends JsonCodec[E] {
  override def decodeKey(in: JsonReader): E =
    in.readKeyAsLong()
      .pipe(EnumeratumCodecBase.findValueOrThrowError(enumeration, in, _))

  override def encodeKey(e: E, out: JsonWriter): Unit = out.writeKey(e.value)

  override def decodeValue(in: JsonReader, default: E): E =
    EnumeratumCodecBase.decodeValue(enumeration, () => in.readLong(), in, default)

  override def encodeValue(e: E, out: JsonWriter): Unit =
    EnumeratumCodecBase.encodeValue(e, out, () => out.writeVal(e.value))

  override def nullValue: E = EnumeratumCodecBase.nullValue
}

final private[infrastructure] class ShortEnumeratumCodec[E <: ShortEnumEntry](enumeration: ShortEnum[E]) extends JsonCodec[E] {
  override def decodeKey(in: JsonReader): E =
    in.readKeyAsShort()
      .pipe(EnumeratumCodecBase.findValueOrThrowError(enumeration, in, _))

  override def encodeKey(e: E, out: JsonWriter): Unit = out.writeKey(e.value)

  override def decodeValue(in: JsonReader, default: E): E =
    EnumeratumCodecBase.decodeValue(enumeration, () => in.readShort(), in, default)

  override def encodeValue(e: E, out: JsonWriter): Unit =
    EnumeratumCodecBase.encodeValue(e, out, () => out.writeVal(e.value))

  override def nullValue: E = EnumeratumCodecBase.nullValue
}

final private[infrastructure] class StringEnumeratumCodec[E <: StringEnumEntry](enumeration: StringEnum[E]) extends JsonCodec[E] {
  override def decodeKey(in: JsonReader): E =
    in.readKeyAsString()
      .pipe(EnumeratumCodecBase.findValueOrThrowError(enumeration, in, _))

  override def encodeKey(e: E, out: JsonWriter): Unit = out.writeKey(e.value)

  override def decodeValue(in: JsonReader, default: E): E =
    EnumeratumCodecBase.decodeValue(enumeration, () => in.readString(null), in, default)

  override def encodeValue(e: E, out: JsonWriter): Unit =
    EnumeratumCodecBase.encodeValue(e, out, () => out.writeVal(e.value))

  override def nullValue: E = EnumeratumCodecBase.nullValue
}

final private[infrastructure] class ByteEnumeratumCodec[E <: ByteEnumEntry](enumeration: ByteEnum[E]) extends JsonCodec[E] {
  override def decodeKey(in: JsonReader): E =
    in.readKeyAsByte()
      .pipe(EnumeratumCodecBase.findValueOrThrowError(enumeration, in, _))

  override def encodeKey(e: E, out: JsonWriter): Unit = out.writeKey(e.value)

  override def decodeValue(in: JsonReader, default: E): E =
    EnumeratumCodecBase.decodeValue(enumeration, () => in.readByte(), in, default)

  override def encodeValue(e: E, out: JsonWriter): Unit =
    EnumeratumCodecBase.encodeValue(e, out, () => out.writeVal(e.value))

  override def nullValue: E = EnumeratumCodecBase.nullValue
}

final private[infrastructure] class CharEnumeratumCodec[E <: CharEnumEntry](enumeration: CharEnum[E]) extends JsonCodec[E] {
  override def decodeKey(in: JsonReader): E =
    in.readKeyAsChar()
      .pipe(EnumeratumCodecBase.findValueOrThrowError(enumeration, in, _))

  override def encodeKey(e: E, out: JsonWriter): Unit = out.writeKey(e.value)

  override def decodeValue(in: JsonReader, default: E): E =
    EnumeratumCodecBase.decodeValue(enumeration, () => in.readChar(), in, default)

  override def encodeValue(e: E, out: JsonWriter): Unit =
    EnumeratumCodecBase.encodeValue(e, out, () => out.writeVal(e.value))

  override def nullValue: E = EnumeratumCodecBase.nullValue
}
