package com.tersesystems.echopraxia.plusscala.nameof

import com.tersesystems.echopraxia.spi.{Caller, CoreLoggerFactory}
import com.tersesystems.echopraxia.plusscala.api.FieldBuilder

object NameOfLoggerFactory {

  val FQCN: String = classOf[NameOfLogger[_]].getName

  private val fieldBuilder = FieldBuilder

  def getLogger(name: String): NameOfLogger[FieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    new NameOfLogger(core, fieldBuilder)
  }

  def getLogger(clazz: Class[_]): NameOfLogger[FieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    new NameOfLogger(core, fieldBuilder)
  }

  def getLogger: NameOfLogger[FieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, Caller.resolveClassName)
    new NameOfLogger(core, fieldBuilder)
  }

}
