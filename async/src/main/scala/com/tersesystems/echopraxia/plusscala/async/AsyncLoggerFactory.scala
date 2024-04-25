package com.tersesystems.echopraxia.plusscala.async

import com.tersesystems.echopraxia.plusscala.api.FieldBuilder
import com.tersesystems.echopraxia.spi.Caller
import com.tersesystems.echopraxia.spi.CoreLoggerFactory

/**
 * Async Logger Factory with source code enabled.
 */
object AsyncLoggerFactory {
  val FQCN: String = classOf[DefaultAsyncLoggerMethods[_]].getName

  val fieldBuilder: FieldBuilder.type = FieldBuilder

  def getLogger(name: String): AsyncLogger[FieldBuilder.type] = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    AsyncLogger(core, fieldBuilder)
  }

  def getLogger[FB <: Singleton](name: String, fieldBuilder: FB): AsyncLogger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    AsyncLogger(core, fieldBuilder)
  }

  def getLogger(clazz: Class[_]): AsyncLogger[FieldBuilder.type] = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    AsyncLogger(core, fieldBuilder)
  }

  def getLogger[FB <: Singleton](clazz: Class[_], fieldBuilder: FB): AsyncLogger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    AsyncLogger(core, fieldBuilder)
  }

  def getLogger: AsyncLogger[FieldBuilder.type] = {
    val core = CoreLoggerFactory.getLogger(FQCN, Caller.resolveClassName)
    AsyncLogger(core, fieldBuilder)
  }

  def getLogger[FB <: Singleton](fieldBuilder: FB): AsyncLogger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, Caller.resolveClassName)
    AsyncLogger(core, fieldBuilder)
  }

}
