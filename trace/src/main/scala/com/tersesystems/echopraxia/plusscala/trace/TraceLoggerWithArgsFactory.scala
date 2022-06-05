package com.tersesystems.echopraxia.plusscala.trace

import com.tersesystems.echopraxia.api.{Caller, CoreLoggerFactory}

object TraceLoggerWithArgsFactory {
  val FQCN: String = classOf[DefaultTraceLoggerMethods[_]].getName

  val fieldBuilder: TracingWithArgsFieldBuilder = DefaultTracingWithArgsFieldBuilder

  def getLogger(name: String): TraceLoggerWithArgs[TracingWithArgsFieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    new TraceLoggerWithArgs(core, fieldBuilder)
  }

  def getLogger(clazz: Class[_]): TraceLoggerWithArgs[TracingWithArgsFieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    new TraceLoggerWithArgs(core, fieldBuilder)
  }

  def getLogger: TraceLoggerWithArgs[TracingWithArgsFieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, Caller.resolveClassName)
    new TraceLoggerWithArgs(core, fieldBuilder)
  }

}
