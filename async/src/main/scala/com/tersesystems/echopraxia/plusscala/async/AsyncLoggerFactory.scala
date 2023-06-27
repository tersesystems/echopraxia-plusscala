package com.tersesystems.echopraxia.plusscala.async

import com.tersesystems.echopraxia.spi.{Caller, CoreLoggerFactory}
import com.tersesystems.echopraxia.plusscala.api.FieldBuilder

/**
 * Async Logger Factory with source code enabled.
 */
object AsyncLoggerFactory {
  val FQCN: String = classOf[DefaultAsyncLoggerMethods[_]].getName

  val fieldBuilder: FieldBuilder = new FieldBuilder {}

  def getLogger(name: String): AsyncLogger[FieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    AsyncLogger(core, fieldBuilder)
  }

  def getLogger[FB](name: String, fieldBuilder: FB): AsyncLogger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    AsyncLogger(core, fieldBuilder)
  }

  def getLogger(clazz: Class[_]): AsyncLogger[FieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    AsyncLogger(core, fieldBuilder)
  }

  def getLogger[FB](clazz: Class[_], fieldBuilder: FB): AsyncLogger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    AsyncLogger(core, fieldBuilder)
  }

  def getLogger: AsyncLogger[FieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, Caller.resolveClassName)
    AsyncLogger(core, fieldBuilder)
  }

  def getLogger[FB](fieldBuilder: FB) : AsyncLogger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, Caller.resolveClassName)
    AsyncLogger(core, fieldBuilder)
  }

}
