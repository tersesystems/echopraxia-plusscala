package com.tersesystems.echopraxia.plusscala.trace

import com.tersesystems.echopraxia.api
import com.tersesystems.echopraxia.api.{CoreLogger, FieldBuilderResult, Utilities}
import com.tersesystems.echopraxia.plusscala.api._
import sourcecode.{Args, Enclosing, File, Line}

import scala.compat.java8.FunctionConverters._

class TraceLoggerWithArgs[FB <: TracingWithArgsFieldBuilder](core: CoreLogger, fieldBuilder: FB)
  extends AbstractLoggerSupport(core, fieldBuilder)
    with DefaultTraceLoggerMethodsWithArgs[FB]
    with LoggerSupport[FB] {

  override def withCondition(condition: Condition): TraceLoggerWithArgs[FB] = {
    condition match {
      case Condition.never =>
        neverLogger()
      case other =>
        newLogger(newCoreLogger = core.withCondition(other.asJava))
    }
  }

  override def withFields(f: FB => FieldBuilderResult): TraceLoggerWithArgs[FB] = {
    newLogger(newCoreLogger = core.withFields(f.asJava, fieldBuilder))
  }

  override def withThreadContext: TraceLoggerWithArgs[FB] = {
    newLogger(
      newCoreLogger = core.withThreadContext(Utilities.threadContext())
    )
  }

  def withFieldBuilder[NEWFB <: TracingWithArgsFieldBuilder](newFieldBuilder: NEWFB): TraceLoggerWithArgs[NEWFB] = {
    newLogger(newFieldBuilder = newFieldBuilder)
  }

  @inline
  private def neverLogger(): TraceLoggerWithArgs[FB] = {
    new TraceLoggerWithArgs.NeverLogger[FB](core.withCondition(Condition.never.asJava), fieldBuilder)
  }

  @inline
  private def newLogger[T <: TracingWithArgsFieldBuilder](
                                                   newCoreLogger: CoreLogger = core,
                                                   newFieldBuilder: T = fieldBuilder
                                                 ): TraceLoggerWithArgs[T] =
    new TraceLoggerWithArgs[T](newCoreLogger, newFieldBuilder)
}

object TraceLoggerWithArgs {
  final class NeverLogger[FB <: TracingWithArgsFieldBuilder](core: CoreLogger, fieldBuilder: FB) extends TraceLoggerWithArgs[FB](core, fieldBuilder) {
    override protected def handle[B: ToValue](level: api.Level, attempt: => B)(implicit line: Line, file: File, enc: Enclosing, args: Args): B = attempt

    override protected def handleCondition[B: ToValue](level: api.Level, condition: Condition, attempt: => B)(implicit line: Line, file: File, enc: Enclosing, args: Args): B = attempt
  }
}
