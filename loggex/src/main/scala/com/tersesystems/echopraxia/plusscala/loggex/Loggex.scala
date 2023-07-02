package com.tersesystems.echopraxia.plusscala.loggex

import com.tersesystems.echopraxia.api.{Field, FieldBuilderResult, Value, Level => JLevel}
import com.tersesystems.echopraxia.plusscala.api.Condition
import com.tersesystems.echopraxia.plusscala.spi.{DefaultMethodsSupport, LoggerSupport}
import com.tersesystems.echopraxia.spi.{CoreLogger, FieldConstants, Utilities}

import scala.compat.java8.FunctionConverters.enrichAsJavaFunction

class Loggex[FB](val core: CoreLogger, val fieldBuilder: FB) extends LoggerSupport[FB, Loggex] with DefaultMethodsSupport[FB] {

  override def name: String = core.getName

  override def withCondition(condition: Condition): Loggex[FB] = {
    condition match {
      case Condition.always =>
        this
      case Condition.never =>
        newLogger(newCoreLogger = core.withCondition(Condition.never.asJava))
      case other =>
        newLogger(newCoreLogger = core.withCondition(other.asJava))
    }
  }

  override def withFields(f: FB => FieldBuilderResult): Loggex[FB] = {
    newLogger(newCoreLogger = core.withFields(f.asJava, fieldBuilder))
  }

  override def withThreadContext: Loggex[FB] = {
    newLogger(
      newCoreLogger = core.withThreadContext(Utilities.threadContext())
    )
  }

  override def withFieldBuilder[NEWFB](newFieldBuilder: NEWFB): Loggex[NEWFB] = {
    newLogger(newFieldBuilder = newFieldBuilder)
  }

  @inline
  private def newLogger[T](
      newCoreLogger: CoreLogger = core,
      newFieldBuilder: T = fieldBuilder
  ): Loggex[T] =
    new Loggex[T](newCoreLogger, newFieldBuilder)

  private def createMethod(level: JLevel): LogMethod = new LogMethod(level, this)

  val error: LogMethod = createMethod(JLevel.ERROR)
  val warn: LogMethod  = createMethod(JLevel.WARN)
  val info: LogMethod  = createMethod(JLevel.INFO)
  val debug: LogMethod = createMethod(JLevel.DEBUG)
  val trace: LogMethod = createMethod(JLevel.TRACE)

  class LogMethod(val level: JLevel, val support: DefaultMethodsSupport[FB]) {
    def enabled: Boolean = core.isEnabled(level)

    def enabled(condition: Condition): Boolean = core.isEnabled(level, condition.asJava)
  }
}

object Loggex {
  // We can't simply use overloaded arguments on apply, because we'll get
  // "missing argument type" on the fb argument:
  //
  // def apply(message: String, f: FB => FieldBuilderResult): Unit = methods.handleMessageArgs(level, message, f)
  //
  // logger.debug("message", fb => fb.string("herp" -> "derp")) // WILL NOT COMPILE
  //
  // so instead, we have a single apply, and tell the compiler to try out implicit definitions!
  //
  // This is not a problem in Scala 3, where overload resolution is much more powerful, but unfortunately
  // published libraries are recommended not to rely on forward/backwards compatility features :-/

  // XXX make sure you can extend this

  implicit class RichLogMethod[FB](lm: Loggex[FB]#LogMethod) {
    import lm.level
    import lm.support._

    def apply(message: String): Unit = core.log(level, message)

    def apply(message: String, f: FB => FieldBuilderResult): Unit = core.log(level, message, f.asJava, fieldBuilder)

    def apply(message: String, e: Throwable): Unit = core.log(level, message, (_: FB) => onlyException(e), fieldBuilder)

    def apply(condition: Condition, message: String): Unit = core.log(level, condition.asJava, message)

    def apply(condition: Condition, message: String, f: FB => FieldBuilderResult): Unit =
      core.log(level, condition.asJava, message, f.asJava, fieldBuilder)

    def apply(condition: Condition, message: String, e: Throwable): Unit = {
      core.log(level, condition.asJava, message, (_: FB) => onlyException(e), fieldBuilder)
    }

    def onlyException(e: Throwable): FieldBuilderResult = {
      Field.keyValue(FieldConstants.EXCEPTION, Value.exception(e))
    }

  }
}
