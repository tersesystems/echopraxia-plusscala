package com.tersesystems.echopraxia.plusscala

import com.tersesystems.echopraxia.api.{CoreLogger, FieldBuilderResult, Utilities}
import com.tersesystems.echopraxia.plusscala.api.{AbstractLoggerSupport, Condition, LoggerSupport}

import scala.compat.java8.FunctionConverters.enrichAsJavaFunction

/** 
 * Logger with source code implicit parameters.
 */
final class Logger[FB](core: CoreLogger, fieldBuilder: FB)
    extends AbstractLoggerSupport(core, fieldBuilder)
    with LoggerSupport[FB]
    with DefaultLoggerMethods[FB] {

  @inline
  override def name: String = core.getName

  @inline
  override def withCondition(condition: Condition): Logger[FB] = {
    newLogger(newCoreLogger = core.withCondition(condition.asJava))
  }

  @inline
  override def withFields(f: FB => FieldBuilderResult): Logger[FB] = {
    newLogger(newCoreLogger = core.withFields(f.asJava, fieldBuilder))
  }

  @inline
  override def withThreadContext: Logger[FB] = {
    newLogger(
      newCoreLogger = core.withThreadContext(Utilities.threadContext())
    )
  }

  @inline
  override def withFieldBuilder[NEWFB](newFieldBuilder: NEWFB): Logger[NEWFB] = {
    newLogger(newFieldBuilder = newFieldBuilder)
  }

  @inline
  private def newLogger[T](
      newCoreLogger: CoreLogger = core,
      newFieldBuilder: T = fieldBuilder
  ): Logger[T] =
    new Logger[T](newCoreLogger, newFieldBuilder)

}
