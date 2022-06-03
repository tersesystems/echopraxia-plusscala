package com.tersesystems.echopraxia.plusscala.trace

import com.tersesystems.echopraxia.api.{CoreLogger, FieldBuilderResult, Utilities}
import com.tersesystems.echopraxia.plusscala.api._
import sourcecode.{Args, Enclosing, File, FullName, Line}

import scala.compat.java8.FunctionConverters._

class TraceLogger[FB <: TracingFieldBuilder](core: CoreLogger, fieldBuilder: FB)
    extends AbstractLoggerSupport(core, fieldBuilder)
    with DefaultTraceLoggerMethods[FB]
    with LoggerSupport[FB] {

  override def withCondition(condition: Condition): TraceLogger[FB] = {
    condition match {
      case Condition.never =>
        neverLogger()
      case other =>
        newLogger(newCoreLogger = core.withCondition(other.asJava))
    }
  }

  override def withFields(f: FB => FieldBuilderResult): TraceLogger[FB] = {
    newLogger(newCoreLogger = core.withFields(f.asJava, fieldBuilder))
  }

  override def withThreadContext: TraceLogger[FB] = {
    newLogger(
      newCoreLogger = core.withThreadContext(Utilities.threadContext())
    )
  }

  def withFieldBuilder[NEWFB <: TracingFieldBuilder](newFieldBuilder: NEWFB): TraceLogger[NEWFB] = {
    newLogger(newFieldBuilder = newFieldBuilder)
  }

  @inline
  private def neverLogger(): TraceLogger[FB] = {
    new TraceLogger.NeverLogger[FB](core.withCondition(Condition.never.asJava), fieldBuilder)
  }

  @inline
  private def newLogger[T <: TracingFieldBuilder](
      newCoreLogger: CoreLogger = core,
      newFieldBuilder: T = fieldBuilder
  ): TraceLogger[T] =
    new TraceLogger[T](newCoreLogger, newFieldBuilder)
}

object TraceLogger {

  final class NeverLogger[FB <: TracingFieldBuilder](core: CoreLogger, fieldBuilder: FB) extends TraceLogger[FB](core, fieldBuilder) {

    override def trace[B: ToValue](
        attempt: => B
    )(implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): B = attempt

    override def trace[B: ToValue](condition: Condition)(
        attempt: => B
    )(implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): B = attempt

    override def debug[B: ToValue](
        attempt: => B
    )(implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): B = attempt

    override def debug[B: ToValue](condition: Condition)(
        attempt: => B
    )(implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): B = attempt

    override def info[B: ToValue](
        attempt: => B
    )(implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): B = attempt

    override def info[B: ToValue](condition: Condition)(
        attempt: => B
    )(implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): B = attempt

    override def warn[B: ToValue](
        attempt: => B
    )(implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): B = attempt

    override def warn[B: ToValue](condition: Condition)(
        attempt: => B
    )(implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): B = attempt

    override def error[B: ToValue](
        attempt: => B
    )(implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): B = attempt

    override def error[B: ToValue](condition: Condition)(
        attempt: => B
    )(implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): B = attempt
  }
}
