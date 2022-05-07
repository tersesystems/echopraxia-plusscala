package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.CoreLogger

trait DefaultMethodsSupport[FB] {
  def name: String

  def core: CoreLogger

  def fieldBuilder: FB
}
