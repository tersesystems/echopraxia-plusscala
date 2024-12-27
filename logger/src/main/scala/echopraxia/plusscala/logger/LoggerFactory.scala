package echopraxia.plusscala.logger

import echopraxia.logging.spi.{Caller, CoreLoggerFactory}
import echopraxia.plusscala.api.FieldBuilder

/**
 * LoggerFactory for a logger with source code enabled.
 */
object LoggerFactory {
  val FQCN: String = classOf[Logger[_]].getName

  val fieldBuilder: FieldBuilder.type = FieldBuilder

  def getLogger(name: String): Logger[FieldBuilder.type] = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    Logger(core, fieldBuilder)
  }

  def getLogger(clazz: Class[_]): Logger[FieldBuilder.type] = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    Logger(core, fieldBuilder)
  }

  def getLogger[FB <: Singleton](name: String, fieldBuilder: FB): Logger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    Logger(core, fieldBuilder)
  }

  def getLogger[FB <: Singleton](clazz: Class[_], fieldBuilder: FB): Logger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    Logger(core, fieldBuilder)
  }

  def getLogger: Logger[FieldBuilder.type] = {
    val core = CoreLoggerFactory.getLogger(FQCN, Caller.resolveClassName)
    Logger(core, fieldBuilder)
  }
}
