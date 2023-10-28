package com.tersesystems.echopraxia.logger2

import com.tersesystems.echopraxia.api.{FieldBuilderResult, Level}
import com.tersesystems.echopraxia.api.Level._
import com.tersesystems.echopraxia.plusscala.api.{Condition, Implicits}
import com.tersesystems.echopraxia.plusscala.spi.{DefaultMethodsSupport, LoggerSupport}
import com.tersesystems.echopraxia.spi.{CoreLogger, Utilities}

import scala.compat.java8.FunctionConverters.enrichAsJavaFunction

trait Logger[FB] extends LoggerSupport[FB, Logger] with DefaultMethodsSupport[FB] {

  def trace: LoggerMethod[FB]
  def debug: LoggerMethod[FB]
  def info: LoggerMethod[FB]
  def warn: LoggerMethod[FB]
  def error: LoggerMethod[FB]
}

object Logger {

  def apply[FB](core: CoreLogger, fieldBuilder: FB): Logger[FB] = new Impl[FB](core, fieldBuilder)

  /**
   */
  class Impl[FB](val core: CoreLogger, val fieldBuilder: FB) extends Logger[FB] {
    class DefaultLoggerMethod(level: Level) extends LoggerMethod[FB] {
      import Implicits._

      override def enabled: Boolean = core.isEnabled(level)

      override def enabled(condition: Condition): Boolean = core.isEnabled(level, condition.asJava)

      override def apply(message: String): Unit = core.log(level, message)

      override def apply(message: String, f1: FB => FieldBuilderResult): Unit = core.log(level, message, f1.asJava, fieldBuilder)
      override def apply(message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult): Unit = {
        val f: FB => FieldBuilderResult = fb => f1(fb) ++ f2(fb)
        core.log(level, message, f.asJava, fieldBuilder)
      }

      override def apply(message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult, f3: FB => FieldBuilderResult): Unit = {
        val f: FB => FieldBuilderResult = fb => f1(fb) ++ f2(fb) ++ f3(fb)
        core.log(level, message, f.asJava, fieldBuilder)
      }

      override def apply(message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult, f3: FB => FieldBuilderResult, f4: FB => FieldBuilderResult): Unit = {
        val f: FB => FieldBuilderResult = fb => f1(fb) ++ f2(fb) ++ f3(fb) ++ f4(fb)
        core.log(level, message, f.asJava, fieldBuilder)
      }

      override def apply(condition: Condition, message: String): Unit = core.log(level, condition.asJava, message)

      override def apply(condition: Condition, message: String, f1: FB => FieldBuilderResult): Unit = core.log(level, condition.asJava, message, f1.asJava, fieldBuilder)

    }

    override val trace: DefaultLoggerMethod = new DefaultLoggerMethod(TRACE)

    override val debug: DefaultLoggerMethod = new DefaultLoggerMethod(DEBUG)

    override val info: DefaultLoggerMethod = new DefaultLoggerMethod(INFO)

    override val warn: DefaultLoggerMethod = new DefaultLoggerMethod(WARN)

    override val error: DefaultLoggerMethod = new DefaultLoggerMethod(ERROR)

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
                            ): Logger[T] = new Impl[T](newCoreLogger, newFieldBuilder)

  }

  trait NoOp[FB] extends Logger[FB] {
    object NoOpMethod extends LoggerMethod[FB] {
      override def enabled: Boolean = false

      override def enabled(condition: Condition): Boolean = false

      override def apply(message: String): Unit = ()

      override def apply(message: String, f: FB => FieldBuilderResult): Unit = ()

      override def apply(condition: Condition, message: String): Unit = ()

      override def apply(condition: Condition, message: String, f: FB => FieldBuilderResult): Unit = ()

      override def apply(message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult): Unit = ()

      override def apply(message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult, f3: FB => FieldBuilderResult): Unit = ()

      override def apply(message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult, f3: FB => FieldBuilderResult, f4: FB => FieldBuilderResult): Unit = ()
    }
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

      override def trace: LoggerMethod[FB] = NoOpMethod

      override def debug: LoggerMethod[FB] = NoOpMethod

      override def info: LoggerMethod[FB] = NoOpMethod

      override def warn: LoggerMethod[FB] = NoOpMethod

      override def error: LoggerMethod[FB] = NoOpMethod
    }
  }
}
