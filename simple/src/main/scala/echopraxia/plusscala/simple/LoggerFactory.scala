package echopraxia.plusscala.simple

import echopraxia.logging.spi.{Caller, CoreLoggerFactory}

object LoggerFactory {

  def getLogger(): Logger = getLogger(Caller.resolveClassName())

  def getLogger(clazz: Class[_]): Logger = getLogger(clazz.getName)

  def getLogger(name: String): Logger = {
    val core = CoreLoggerFactory.getLogger(classOf[Logger].getName, name)
    new Logger(core)
  }
}
