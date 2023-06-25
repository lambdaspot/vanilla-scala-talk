package dev.lambdaspot.infrastructure

import dev.lambdaspot.infrastructure.wrapper.scalastdlib.orFail

import scala.util.Try

object SystemPropertyReader {
  private val upperCasedKey: String => String = _.toUpperCase(DEFAULT_LOCALE)

  /** Fetches the optional value of the environment variable or system property
    * by key.
    *
    * @param key
    *   the name of the environment variable or system property
    * @return
    *   environment variable or property value or None if both don't exist.
    */
  def lookupConfiguration(key: String): Try[Option[String]] = Try {
    val env  = Option(System.getenv(upperCasedKey(key)))
    val prop = Option(System.getProperty(upperCasedKey(key)))

    env.fold(prop)(Some(_))
  }

  /** Fetches the mandatory value of the environment variable or system property
    * by key.
    *
    * @param key
    *   the name of the environment variable or system property
    * @return
    *   environment variable or property value or error if both don't exist.
    */
  def lookupConfigOrFail(key: String, errorMessage: String): Try[String] =
    SystemPropertyReader
      .lookupConfiguration(key)
      .flatMap(_.orFail(errorMessage))

}
