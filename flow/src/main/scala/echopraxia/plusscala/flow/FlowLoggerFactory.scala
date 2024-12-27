package echopraxia.plusscala.flow

import echopraxia.logging.spi.Caller
import echopraxia.logging.spi.CoreLoggerFactory

object FlowLoggerFactory {
  val FQCN: String = classOf[DefaultFlowLoggerMethods[_]].getName

  val fieldBuilder: DefaultFlowFieldBuilder.type = DefaultFlowFieldBuilder

  def getLogger(name: String): FlowLogger[DefaultFlowFieldBuilder.type] = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    FlowLogger(core, fieldBuilder)
  }

  def getLogger[FB <: FlowFieldBuilder with Singleton](name: String, fieldBuilder: FB): FlowLogger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    FlowLogger(core, fieldBuilder)
  }

  def getLogger(clazz: Class[_]): FlowLogger[DefaultFlowFieldBuilder.type] = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    FlowLogger(core, fieldBuilder)
  }

  def getLogger[FB <: FlowFieldBuilder with Singleton](clazz: Class[_], fieldBuilder: FB): FlowLogger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    FlowLogger(core, fieldBuilder)
  }

  def getLogger: FlowLogger[DefaultFlowFieldBuilder.type] = {
    val core = CoreLoggerFactory.getLogger(FQCN, Caller.resolveClassName)
    FlowLogger(core, fieldBuilder)
  }

  def getLogger[FB <: FlowFieldBuilder with Singleton](fieldBuilder: FB): FlowLogger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, Caller.resolveClassName)
    FlowLogger(core, fieldBuilder)
  }

}
