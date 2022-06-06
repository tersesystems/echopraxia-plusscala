package com.tersesystems.echopraxia.plusscala.flow

import com.tersesystems.echopraxia.api
import com.tersesystems.echopraxia.api.{CoreLogger, FieldBuilderResult, Utilities}
import com.tersesystems.echopraxia.plusscala.api._

import scala.compat.java8.FunctionConverters._

class FlowLogger[FB <: FlowFieldBuilder](core: CoreLogger, fieldBuilder: FB)
    extends AbstractLoggerSupport(core, fieldBuilder)
    with DefaultFlowLoggerMethods[FB]
    with LoggerSupport[FB] {

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
    new FlowLogger.NeverLogger[FB](core.withCondition(Condition.never.asJava), fieldBuilder)
  }

  @inline
  private def newLogger[T <: FlowFieldBuilder](
      newCoreLogger: CoreLogger = core,
      newFieldBuilder: T = fieldBuilder
  ): FlowLogger[T] =
    new FlowLogger[T](newCoreLogger, newFieldBuilder)
}

object FlowLogger {
  final class NeverLogger[FB <: FlowFieldBuilder](core: CoreLogger, fieldBuilder: FB) extends FlowLogger[FB](core, fieldBuilder) {
    override protected def handle[B: ToValue](level: api.Level, attempt: => B): B                                = attempt
    override protected def handleCondition[B: ToValue](level: api.Level, condition: Condition, attempt: => B): B = attempt
  }
}
