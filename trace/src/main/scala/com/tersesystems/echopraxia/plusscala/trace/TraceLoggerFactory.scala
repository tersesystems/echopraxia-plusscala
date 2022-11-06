package com.tersesystems.echopraxia.plusscala.trace

import com.tersesystems.echopraxia.api.{Caller, CoreLoggerFactory}

object TraceLoggerFactory {
  val FQCN: String = classOf[DefaultTraceLoggerMethods[_]].getName

  val fieldBuilder: TraceFieldBuilder = DefaultTraceFieldBuilder

  def getLogger(name: String): TraceLogger[TraceFieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    TraceLogger(core, fieldBuilder)
  }

  def getLogger(clazz: Class[_]): TraceLogger[TraceFieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    TraceLogger(core, fieldBuilder)
  }

  def getLogger: TraceLogger[TraceFieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, Caller.resolveClassName)
    TraceLogger(core, fieldBuilder)
  }

}
