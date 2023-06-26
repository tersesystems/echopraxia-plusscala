package com.tersesystems.echopraxia.plusscala

import com.tersesystems.echopraxia.api.FieldBuilderResult
import com.tersesystems.echopraxia.spi.{CoreLogger, Utilities}
import com.tersesystems.echopraxia.plusscala.api.{Condition, DefaultMethodsSupport, LoggerSupport}

import scala.compat.java8.FunctionConverters.enrichAsJavaFunction

trait Logger[FB] extends LoggerMethods[FB] with LoggerSupport[FB, Logger] with DefaultMethodsSupport[FB]

object Logger {

  def apply[FB](core: CoreLogger, fieldBuilder: FB): Logger[FB] = new Impl[FB](core, fieldBuilder)

  /**
   */
  class Impl[FB](val core: CoreLogger, val fieldBuilder: FB) extends Logger[FB] with DefaultLoggerMethods[FB] {

    override def name: String = core.getName

    override def withCondition(condition: Condition): Logger[FB] = {
      condition match {
        case Condition.always =>
          this
        case Condition.never =>
          NoOp(core, fieldBuilder)
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
      new Impl[T](newCoreLogger, newFieldBuilder)

  }

  trait NoOp[FB] extends Logger[FB] {
    override def isTraceEnabled: Boolean = false

    override def isTraceEnabled(condition: Condition): Boolean = false

    override def trace(message: String): Unit = {}

    override def trace(message: String, e: Throwable): Unit = {}

    override def trace(message: String, f: FB => FieldBuilderResult): Unit = {}

    override def trace(condition: Condition, message: String): Unit = {}

    override def trace(condition: Condition, message: String, e: Throwable): Unit = {}

    override def trace(condition: Condition, message: String, f: FB => FieldBuilderResult): Unit = {}

    override def isDebugEnabled: Boolean = false

    override def isDebugEnabled(condition: Condition): Boolean = false

    override def debug(message: String): Unit = {}

    override def debug(message: String, e: Throwable): Unit = {}

    override def debug(message: String, f: FB => FieldBuilderResult): Unit = {}

    override def debug(condition: Condition, message: String): Unit = {}

    override def debug(condition: Condition, message: String, e: Throwable): Unit = {}

    override def debug(condition: Condition, message: String, f: FB => FieldBuilderResult): Unit = {}

    override def isInfoEnabled: Boolean = false

    override def isInfoEnabled(condition: Condition): Boolean = false

    override def info(message: String): Unit = {}

    override def info(message: String, f: FB => FieldBuilderResult): Unit = {}

    override def info(message: String, e: Throwable): Unit = {}

    override def info(condition: Condition, message: String): Unit = {}

    override def info(condition: Condition, message: String, e: Throwable): Unit = {}

    override def info(condition: Condition, message: String, f: FB => FieldBuilderResult): Unit = {}

    override def isWarnEnabled: Boolean = false

    override def isWarnEnabled(condition: Condition): Boolean = false

    override def warn(message: String): Unit = {}

    override def warn(message: String, f: FB => FieldBuilderResult): Unit = {}

    override def warn(message: String, e: Throwable): Unit = {}

    override def warn(condition: Condition, message: String): Unit = {}

    override def warn(condition: Condition, message: String, e: Throwable): Unit = {}

    override def warn(condition: Condition, message: String, f: FB => FieldBuilderResult): Unit = {}

    override def isErrorEnabled: Boolean = false

    override def isErrorEnabled(condition: Condition): Boolean = false

    override def error(message: String): Unit = {}

    override def error(message: String, f: FB => FieldBuilderResult): Unit = {}

    override def error(message: String, e: Throwable): Unit = {}

    override def error(condition: Condition, message: String): Unit = {}

    override def error(condition: Condition, message: String, e: Throwable): Unit = {}

    override def error(condition: Condition, message: String, f: FB => FieldBuilderResult): Unit = {}
  }

  object NoOp {
    def apply[FB](c: CoreLogger, fb: FB): NoOp[FB] = new NoOp[FB] {
      override def name: String = c.getName

      override def core: CoreLogger = c

      override def fieldBuilder: FB = fb

      override def withCondition(scalaCondition: Condition): Logger[FB] = this

      override def withFields(f: FB => FieldBuilderResult): Logger[FB] = this

      override def withThreadContext: Logger[FB] = this

      override def withFieldBuilder[T <: FB](newBuilder: T): Logger[T] = NoOp(core, newBuilder)
    }
  }
}
