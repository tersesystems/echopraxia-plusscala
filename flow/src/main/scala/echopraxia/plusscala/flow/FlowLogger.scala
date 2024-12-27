package echopraxia.plusscala.flow

import echopraxia.api.FieldBuilderResult
import echopraxia.logging.spi.CoreLogger
import echopraxia.logging.spi.Utilities
import echopraxia.plusscala.logging.api.Condition
import echopraxia.plusscala.logging.api.DefaultMethodsSupport
import echopraxia.plusscala.logging.api.LoggerSupport

import scala.compat.java8.FunctionConverters._

trait FlowLogger[FB <: FlowFieldBuilder with Singleton]
    extends FlowLoggerMethods[FB]
    with LoggerSupport[FB, FlowLogger]
    with DefaultMethodsSupport[FB]

object FlowLogger {

  def apply[FB <: FlowFieldBuilder with Singleton](c: CoreLogger, fb: FB): FlowLogger[FB] = new Impl(c, fb)

  class Impl[FB <: FlowFieldBuilder with Singleton](val core: CoreLogger, val fieldBuilder: FB)
      extends FlowLogger[FB]
      with DefaultFlowLoggerMethods[FB] {

    override def name: String = core.getName

    override def withCondition(condition: Condition): FlowLogger[FB] = {
      condition match {
        case Condition.never =>
          neverLogger()
        case other =>
          newLogger(newCoreLogger = core.withCondition(other.asJava))
      }
    }

    override def withFields(f: FB => FieldBuilderResult): FlowLogger[FB] = {
      newLogger(newCoreLogger = core.withFields(f.asJava, fieldBuilder))
    }

    override def withThreadContext: FlowLogger[FB] = {
      newLogger(
        newCoreLogger = core.withThreadContext(Utilities.threadContext())
      )
    }

    def withFieldBuilder[NEWFB <: FlowFieldBuilder with Singleton](newFieldBuilder: NEWFB): FlowLogger[NEWFB] = {
      newLogger(newFieldBuilder = newFieldBuilder)
    }

    @inline
    private def neverLogger(): FlowLogger[FB] = {
      NoOp[FB](core.withCondition(Condition.never.asJava), fieldBuilder)
    }

    @inline
    private def newLogger[T <: FlowFieldBuilder with Singleton](
        newCoreLogger: CoreLogger = core,
        newFieldBuilder: T = fieldBuilder
    ): FlowLogger[T] =
      FlowLogger[T](newCoreLogger, newFieldBuilder)
  }

  object NoOp {
    def apply[FB <: FlowFieldBuilder with Singleton](c: CoreLogger, fb: FB): FlowLogger[FB] = new NoOp[FB] {
      override def name: String = c.getName

      override def core: CoreLogger = c

      override def fieldBuilder: FB = this.fb

      override def withCondition(scalaCondition: Condition): FlowLogger[FB] = this

      override def withFields(f: FB => FieldBuilderResult): FlowLogger[FB] = this

      override def withThreadContext: FlowLogger[FB] = this

      override def withFieldBuilder[T <: FB](newBuilder: T): FlowLogger[T] = NoOp(c, newBuilder)
    }
  }

  trait NoOp[FB <: FlowFieldBuilder with Singleton] extends FlowLogger[FB] {
    override def trace[B: ToValue](attempt: => B): B                       = attempt
    override def trace[B: ToValue](condition: Condition)(attempt: => B): B = attempt
    override def debug[B: ToValue](attempt: => B): B                       = attempt
    override def debug[B: ToValue](condition: Condition)(attempt: => B): B = attempt
    override def info[B: ToValue](attempt: => B): B                        = attempt
    override def info[B: ToValue](condition: Condition)(attempt: => B): B  = attempt
    override def warn[B: ToValue](attempt: => B): B                        = attempt
    override def warn[B: ToValue](condition: Condition)(attempt: => B): B  = attempt
    override def error[B: ToValue](attempt: => B): B                       = attempt
    override def error[B: ToValue](condition: Condition)(attempt: => B): B = attempt
  }
}
