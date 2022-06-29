package com.tersesystems.echopraxia.plusscala.trace

import com.tersesystems.echopraxia.api.{CoreLogger, FieldBuilderResult, Level => JLevel}
import com.tersesystems.echopraxia.plusscala.api.{Condition, DefaultMethodsSupport}
import sourcecode._

import java.util.function.Function
import scala.compat.java8.FunctionConverters._
import scala.util.{Failure, Success, Try}

trait DefaultTraceLoggerMethods[FB <: TraceFieldBuilder] extends DefaultMethodsSupport[FB] with TraceLoggerMethods[FB] {

  def trace[B: ToValue](attempt: => B)(implicit line: Line, file: File, enc: Enclosing, args: Args): B = {
    handle(JLevel.TRACE, attempt)
  }

  def trace[B: ToValue](condition: Condition)(attempt: => B)(implicit line: Line, file: File, enc: Enclosing, args: Args): B = {
    handleCondition(JLevel.TRACE, condition, attempt)
  }

  def debug[B: ToValue](attempt: => B)(implicit line: Line, file: File, enc: Enclosing, args: Args): B = {
    handle(JLevel.DEBUG, attempt)
  }

  def debug[B: ToValue](condition: Condition)(attempt: => B)(implicit line: Line, file: File, enc: Enclosing, args: Args): B = {
    handleCondition(JLevel.DEBUG, condition, attempt)
  }

  def info[B: ToValue](attempt: => B)(implicit line: Line, file: File, enc: Enclosing, args: Args): B = {
    handle(JLevel.INFO, attempt)
  }

  def info[B: ToValue](condition: Condition)(attempt: => B)(implicit line: Line, file: File, enc: Enclosing, args: Args): B = {
    handleCondition(JLevel.INFO, condition, attempt)
  }

  def warn[B: ToValue](attempt: => B)(implicit line: Line, file: File, enc: Enclosing, args: Args): B = {
    handle(JLevel.WARN, attempt)
  }

  def warn[B: ToValue](condition: Condition)(attempt: => B)(implicit line: Line, file: File, enc: Enclosing, args: Args): B = {
    handleCondition(JLevel.WARN, condition, attempt)
  }

  def error[B: ToValue](attempt: => B)(implicit line: Line, file: File, enc: Enclosing, args: Args): B = {
    handle(JLevel.ERROR, attempt)
  }

  def error[B: ToValue](condition: Condition)(
      attempt: => B
  )(implicit line: Line, file: File, enc: Enclosing, args: Args): B = {
    handleCondition(JLevel.ERROR, condition, attempt)
  }

  @inline
  private def sourceInfoFields(fb: FB)(implicit line: Line, file: File, enc: Enclosing): FieldBuilderResult = {
    fb.sourceCodeFields(line.value, file.value, enc.value)
  }

  @inline
  private def entering(implicit line: Line, file: File, enc: Enclosing, args: Args): Function[FB, FieldBuilderResult] = { fb: FB =>
    fb.entering
  }.asJava

  @inline
  private def exiting[B: ToValue](
      ret: B
  )(implicit line: Line, file: File, enc: Enclosing, args: Args): Function[FB, FieldBuilderResult] = { fb: FB =>
    fb.exiting(implicitly[ToValue[B]].toValue(ret))
  }.asJava

  @inline
  private def throwing(
      ex: Throwable
  )(implicit line: Line, file: File, enc: Enclosing, args: Args): Function[FB, FieldBuilderResult] = { fb: FB =>
    fb.throwing(ex)
  }.asJava

  protected def handle[B: ToValue](
      level: JLevel,
      attempt: => B
  )(implicit line: Line, file: File, enc: Enclosing, args: Args): B = {
    if (core.isEnabled(level)) {
      execute(core, level, attempt)
    } else {
      attempt
    }
  }

  protected def handleCondition[B: ToValue](
      level: JLevel,
      condition: Condition,
      attempt: => B
  )(implicit line: Line, file: File, enc: Enclosing, args: Args): B = {
    if (core.isEnabled(level, condition.asJava)) {
      execute(core, level, attempt)
    } else {
      attempt
    }
  }

  @inline
  private def execute[B: ToValue](core: CoreLogger, level: JLevel, attempt: => B)(implicit line: Line, file: File, enc: Enclosing, args: Args): B = {
    val extraFields = sourceInfoFields(fieldBuilder).fields()
    core.log(level, () => extraFields, fieldBuilder.enteringTemplate, entering, fieldBuilder)
    val result = Try(attempt)
    result match {
      case Success(ret) =>
        core.log(level, () => extraFields, fieldBuilder.exitingTemplate, exiting(ret), fieldBuilder)
      case Failure(ex) =>
        core.log(level, () => extraFields, fieldBuilder.throwingTemplate, throwing(ex), fieldBuilder)
    }
    result.get // rethrow the exception
  }

}
