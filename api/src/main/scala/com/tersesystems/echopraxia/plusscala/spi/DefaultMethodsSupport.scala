package com.tersesystems.echopraxia.plusscala.spi

import com.tersesystems.echopraxia.spi.CoreLogger

trait DefaultMethodsSupport[FB] {
  def name: String

  def core: CoreLogger

  def fieldBuilder: FB
}
