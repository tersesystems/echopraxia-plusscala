package com.tersesystems.echopraxia.plusscala

import com.tersesystems.echopraxia.api.{CoreLogger, FieldBuilderResult, Utilities}
import com.tersesystems.echopraxia.plusscala.api.{AbstractLoggerSupport, Condition, LoggerSupport}

import scala.compat.java8.FunctionConverters.enrichAsJavaFunction

/**
 *
 */
class Logger[FB](core: CoreLogger, fieldBuilder: FB)
    extends AbstractLoggerSupport(core, fieldBuilder)
    with LoggerSupport[FB]
    with DefaultLoggerMethods[FB] {

  override def name: String = core.getName

  override def withCondition(condition: Condition): Logger[FB] = {
    condition match {
      case Condition.always =>
        this
      case Condition.never =>
        new NeverLogger(core, fieldBuilder)
      case other =>
        newLogger(newCoreLogger = core.withCondition(other.asJava))
    }
  }

  override def withFields(f: FB => FieldBuilderResult): Logger[FB] = {
    newLogger(newCoreLogger = core.withFields(f.asJava, fieldBuilder))
  }

  override def withThreadContext: Logger[FB] = {
    newLogger(
      newCoreLogger = core.withThreadContext(Utilities.threadContext())
    )
  }

  override def withFieldBuilder[NEWFB](newFieldBuilder: NEWFB): Logger[NEWFB] = {
    newLogger(newFieldBuilder = newFieldBuilder)
  }

  @inline
  private def newLogger[T](
      newCoreLogger: CoreLogger = core,
      newFieldBuilder: T = fieldBuilder
  ): Logger[T] =
    new Logger[T](newCoreLogger, newFieldBuilder)

  class NeverLogger(core: CoreLogger, fieldBuilder: FB) extends Logger[FB](core: CoreLogger, fieldBuilder: FB) {
    override def isTraceEnabled: Boolean                                                                                           = false
    override def isTraceEnabled(condition: Condition): Boolean                                                                     = false
    override def trace(message: String): Unit                                     = {}
    override def trace(message: String, e: Throwable): Unit                       = {}
    override def trace(message: String, f: FB => FieldBuilderResult): Unit        = {}
    override def trace(condition: Condition, message: String): Unit               = {}
    override def trace(condition: Condition, message: String, e: Throwable): Unit = {}
    override def trace(condition: Condition, message: String, f: FB => FieldBuilderResult): Unit = {}

    override def isDebugEnabled: Boolean                                                                                           = false
    override def isDebugEnabled(condition: Condition): Boolean                                                                     = false
    override def debug(message: String): Unit                                     = {}
    override def debug(message: String, e: Throwable): Unit                       = {}
    override def debug(message: String, f: FB => FieldBuilderResult): Unit        = {}
    override def debug(condition: Condition, message: String): Unit               = {}
    override def debug(condition: Condition, message: String, e: Throwable): Unit = {}
    override def debug(condition: Condition, message: String, f: FB => FieldBuilderResult): Unit = {}

    override def isInfoEnabled: Boolean                                                                                           = false
    override def isInfoEnabled(condition: Condition): Boolean                                                                     = false
    override def info(message: String): Unit                                     = {}
    override def info(message: String, f: FB => FieldBuilderResult): Unit        = {}
    override def info(message: String, e: Throwable): Unit                       = {}
    override def info(condition: Condition, message: String): Unit               = {}
    override def info(condition: Condition, message: String, e: Throwable): Unit = {}
    override def info(condition: Condition, message: String, f: FB => FieldBuilderResult): Unit = {}

    override def isWarnEnabled: Boolean                                                                                           = false
    override def isWarnEnabled(condition: Condition): Boolean                                                                     = false
    override def warn(message: String): Unit                                     = {}
    override def warn(message: String, f: FB => FieldBuilderResult): Unit        = {}
    override def warn(message: String, e: Throwable): Unit                       = {}
    override def warn(condition: Condition, message: String): Unit               = {}
    override def warn(condition: Condition, message: String, e: Throwable): Unit = {}
    override def warn(condition: Condition, message: String, f: FB => FieldBuilderResult): Unit = {}

    override def isErrorEnabled: Boolean                                                                                           = false
    override def isErrorEnabled(condition: Condition): Boolean                                                                     = false
    override def error(message: String): Unit                                     = {}
    override def error(message: String, f: FB => FieldBuilderResult): Unit        = {}
    override def error(message: String, e: Throwable): Unit                       = {}
    override def error(condition: Condition, message: String): Unit               = {}
    override def error(condition: Condition, message: String, e: Throwable): Unit = {}
    override def error(condition: Condition, message: String, f: FB => FieldBuilderResult): Unit = {}
  }
}
