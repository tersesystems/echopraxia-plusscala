package echopraxia.plusscala.flow

import echopraxia.logging.spi.Caller
import echopraxia.logging.spi.CoreLoggerFactory

object FlowLoggerFactory {
  val FQCN: String = classOf[DefaultFlowLoggerMethods[?]].getName

  val fieldBuilder: DefaultFlowFieldBuilder.type = DefaultFlowFieldBuilder

  def getLogger(name: String): FlowLogger[DefaultFlowFieldBuilder.type] = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    FlowLogger(core, fieldBuilder)
  }

  def getLogger[FB <: FlowFieldBuilder & Singleton](name: String, fieldBuilder: FB): FlowLogger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    FlowLogger(core, fieldBuilder)
  }

  def getLogger(clazz: Class[?]): FlowLogger[DefaultFlowFieldBuilder.type] = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    FlowLogger(core, fieldBuilder)
  }

  def getLogger[FB <: FlowFieldBuilder & Singleton](clazz: Class[?], fieldBuilder: FB): FlowLogger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    FlowLogger(core, fieldBuilder)
  }

  def getLogger: FlowLogger[DefaultFlowFieldBuilder.type] = {
    val core = CoreLoggerFactory.getLogger(FQCN, Caller.resolveClassName)
    FlowLogger(core, fieldBuilder)
  }

  def getLogger[FB <: FlowFieldBuilder & Singleton](fieldBuilder: FB): FlowLogger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, Caller.resolveClassName)
    FlowLogger(core, fieldBuilder)
  }

}
