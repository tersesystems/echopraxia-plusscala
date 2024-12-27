package echopraxia.plusscala.trace

import echopraxia.api.FieldBuilderResult
import echopraxia.plusscala.api._
import echopraxia.logging.spi.{CoreLogger, Utilities}
import echopraxia.plusscala.logging.api.{Condition, DefaultMethodsSupport, LoggerSupport}
import sourcecode.Args
import sourcecode.Enclosing
import sourcecode.File
import sourcecode.Line

import scala.compat.java8.FunctionConverters._

trait TraceLogger[FB <: TraceFieldBuilder] extends LoggerSupport[FB, TraceLogger] with TraceLoggerMethods[FB] with DefaultMethodsSupport[FB]

object TraceLogger {

  def apply[FB <: TraceFieldBuilder](core: CoreLogger, fieldBuilder: FB): TraceLogger[FB] = new Impl(core, fieldBuilder)

  class Impl[FB <: TraceFieldBuilder](val core: CoreLogger, val fieldBuilder: FB) extends TraceLogger[FB] with DefaultTraceLoggerMethods[FB] {

    override def name: String = core.getName

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

    override def withFieldBuilder[NEWFB <: TraceFieldBuilder](newFieldBuilder: NEWFB): TraceLogger[NEWFB] = {
      newLogger(newFieldBuilder = newFieldBuilder)
    }

    @inline
    private def neverLogger(): TraceLogger[FB] = {
      TraceLogger.NoOp[FB](core.withCondition(Condition.never.asJava), fieldBuilder)
    }

    @inline
    private def newLogger[T <: TraceFieldBuilder](
        newCoreLogger: CoreLogger = core,
        newFieldBuilder: T = fieldBuilder
    ): TraceLogger[T] = TraceLogger[T](newCoreLogger, newFieldBuilder)
  }

  trait NoOp[FB <: TraceFieldBuilder] extends TraceLogger[FB] {
    override def trace[B: ToValue](attempt: => B)(implicit line: Line, file: File, enc: Enclosing, args: Args): B = attempt

    override def trace[B: ToValue](condition: Condition)(attempt: => B)(implicit line: Line, file: File, enc: Enclosing, args: Args): B = attempt

    override def debug[B: ToValue](attempt: => B)(implicit line: Line, file: File, enc: Enclosing, args: Args): B = attempt

    override def debug[B: ToValue](condition: Condition)(attempt: => B)(implicit line: Line, file: File, enc: Enclosing, args: Args): B = attempt

    override def info[B: ToValue](attempt: => B)(implicit line: Line, file: File, enc: Enclosing, args: Args): B = attempt

    override def info[B: ToValue](condition: Condition)(attempt: => B)(implicit line: Line, file: File, enc: Enclosing, args: Args): B = attempt

    override def warn[B: ToValue](attempt: => B)(implicit line: Line, file: File, enc: Enclosing, args: Args): B = attempt

    override def warn[B: ToValue](condition: Condition)(attempt: => B)(implicit line: Line, file: File, enc: Enclosing, args: Args): B = attempt

    override def error[B: ToValue](attempt: => B)(implicit line: Line, file: File, enc: Enclosing, args: Args): B = attempt

    override def error[B: ToValue](condition: Condition)(attempt: => B)(implicit line: Line, file: File, enc: Enclosing, args: Args): B = attempt
  }

  object NoOp {
    def apply[FB <: TraceFieldBuilder](c: CoreLogger, fb: FB): TraceLogger[FB] = new NoOp[FB] {
      override def name: String = c.getName

      override def core: CoreLogger = c

      override def fieldBuilder: FB = this.fb

      override def withCondition(scalaCondition: Condition): TraceLogger[FB] = this

      override def withFields(f: FB => FieldBuilderResult): TraceLogger[FB] = this

      override def withThreadContext: TraceLogger[FB] = this

      override def withFieldBuilder[T <: FB](newBuilder: T): TraceLogger[T] = NoOp(c, newBuilder)
    }

  }
}
