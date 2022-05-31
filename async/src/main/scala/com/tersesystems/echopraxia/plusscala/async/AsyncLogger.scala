package com.tersesystems.echopraxia.plusscala.async

import com.tersesystems.echopraxia.api.{CoreLogger, FieldBuilderResult, Utilities}
import com.tersesystems.echopraxia.plusscala.api.{AbstractLoggerSupport, Condition, LoggerSupport, SourceCodeFieldBuilder}
import sourcecode.{Enclosing, File, Line}

import scala.compat.java8.FunctionConverters._

/**
 * Async Logger with source code enabled.
 */
class AsyncLogger[FB <: SourceCodeFieldBuilder](core: CoreLogger, fieldBuilder: FB)
    extends AbstractLoggerSupport[FB](core, fieldBuilder)
    with LoggerSupport[FB]
    with DefaultAsyncLoggerMethods[FB] {

  override def name: String = core.getName

  override def withCondition(scalaCondition: Condition): AsyncLogger[FB] = {
    scalaCondition match {
      case Condition.always =>
        this
      case Condition.never =>
        new NeverAsyncLogger(core, fieldBuilder)
      case other =>
        newLogger(newCoreLogger = core.withCondition(other.asJava))
    }
  }

  override def withFields(f: FB => FieldBuilderResult): AsyncLogger[FB] = {
    newLogger(newCoreLogger = core.withFields[FB](f.asJava, fieldBuilder))
  }

  override def withThreadContext: AsyncLogger[FB] = newLogger(
    newCoreLogger = core.withThreadContext(Utilities.threadContext())
  )

  override def withFieldBuilder[T <: SourceCodeFieldBuilder](newBuilder: T): AsyncLogger[T] =
    newLogger(newFieldBuilder = newBuilder)

  @inline
  private def newLogger[T <: SourceCodeFieldBuilder](
      newCoreLogger: CoreLogger = core,
      newFieldBuilder: T = fieldBuilder
  ): AsyncLogger[T] =
    new AsyncLogger[T](newCoreLogger, newFieldBuilder)

  class NeverAsyncLogger(core: CoreLogger, fieldBuilder: FB) extends AsyncLogger[FB](core, fieldBuilder) {

    override def ifTraceEnabled(consumer: Handle => Unit)(implicit line: Line, file: File, enc: Enclosing): Unit                                  = {}
    override def ifTraceEnabled(condition: Condition)(consumer: Handle => Unit)(implicit line: Line, file: File, enc: Enclosing): Unit            = {}
    override def trace(message: String)(implicit line: Line, file: File, enc: Enclosing): Unit                                                    = {}
    override def trace(message: String, f: FB => FieldBuilderResult)(implicit line: Line, file: File, enc: Enclosing): Unit                       = {}
    override def trace(message: String, e: Throwable)(implicit line: Line, file: File, enc: Enclosing): Unit                                      = {}
    override def trace(condition: Condition, message: String)(implicit line: Line, file: File, enc: Enclosing): Unit                              = {}
    override def trace(condition: Condition, message: String, f: FB => FieldBuilderResult)(implicit line: Line, file: File, enc: Enclosing): Unit = {}
    override def trace(condition: Condition, message: String, e: Throwable)(implicit line: Line, file: File, enc: Enclosing): Unit                = {}

    override def ifDebugEnabled(consumer: Handle => Unit)(implicit line: Line, file: File, enc: Enclosing): Unit                                  = {}
    override def ifDebugEnabled(condition: Condition)(consumer: Handle => Unit)(implicit line: Line, file: File, enc: Enclosing): Unit            = {}
    override def debug(message: String)(implicit line: Line, file: File, enc: Enclosing): Unit                                                    = {}
    override def debug(message: String, f: FB => FieldBuilderResult)(implicit line: Line, file: File, enc: Enclosing): Unit                       = {}
    override def debug(message: String, e: Throwable)(implicit line: Line, file: File, enc: Enclosing): Unit                                      = {}
    override def debug(condition: Condition, message: String)(implicit line: Line, file: File, enc: Enclosing): Unit                              = {}
    override def debug(condition: Condition, message: String, f: FB => FieldBuilderResult)(implicit line: Line, file: File, enc: Enclosing): Unit = {}
    override def debug(condition: Condition, message: String, e: Throwable)(implicit line: Line, file: File, enc: Enclosing): Unit                = {}

    override def ifInfoEnabled(consumer: Handle => Unit)(implicit line: Line, file: File, enc: Enclosing): Unit                                  = {}
    override def ifInfoEnabled(condition: Condition)(consumer: Handle => Unit)(implicit line: Line, file: File, enc: Enclosing): Unit            = {}
    override def info(message: String)(implicit line: Line, file: File, enc: Enclosing): Unit                                                    = {}
    override def info(message: String, f: FB => FieldBuilderResult)(implicit line: Line, file: File, enc: Enclosing): Unit                       = {}
    override def info(message: String, e: Throwable)(implicit line: Line, file: File, enc: Enclosing): Unit                                      = {}
    override def info(condition: Condition, message: String)(implicit line: Line, file: File, enc: Enclosing): Unit                              = {}
    override def info(condition: Condition, message: String, f: FB => FieldBuilderResult)(implicit line: Line, file: File, enc: Enclosing): Unit = {}
    override def info(condition: Condition, message: String, e: Throwable)(implicit line: Line, file: File, enc: Enclosing): Unit                = {}

    override def ifWarnEnabled(consumer: Handle => Unit)(implicit line: Line, file: File, enc: Enclosing): Unit                                  = {}
    override def ifWarnEnabled(condition: Condition)(consumer: Handle => Unit)(implicit line: Line, file: File, enc: Enclosing): Unit            = {}
    override def warn(message: String)(implicit line: Line, file: File, enc: Enclosing): Unit                                                    = {}
    override def warn(message: String, f: FB => FieldBuilderResult)(implicit line: Line, file: File, enc: Enclosing): Unit                       = {}
    override def warn(message: String, e: Throwable)(implicit line: Line, file: File, enc: Enclosing): Unit                                      = {}
    override def warn(condition: Condition, message: String)(implicit line: Line, file: File, enc: Enclosing): Unit                              = {}
    override def warn(condition: Condition, message: String, f: FB => FieldBuilderResult)(implicit line: Line, file: File, enc: Enclosing): Unit = {}
    override def warn(condition: Condition, message: String, e: Throwable)(implicit line: Line, file: File, enc: Enclosing): Unit                = {}

    override def ifErrorEnabled(consumer: Handle => Unit)(implicit line: Line, file: File, enc: Enclosing): Unit                                  = {}
    override def ifErrorEnabled(condition: Condition)(consumer: Handle => Unit)(implicit line: Line, file: File, enc: Enclosing): Unit            = {}
    override def error(message: String)(implicit line: Line, file: File, enc: Enclosing): Unit                                                    = {}
    override def error(message: String, f: FB => FieldBuilderResult)(implicit line: Line, file: File, enc: Enclosing): Unit                       = {}
    override def error(message: String, e: Throwable)(implicit line: Line, file: File, enc: Enclosing): Unit                                      = {}
    override def error(condition: Condition, message: String)(implicit line: Line, file: File, enc: Enclosing): Unit                              = {}
    override def error(condition: Condition, message: String, f: FB => FieldBuilderResult)(implicit line: Line, file: File, enc: Enclosing): Unit = {}
    override def error(condition: Condition, message: String, e: Throwable)(implicit line: Line, file: File, enc: Enclosing): Unit                = {}
  }
}
