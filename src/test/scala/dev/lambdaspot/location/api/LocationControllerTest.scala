package dev.lambdaspot.location.api

import com.amazonaws.services.lambda.runtime.Context
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import dev.lambdaspot
import dev.lambdaspot.infrastructure.awsapigateway
import dev.lambdaspot.infrastructure.wrapper.sttp.ClientError
import dev.lambdaspot.location.api.LocationControllerTest.*
import dev.lambdaspot.{TestBase, getFixtureOrFail}
import org.mockito.Mockito.mock
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, Inside, TryValues}
import sttp.model.StatusCode

class LocationControllerTest extends TestBase {

  private lazy val underTest  = LocationController.entryPoint
  private val wireMockServer  = new WireMockServer()
  private val dummyAwsContext = mock(classOf[Context])

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    wireMockServer.start()
    System.setProperty("LOCATIONS_API_URL", wireMockServer.baseUrl())
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    wireMockServer.stop()
    System.clearProperty("LOCATIONS_API_URL")
  }

  describe("An endpoint for getting carrier drop-off locations") {

    val validRequest = awsapigateway.request(
      method = "GET",
      queryStringParameters = Map("courierSlug" -> "evri", "country" -> "GBR")
    )

    it("should handle a successful response from the underlying locations API") {
      // Given
      stubFor {
        get("/?slug=evri&country=GBR").willReturn {
          aResponse()
            .withStatus(200)
            .withBody(SuccessfulApiResponse)
        }
      }

      // When
      val result = underTest.run(validRequest, dummyAwsContext)

      // Then
      val expectedResponse = LocationResponseDto(
        Vector(
          LocationDto("S12419", "Co-Op", "02078 394570", AddressDto("WC2R 0RG", "GBR", "London", 51.5083149, -0.1263478), true),
          LocationDto("S23765", "Tesco Metro", "0345 677 9173", AddressDto("WC2E 9EQ", "GBR", "London", 51.51111, -0.12517), true),
          LocationDto("S15011", "Crispins Food & Wine", "02072401624", AddressDto("W1D 5EA", "GBR", "London", 51.51215, -0.13129), true)
        )
      )
      result.success.value shouldBe expectedResponse
    }

    it("should handle a failure response from the underlying locations API") {
      // Given
      stubFor {
        get("/?slug=evri&country=GBR").willReturn {
          aResponse()
            .withStatus(401)
            .withBody(Failure401ApiResponse)
        }
      }

      // When
      val result = underTest.run(validRequest, dummyAwsContext)

      // Then
      inside(result.failure.exception) { case error: ClientError =>
        error.code shouldBe StatusCode.Unauthorized
        error.getMessage should include("The API key is invalid")
      }
    }

    describe("should handle processing error from the underlying locations API") {
      final case class TestScenario(metaCode: Int, apiResponse: String, expectedStatus: StatusCode, message: String)

      for {
        scenario <- Seq(
                      TestScenario(
                        4155,
                        Failure4155ApiResponse,
                        StatusCode.UnprocessableEntity,
                        "Access to shipper_account locked during manifest/cancel-label operation."
                      ),
                      TestScenario(
                        4161,
                        Failure4161ApiResponse,
                        StatusCode.UnprocessableEntity,
                        "Your card is declined by payment gateway."
                      ),
                      TestScenario(
                        4153,
                        Failure4153ApiResponse,
                        StatusCode.BadRequest,
                        "Item does not exist."
                      ),
                      TestScenario(
                        4101,
                        Failure4101ApiResponse,
                        StatusCode.InternalServerError,
                        "Internal Error, please try again."
                      )
                    )
      } it(s"for meta code #${scenario.metaCode}") {
        // Given
        stubFor {
          get("/?slug=evri&country=GBR").willReturn {
            aResponse()
              .withStatus(200)
              .withBody(scenario.apiResponse)
          }
        }

        // When
        val result = underTest.run(validRequest, dummyAwsContext)

        // Then
        inside(result.failure.exception) { case error: ClientError =>
          error.code shouldBe scenario.expectedStatus
          error.getMessage should include(scenario.message)
        }
      }
    }

    it("should reject an invalid request") {
      // Given
      val invalidRequest = validRequest.copy(queryStringParameters = Map("courierSlug" -> "evri", "country" -> "A"))

      // When
      val result = underTest.run(invalidRequest, dummyAwsContext)

      // Then
      inside(result.failure.exception) { case error: IllegalArgumentException =>
        error.getMessage should be(
          "All letters should be upper cased & Should have a minimum length of 3 & Should have a maximum length of 3"
        )
      }
    }

  }
}

object LocationControllerTest {
  private val SuccessfulApiResponse  = getFixtureOrFail("location/successExample.json")
  private val Failure401ApiResponse  = getFixtureOrFail("location/errorApiFailureExample.json")
  private val Failure4155ApiResponse = getFixtureOrFail("location/errorProcessingFailureExample1.json")
  private val Failure4161ApiResponse = getFixtureOrFail("location/errorProcessingFailureExample2.json")
  private val Failure4153ApiResponse = getFixtureOrFail("location/errorProcessingFailureExample3.json")
  private val Failure4101ApiResponse = getFixtureOrFail("location/errorProcessingFailureExample4.json")
}
