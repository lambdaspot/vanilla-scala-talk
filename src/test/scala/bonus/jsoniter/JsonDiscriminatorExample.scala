package bonus.jsoniter

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import dev.lambdaspot.TestBase
import dev.lambdaspot.infrastructure.wrapper.jsoniter.*

import scala.util.Success

class JsonDiscriminatorExample extends TestBase {
  describe("JSON parser") {
    it("should map proper type based on dyscriminator") {
      """{"sound": "woof", "name": "Rex", "breed": "Labrador"}""".fromJson[Animal] shouldBe Success(Dog("Rex", "Labrador"))
      """{"sound": "meow", "name": "Filemon", "age": 1}""".fromJson[Animal] shouldBe Success(Cat("Filemon", 1))
    }
  }
}

sealed trait Animal
final case class Cat(name: String, age: Int)      extends Animal
final case class Dog(name: String, breed: String) extends Animal

object Animal:
  given JsonValueCodec[Animal] =
    JsonCodecMaker.make {
      CodecMakerConfig
        .withDiscriminatorFieldName(Some("sound"))
        .withAdtLeafClassNameMapper {
          JsonCodecMaker.simpleClassName(_) match {
            case "Cat" => "meow"
            case "Dog" => "woof"
          }
        }
    }
