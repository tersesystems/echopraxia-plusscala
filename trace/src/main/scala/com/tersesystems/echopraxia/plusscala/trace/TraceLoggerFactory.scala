package com.tersesystems.echopraxia.plusscala.trace

import com.tersesystems.echopraxia.api.{Caller, CoreLoggerFactory}
import com.tersesystems.echopraxia.plusscala.api.FieldBuilder

object TraceLoggerFactory {
  val FQCN: String = classOf[TraceLogger[_]].getName

  val fieldBuilder: FieldBuilder with DefaultArgumentFieldBuilder = new FieldBuilder with DefaultArgumentFieldBuilder {}

  def getLogger(name: String): TraceLogger[FieldBuilder with DefaultArgumentFieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    new TraceLogger(core, fieldBuilder)
  }

  def getLogger(clazz: Class[_]): TraceLogger[FieldBuilder with DefaultArgumentFieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    new TraceLogger(core, fieldBuilder)
  }

  def getLogger: TraceLogger[FieldBuilder with DefaultArgumentFieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, Caller.resolveClassName)
    new TraceLogger(core, fieldBuilder)
  }

}
