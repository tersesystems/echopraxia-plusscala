package com.tersesystems.echopraxia.plusscala.trace

import com.tersesystems.echopraxia.api.{CoreLogger, Field, FieldBuilderResult, Level => JLevel}
import com.tersesystems.echopraxia.plusscala.api.{Condition, DefaultMethodsSupport}
import sourcecode._

import java.util.function.{Function, Supplier}
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
  private def sourceLoggerFields(signature: fb.SourceFields): Supplier[java.util.List[Field]] = {
    import scala.jdk.CollectionConverters._
    () => signature.loggerFields.asJava
  }

  @inline
  private def entering(signature: fb.SourceFields): Function[FB, FieldBuilderResult] = { fb: FB =>
    fb.entering(signature)
  }.asJava

  @inline
  private def exiting[B: ToValue](signature: fb.SourceFields, ret: B): Function[FB, FieldBuilderResult] = { fb: FB =>
    fb.exiting(signature, implicitly[ToValue[B]].toValue(ret))
  }.asJava

  @inline
  private def throwing(signature: fb.SourceFields, ex: Throwable): Function[FB, FieldBuilderResult] = { fb: FB =>
    fb.throwing(signature, ex)
  }.asJava

  @inline
  private def handle[B: ToValue](
      level: JLevel,
      attempt: => B
  )(implicit line: Line, file: File, enc: Enclosing, args: Args): B = {
    if (core.isEnabled(level)) {
      execute(core, level, attempt)
    } else {
      attempt
    }
  }

  @inline
  private def handleCondition[B: ToValue](
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
    val sourceFields = fb.sourceFields
    val extraFields = sourceLoggerFields(sourceFields)
    core.log(level, extraFields, fb.enteringTemplate, entering(sourceFields), fb)
    val result = Try(attempt)
    result match {
      case Success(ret) =>
        core.log(level, extraFields, fb.exitingTemplate, exiting(sourceFields, ret), fb)
      case Failure(ex) =>
        core.log(level, extraFields, fb.throwingTemplate, throwing(sourceFields, ex), fb)
    }
    result.get // rethrow the exception
  }

}
