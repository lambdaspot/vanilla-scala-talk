package dev.lambdaspot.infrastructure.wrapper.awslambda

import dev.lambdaspot.aws.lambda.events.ApiGatewayProxiedRequest
import dev.lambdaspot.infrastructure.wrapper.awslambda.ParameterParsingOps.hasParameter
import dev.lambdaspot.infrastructure.wrapper.scalastdlib.*
import io.github.iltotore.iron.Constraint

import scala.util.{Failure, Success, Try}

extension (request: ApiGatewayProxiedRequest) {

  def parseQueryStringParam(key: String): Try[String] =
    (for {
      value          <- request.queryStringParameters.get(key)
      validatedValue <- Option(value).filter(_.nonEmpty)
    } yield validatedValue)
      .orFail(s"Missing query string parameter. key=[$key]")

  def parseOptionalQueryStringParam(paramName: String): Try[Option[String]] =
    Try(Some(request.queryStringParameters).flatMap(hasParameter(_, paramName)))

}

private[awslambda] object ParameterParsingOps {

  val hasParameter: (Map[String, String], String) => Option[String] =
    (params, paramName) =>
      Option
        .when(params.isDefinedAt(paramName))(Option(params(paramName)))
        .filter(_.nonEmpty)
        .flatten
}
