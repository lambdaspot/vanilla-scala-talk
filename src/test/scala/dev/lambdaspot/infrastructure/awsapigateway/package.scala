package dev.lambdaspot.infrastructure

import com.softwaremill.quicklens.*
import dev.lambdaspot.aws.lambda.events.ApiGatewayProxiedRequest
import dev.lambdaspot.getFixtureObjectOrFail

package object awsapigateway {

  def request(
      method: String,
      body: Option[String] = None,
      pathParameters: Map[String, String] = Map.empty,
      queryStringParameters: Map[String, String] = Map.empty
  ): ApiGatewayProxiedRequest = {
    val requestTemplate = getFixtureObjectOrFail[ApiGatewayProxiedRequest]("awsapigateway/event.json")
    // format: off
    val updatedRequest  = 
      modify(requestTemplate)(_.body).setTo(body)
      .modify(_.httpMethod).setTo(method)
      .modify(_.requestContext.httpMethod).setTo(method)
      .modify(_.pathParameters).setTo(pathParameters)
      .modify(_.queryStringParameters).setTo(queryStringParameters)
    // format: on
    updatedRequest
  }

}
