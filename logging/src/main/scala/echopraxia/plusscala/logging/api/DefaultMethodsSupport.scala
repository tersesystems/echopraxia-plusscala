package echopraxia.plusscala.logging.api

import echopraxia.logging.spi.CoreLogger

trait DefaultMethodsSupport[FB] {
  def name: String

  def core: CoreLogger

  def fieldBuilder: FB
}
