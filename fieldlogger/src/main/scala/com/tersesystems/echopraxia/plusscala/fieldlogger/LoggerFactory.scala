package com.tersesystems.echopraxia.plusscala.fieldlogger

import com.tersesystems.echopraxia.spi.{CoreLogger, CoreLoggerFactory}

object LoggerFactory {
  private val FQCN = classOf[Logger].getName

  def getLogger(clazz: Class[_]): Logger = {
    val core: CoreLogger = CoreLoggerFactory.getLogger(FQCN, clazz)
    new Logger(core)
  }

  def getLogger(name: String): Logger = {
    val core: CoreLogger = CoreLoggerFactory.getLogger(FQCN, name)
    new Logger(core)
  }
}

