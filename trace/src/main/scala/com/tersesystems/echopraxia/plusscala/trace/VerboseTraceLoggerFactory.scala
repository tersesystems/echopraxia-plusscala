package com.tersesystems.echopraxia.plusscala.trace

import com.tersesystems.echopraxia.api.{Caller, CoreLoggerFactory}

object VerboseTraceLoggerFactory {
  val FQCN: String = classOf[DefaultVerboseTraceLoggerMethods[_]].getName

  val fieldBuilder: VerboseTracingFieldBuilder = DefaultVerboseTracingFieldBuilder

  def getLogger(name: String): VerboseTraceLogger[VerboseTracingFieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    new VerboseTraceLogger(core, fieldBuilder)
  }

  def getLogger(clazz: Class[_]): VerboseTraceLogger[VerboseTracingFieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    new VerboseTraceLogger(core, fieldBuilder)
  }

  def getLogger: VerboseTraceLogger[VerboseTracingFieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, Caller.resolveClassName)
    new VerboseTraceLogger(core, fieldBuilder)
  }

}
