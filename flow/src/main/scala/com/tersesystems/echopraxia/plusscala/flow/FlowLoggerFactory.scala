package com.tersesystems.echopraxia.plusscala.flow

import com.tersesystems.echopraxia.api.{Caller, CoreLoggerFactory}

object FlowLoggerFactory {
  val FQCN: String = classOf[DefaultFlowLoggerMethods[_]].getName

  val fieldBuilder: FlowFieldBuilder = DefaultFlowFieldBuilder

  def getLogger(name: String): FlowLogger[FlowFieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    new FlowLogger(core, fieldBuilder)
  }

  def getLogger(clazz: Class[_]): FlowLogger[FlowFieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    new FlowLogger(core, fieldBuilder)
  }

  def getLogger: FlowLogger[FlowFieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, Caller.resolveClassName)
    new FlowLogger(core, fieldBuilder)
  }

}