package com.tersesystems.echopraxia.plusscala.nameof

import com.tersesystems.echopraxia.plusscala.api.FieldBuilder
import echopraxia.logging.spi.{Caller, CoreLoggerFactory}

object NameOfLoggerFactory {

  val FQCN: String = classOf[NameOfLogger[_]].getName

  private val fieldBuilder = FieldBuilder

  def getLogger(name: String): NameOfLogger[FieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    new NameOfLogger(core, fieldBuilder)
  }

  def getLogger[FB <: FieldBuilder](name: String, fieldBuilder: FB): NameOfLogger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    new NameOfLogger(core, fieldBuilder)
  }

  def getLogger(clazz: Class[_]): NameOfLogger[FieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    new NameOfLogger(core, fieldBuilder)
  }

  def getLogger[FB <: FieldBuilder](clazz: Class[_], fieldBuilder: FB): NameOfLogger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    new NameOfLogger(core, fieldBuilder)
  }

  def getLogger: NameOfLogger[FieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, Caller.resolveClassName)
    new NameOfLogger(core, fieldBuilder)
  }

  def getLogger[FB <: FieldBuilder](fieldBuilder: FB): NameOfLogger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, Caller.resolveClassName)
    new NameOfLogger(core, fieldBuilder)
  }

}
