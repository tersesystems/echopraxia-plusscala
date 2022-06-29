package com.tersesystems.echopraxia.plusscala.async

import com.tersesystems.echopraxia.api.{CoreLogger, FieldBuilderResult, Utilities}
import com.tersesystems.echopraxia.plusscala.api.{AbstractLoggerSupport, Condition, LoggerSupport}

import java.util.concurrent.Executor
import scala.compat.java8.FunctionConverters._

/**
 * Async Logger with source code enabled.
 */
class AsyncLogger[FB](core: CoreLogger, fieldBuilder: FB)
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

  def withExecutor(executor: Executor): AsyncLogger[FB] = {
    newLogger(newCoreLogger = core.withExecutor(executor))
  }

  override def withThreadContext: AsyncLogger[FB] = newLogger(
    newCoreLogger = core.withThreadContext(Utilities.threadContext())
  )

  override def withFieldBuilder[T](newBuilder: T): AsyncLogger[T] =
    newLogger(newFieldBuilder = newBuilder)

  @inline
  private def newLogger[T](
      newCoreLogger: CoreLogger = core,
      newFieldBuilder: T = fieldBuilder
  ): AsyncLogger[T] =
    new AsyncLogger[T](newCoreLogger, newFieldBuilder)

  class NeverAsyncLogger(core: CoreLogger, fieldBuilder: FB) extends AsyncLogger[FB](core, fieldBuilder) {

    override def ifTraceEnabled(consumer: Handle => Unit): Unit                                  = {}
    override def ifTraceEnabled(condition: Condition)(consumer: Handle => Unit): Unit            = {}
    override def trace(message: String): Unit                                                    = {}
    override def trace(message: String, f: FB => FieldBuilderResult): Unit                       = {}
    override def trace(message: String, e: Throwable): Unit                                      = {}
    override def trace(condition: Condition, message: String): Unit                              = {}
    override def trace(condition: Condition, message: String, f: FB => FieldBuilderResult): Unit = {}
    override def trace(condition: Condition, message: String, e: Throwable): Unit                = {}

    override def ifDebugEnabled(consumer: Handle => Unit): Unit                                  = {}
    override def ifDebugEnabled(condition: Condition)(consumer: Handle => Unit): Unit            = {}
    override def debug(message: String): Unit                                                    = {}
    override def debug(message: String, f: FB => FieldBuilderResult): Unit                       = {}
    override def debug(message: String, e: Throwable): Unit                                      = {}
    override def debug(condition: Condition, message: String): Unit                              = {}
    override def debug(condition: Condition, message: String, f: FB => FieldBuilderResult): Unit = {}
    override def debug(condition: Condition, message: String, e: Throwable): Unit                = {}

    override def ifInfoEnabled(consumer: Handle => Unit): Unit                                  = {}
    override def ifInfoEnabled(condition: Condition)(consumer: Handle => Unit): Unit            = {}
    override def info(message: String): Unit                                                    = {}
    override def info(message: String, f: FB => FieldBuilderResult): Unit                       = {}
    override def info(message: String, e: Throwable): Unit                                      = {}
    override def info(condition: Condition, message: String): Unit                              = {}
    override def info(condition: Condition, message: String, f: FB => FieldBuilderResult): Unit = {}
    override def info(condition: Condition, message: String, e: Throwable): Unit                = {}

    override def ifWarnEnabled(consumer: Handle => Unit): Unit                                  = {}
    override def ifWarnEnabled(condition: Condition)(consumer: Handle => Unit): Unit            = {}
    override def warn(message: String): Unit                                                    = {}
    override def warn(message: String, f: FB => FieldBuilderResult): Unit                       = {}
    override def warn(message: String, e: Throwable): Unit                                      = {}
    override def warn(condition: Condition, message: String): Unit                              = {}
    override def warn(condition: Condition, message: String, f: FB => FieldBuilderResult): Unit = {}
    override def warn(condition: Condition, message: String, e: Throwable): Unit                = {}

    override def ifErrorEnabled(consumer: Handle => Unit): Unit                                  = {}
    override def ifErrorEnabled(condition: Condition)(consumer: Handle => Unit): Unit            = {}
    override def error(message: String): Unit                                                    = {}
    override def error(message: String, f: FB => FieldBuilderResult): Unit                       = {}
    override def error(message: String, e: Throwable): Unit                                      = {}
    override def error(condition: Condition, message: String): Unit                              = {}
    override def error(condition: Condition, message: String, f: FB => FieldBuilderResult): Unit = {}
    override def error(condition: Condition, message: String, e: Throwable): Unit                = {}
  }
}
