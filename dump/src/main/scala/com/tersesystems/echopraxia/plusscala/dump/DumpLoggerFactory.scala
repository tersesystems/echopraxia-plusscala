package com.tersesystems.echopraxia.plusscala.dump

import com.tersesystems.echopraxia.api.{Caller, CoreLoggerFactory}
import com.tersesystems.echopraxia.plusscala.api.FieldBuilder

object DumpLoggerFactory {

  val FQCN: String = classOf[DumpLogger[_]].getName

  val fieldBuilder = FieldBuilder

  def getLogger(name: String): DumpLogger[FieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    new DumpLogger(core, fieldBuilder)
  }

  def getLogger(clazz: Class[_]): DumpLogger[FieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    new DumpLogger(core, fieldBuilder)
  }

  def getLogger: DumpLogger[FieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, Caller.resolveClassName)
    new DumpLogger(core, fieldBuilder)
  }

}
