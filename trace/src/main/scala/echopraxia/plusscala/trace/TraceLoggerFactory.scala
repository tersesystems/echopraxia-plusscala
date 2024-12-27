package echopraxia.plusscala.trace

import echopraxia.logging.spi.Caller
import echopraxia.logging.spi.CoreLoggerFactory

object TraceLoggerFactory {
  val FQCN: String = classOf[DefaultTraceLoggerMethods[?]].getName

  val fieldBuilder: TraceFieldBuilder = DefaultTraceFieldBuilder

  def getLogger(name: String): TraceLogger[TraceFieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    TraceLogger(core, fieldBuilder)
  }

  def getLogger[FB <: TraceFieldBuilder](name: String, fieldBuilder: FB): TraceLogger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    TraceLogger(core, fieldBuilder)
  }

  def getLogger(clazz: Class[?]): TraceLogger[TraceFieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    TraceLogger(core, fieldBuilder)
  }

  def getLogger[FB <: TraceFieldBuilder](clazz: Class[?], fieldBuilder: FB): TraceLogger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    TraceLogger(core, fieldBuilder)
  }

  def getLogger: TraceLogger[TraceFieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, Caller.resolveClassName)
    TraceLogger(core, fieldBuilder)
  }

  def getLogger[FB <: TraceFieldBuilder](fieldBuilder: FB): TraceLogger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, Caller.resolveClassName)
    TraceLogger(core, fieldBuilder)
  }

}
