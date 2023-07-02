package com.tersesystems.echopraxia.plusscala.flow

import com.tersesystems.echopraxia.api.FieldBuilderResult
import com.tersesystems.echopraxia.plusscala.api._
import com.tersesystems.echopraxia.plusscala.spi.{DefaultMethodsSupport, LoggerSupport}
import com.tersesystems.echopraxia.spi.CoreLogger
import com.tersesystems.echopraxia.spi.Utilities

import scala.compat.java8.FunctionConverters._

trait FlowLogger[FB <: FlowFieldBuilder] extends FlowLoggerMethods[FB] with LoggerSupport[FB, FlowLogger] with DefaultMethodsSupport[FB]

object FlowLogger {

  def apply[FB <: FlowFieldBuilder](c: CoreLogger, fb: FB): FlowLogger[FB] = new Impl(c, fb)

  class Impl[FB <: FlowFieldBuilder](val core: CoreLogger, val fieldBuilder: FB) extends FlowLogger[FB] with DefaultFlowLoggerMethods[FB] {

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

    def withFieldBuilder[NEWFB <: FlowFieldBuilder](newFieldBuilder: NEWFB): FlowLogger[NEWFB] = {
      newLogger(newFieldBuilder = newFieldBuilder)
    }

    @inline
    private def neverLogger(): FlowLogger[FB] = {
      NoOp[FB](core.withCondition(Condition.never.asJava), fieldBuilder)
    }

    @inline
    private def newLogger[T <: FlowFieldBuilder](
        newCoreLogger: CoreLogger = core,
        newFieldBuilder: T = fieldBuilder
    ): FlowLogger[T] =
      FlowLogger[T](newCoreLogger, newFieldBuilder)
  }

  object NoOp {
    def apply[FB <: FlowFieldBuilder](c: CoreLogger, fb: FB): FlowLogger[FB] = new NoOp[FB] {
      override def name: String = c.getName

      override def core: CoreLogger = c

      override val fieldBuilder: FB = fb

      override def withCondition(scalaCondition: Condition): FlowLogger[FB] = this

      override def withFields(f: FB => FieldBuilderResult): FlowLogger[FB] = this

      override def withThreadContext: FlowLogger[FB] = this

      override def withFieldBuilder[T <: FB](newBuilder: T): FlowLogger[T] = NoOp(c, newBuilder)
    }
  }

  trait NoOp[FB <: FlowFieldBuilder] extends FlowLogger[FB] {
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
