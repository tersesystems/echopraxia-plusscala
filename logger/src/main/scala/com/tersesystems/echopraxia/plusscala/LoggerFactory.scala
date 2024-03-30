package com.tersesystems.echopraxia.plusscala

import com.tersesystems.echopraxia.plusscala.api.PresentationFieldBuilder
import com.tersesystems.echopraxia.spi.Caller
import com.tersesystems.echopraxia.spi.CoreLoggerFactory

/**
 * LoggerFactory for a logger with source code enabled.
 */
object LoggerFactory {
  val FQCN: String = classOf[Logger[_]].getName

  val fieldBuilder: PresentationFieldBuilder = PresentationFieldBuilder

  def getLogger(name: String): Logger[PresentationFieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    Logger(core, fieldBuilder)
  }

  def getLogger(clazz: Class[_]): Logger[PresentationFieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    Logger(core, fieldBuilder)
  }

  def getLogger[FB](name: String, fieldBuilder: FB): Logger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    Logger(core, fieldBuilder)
  }

  def getLogger[FB](clazz: Class[_], fieldBuilder: FB): Logger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    Logger(core, fieldBuilder)
  }

  def getLogger: Logger[PresentationFieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, Caller.resolveClassName)
    Logger(core, fieldBuilder)
  }

  def getLogger[FB](fieldBuilder: FB): Logger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, Caller.resolveClassName)
    Logger(core, fieldBuilder)
  }

}
