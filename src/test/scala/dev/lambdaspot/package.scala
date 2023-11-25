package dev

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import dev.lambdaspot.infrastructure.wrapper.jsoniter.*
import org.scalatest.*
import org.scalatest.Assertions.fail
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import scala.io.{Codec, Source}
import scala.util.Try

package object lambdaspot {

  class TestBase
      extends AnyFunSpec
      with BeforeAndAfterAll
      with TryValues
      with OptionValues
      with PartialFunctionValues
      with Matchers
      with Inside

  def getFixtureOrFail(resourcePath: String): String =
    getFixture(resourcePath)
      .getOrElse(fail("Failed to load test data"))

  def getFixtureObjectOrFail[T](resourcePath: String)(using JsonValueCodec[T]): T =
    getFixture(resourcePath)
      .flatMap(_.fromJson[T])
      .fold(e => fail(s"Failed to load test data: ${e.getMessage}"), identity)

  def getFixture(resourcePath: String): Try[String] =
    Try(Source.fromResource(resourcePath)(Codec.UTF8).mkString)

}
