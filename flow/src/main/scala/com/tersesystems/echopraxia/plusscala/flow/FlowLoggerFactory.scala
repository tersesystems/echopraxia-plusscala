package com.tersesystems.echopraxia.plusscala.flow

import com.tersesystems.echopraxia.spi.{Caller, CoreLoggerFactory}

object FlowLoggerFactory {
  val FQCN: String = classOf[DefaultFlowLoggerMethods[_]].getName

  val fieldBuilder: FlowFieldBuilder = DefaultFlowFieldBuilder

  def getLogger(name: String): FlowLogger[FlowFieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    FlowLogger(core, fieldBuilder)
  }

  def getLogger[FB <: FlowFieldBuilder](name: String, fieldBuilder: FB): FlowLogger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    FlowLogger(core, fieldBuilder)
  }

  def getLogger(clazz: Class[_]): FlowLogger[FlowFieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    FlowLogger(core, fieldBuilder)
  }

  def getLogger[FB <: FlowFieldBuilder](clazz: Class[_], fieldBuilder: FB): FlowLogger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    FlowLogger(core, fieldBuilder)
  }

  def getLogger: FlowLogger[FlowFieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, Caller.resolveClassName)
    FlowLogger(core, fieldBuilder)
  }

  def getLogger[FB <: FlowFieldBuilder](fieldBuilder: FB): FlowLogger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, Caller.resolveClassName)
    FlowLogger(core, fieldBuilder)
  }

}
