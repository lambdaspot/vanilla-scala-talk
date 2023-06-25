package dev.lambdaspot.common.errorhandler.aftership

import dev.lambdaspot.TestBase
import dev.lambdaspot.infrastructure.wrapper.sttp.{ClientError, HttpApiResponse, HttpApiSuccess}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, Inside, TryValues}
import sttp.model.StatusCode

/** An example of unit test for one of application components. However, testing
  * implementation details might block future refactorings. The same is easy
  * achievable with integration tests of the controller. See
  * LocationControllerTest where the same test is also on higher level, what
  * makes it a great feature documentation and example of expected application
  * behaviour.
  */
class AftershipErrorHandlerTest extends TestBase {
  describe("The AfterShip Shipping API error handler") {
    it("should handle error with meta code") {
      // Given
      val testedService = new AftershipErrorHandler()
      val apiResponse   = HttpApiSuccess[String](
        code = StatusCode.Ok,
        responseBody = Some("""
            |{
            |  "meta": {
            |    "code": 4500,
            |    "message": "Your account balance is not enough. Please top up the balance in your account page.",
            |    "details": [],
            |    "retryable": false
            |  },
            |  "data": {}
            |}
            |""".stripMargin),
        headers = Nil
      )

      // When
      val result = testedService.handle(apiResponse)

      // Then
      inside(result.failure.exception) { case e: ClientError =>
        e.code shouldBe StatusCode.UnprocessableEntity
        e.reason.value shouldBe "4500: Your account balance is not enough. Please top up the balance in your account page."
        e.retryable shouldBe false
      }
    }

  }
}
