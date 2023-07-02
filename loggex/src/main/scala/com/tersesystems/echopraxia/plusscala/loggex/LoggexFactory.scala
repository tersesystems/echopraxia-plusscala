package com.tersesystems.echopraxia.plusscala.loggex

import com.tersesystems.echopraxia.plusscala.api.PresentationFieldBuilder
import com.tersesystems.echopraxia.spi.{Caller, CoreLoggerFactory}

object LoggexFactory {
  val FQCN: String = classOf[Loggex[_]].getName

  val fieldBuilder: PresentationFieldBuilder = PresentationFieldBuilder

  def getLogger(name: String): Loggex[PresentationFieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    new Loggex(core, fieldBuilder)
  }

  def getLogger(clazz: Class[_]): Loggex[PresentationFieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    new Loggex(core, fieldBuilder)
  }

  def getLogger[FB](name: String, fieldBuilder: FB): Loggex[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    new Loggex(core, fieldBuilder)
  }

  def getLogger[FB](clazz: Class[_], fieldBuilder: FB): Loggex[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    new Loggex(core, fieldBuilder)
  }

  def getLogger: Loggex[PresentationFieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, Caller.resolveClassName)
    new Loggex(core, fieldBuilder)
  }

  def getLogger[FB](fieldBuilder: FB): Loggex[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, Caller.resolveClassName)
    new Loggex(core, fieldBuilder)
  }

}
