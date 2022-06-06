package com.tersesystems.echopraxia.plusscala.flow

import com.tersesystems.echopraxia.api
import com.tersesystems.echopraxia.api.{CoreLogger, FieldBuilderResult, Utilities}
import com.tersesystems.echopraxia.plusscala.api._
import sourcecode.{Args, Enclosing, File, Line}

import scala.compat.java8.FunctionConverters._

class VerboseTraceLogger[FB <: VerboseTracingFieldBuilder](core: CoreLogger, fieldBuilder: FB)
    extends AbstractLoggerSupport(core, fieldBuilder)
    with DefaultVerboseTraceLoggerMethods[FB]
    with LoggerSupport[FB] {

  override def withCondition(condition: Condition): VerboseTraceLogger[FB] = {
    condition match {
      case Condition.never =>
        neverLogger()
      case other =>
        newLogger(newCoreLogger = core.withCondition(other.asJava))
    }
  }

  override def withFields(f: FB => FieldBuilderResult): VerboseTraceLogger[FB] = {
    newLogger(newCoreLogger = core.withFields(f.asJava, fieldBuilder))
  }

  override def withThreadContext: VerboseTraceLogger[FB] = {
    newLogger(
      newCoreLogger = core.withThreadContext(Utilities.threadContext())
    )
  }

  def withFieldBuilder[NEWFB <: VerboseTracingFieldBuilder](newFieldBuilder: NEWFB): VerboseTraceLogger[NEWFB] = {
    newLogger(newFieldBuilder = newFieldBuilder)
  }

  @inline
  private def neverLogger(): VerboseTraceLogger[FB] = {
    new VerboseTraceLogger.Never[FB](core.withCondition(Condition.never.asJava), fieldBuilder)
  }

  @inline
  private def newLogger[T <: VerboseTracingFieldBuilder](
      newCoreLogger: CoreLogger = core,
      newFieldBuilder: T = fieldBuilder
  ): VerboseTraceLogger[T] =
    new VerboseTraceLogger[T](newCoreLogger, newFieldBuilder)
}

object VerboseTraceLogger {
  final class Never[FB <: VerboseTracingFieldBuilder](core: CoreLogger, fieldBuilder: FB) extends VerboseTraceLogger[FB](core, fieldBuilder) {
    override protected def handle[B: ToValue](level: api.Level, attempt: => B)(implicit line: Line, file: File, enc: Enclosing, args: Args): B =
      attempt

    override protected def handleCondition[B: ToValue](level: api.Level, condition: Condition, attempt: => B)(implicit
        line: Line,
        file: File,
        enc: Enclosing,
        args: Args
    ): B = attempt
  }
}
