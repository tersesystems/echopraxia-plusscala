package com.tersesystems.echopraxia.plusscala.simple

import com.tersesystems.echopraxia.plusscala.api.FieldBuilder
import com.tersesystems.echopraxia.spi.{Caller, CoreLoggerFactory, Utilities}

object LoggerFactory {

  def getLogger(): Logger = getLogger(Caller.resolveClassName())

  def getLogger(clazz: Class[_]): Logger = getLogger(clazz.getName)

  def getLogger(name: String): Logger = {
    val core = CoreLoggerFactory.getLogger(classOf[Logger].getName, name)
    new Logger(core)
  }
}
