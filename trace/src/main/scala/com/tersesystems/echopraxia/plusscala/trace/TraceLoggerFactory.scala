package com.tersesystems.echopraxia.plusscala.trace

import com.tersesystems.echopraxia.api.{Caller, CoreLoggerFactory}

object TraceLoggerFactory {
  val FQCN: String = classOf[TraceLogger[_]].getName

  val fieldBuilder: TracingFieldBuilder = DefaultTracingFieldBuilder

  def getLogger(name: String): TraceLogger[TracingFieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    new TraceLogger(core, fieldBuilder)
  }

  def getLogger(clazz: Class[_]): TraceLogger[TracingFieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    new TraceLogger(core, fieldBuilder)
  }

  def getLogger: TraceLogger[TracingFieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, Caller.resolveClassName)
    new TraceLogger(core, fieldBuilder)
  }

}
