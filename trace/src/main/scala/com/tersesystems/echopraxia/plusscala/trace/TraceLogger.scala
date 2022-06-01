package com.tersesystems.echopraxia.plusscala.trace

import com.tersesystems.echopraxia.api.{Level => JLevel}
import com.tersesystems.echopraxia.api.{CoreLogger, Field, FieldBuilderResult, Utilities, Value}
import com.tersesystems.echopraxia.plusscala.api._
import sourcecode.{Args, Enclosing, File, FullName, Line, Text}

import java.util.Objects
import java.util.function.Function
import scala.compat.java8.FunctionConverters._
import scala.util.{Failure, Success, Try}

trait TracingFieldBuilder extends SourceCodeFieldBuilder with ValueTypeClasses {

  def enteringTemplate: String

  def exitingTemplate: String

  def throwingTemplate: String

  def entering(implicit line: Line, file: File, fullName: FullName, enc: Enclosing, args: Args): FieldBuilderResult

  def exiting(value: Value[_])(implicit line: Line, file: File, fullName: FullName, enc: Enclosing, args: Args): FieldBuilderResult

  def throwing(ex: Throwable)(implicit line: Line, file: File, fullName: FullName, enc: Enclosing, args: Args): FieldBuilderResult
}

trait DefaultTracingFieldBuilder extends FieldBuilder with TracingFieldBuilder {
  import DefaultTracingFieldBuilder._

  val enteringTemplate: String = "{}: "

  val exitingTemplate: String = "{}"

  val throwingTemplate: String = "{}"

  def argumentField(txt: Text[_]): Field = {
    keyValue(txt.source, Value.string(Objects.toString(txt.value)))
  }

  override def entering(implicit line: Line, file: File, fullName: FullName, enc: Enclosing, args: Args): FieldBuilderResult = {
    val argsValue = ToArrayValue(args.value.map(list => ToArrayValue(list.map(argumentField))))
    value(Tag, ToObjectValue(keyValue(Method, fullName.value), keyValue(Tag, Entry), keyValue(Arguments, argsValue)))
  }

  override def exiting(retValue: Value[_])(implicit line: Line, file: File, fullName: FullName, enc: Enclosing, args: Args): FieldBuilderResult = {
    value(Tag, ToObjectValue(keyValue(Method, fullName.value), keyValue(Tag, Exit), keyValue(Result, retValue)))
  }

  override def throwing(ex: Throwable)(implicit line: Line, file: File, fullName: FullName, enc: Enclosing, args: Args): FieldBuilderResult = {
    value(Tag, ToObjectValue(keyValue(Method, fullName.value), keyValue(Tag, Throwing), exception(ex)))
  }
}

object DefaultTracingFieldBuilder extends DefaultTracingFieldBuilder {
  val Tag: String     = "tag"
  val Entry: String     = "entry"
  val Exit: String      = "exit"
  val Throwing: String  = "throwing"
  val Arguments: String = "arguments"
  val Result: String    = "result"
  val Method            = "method"
}

class TraceLogger[FB <: TracingFieldBuilder](core: CoreLogger, fieldBuilder: FB)
    extends AbstractLoggerSupport(core, fieldBuilder)
    with LoggerSupport[FB] {

  import fieldBuilder._

  override def withCondition(condition: Condition): TraceLogger[FB] = {
    condition match {
      case Condition.always =>
        this
      case Condition.never =>
        // XXX optimize this
        new TraceLogger[FB](core.withCondition(Condition.never.asJava), fieldBuilder)
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

  def trace[B: ToValue](attempt: => B)
                          (implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): B = {
    handle(JLevel.TRACE, attempt)
  }

  def trace[B: ToValue](condition: Condition)
                          (attempt: => B)
                          (implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): B = {
    handleCondition(JLevel.TRACE, condition, attempt)
  }

  def debug[B: ToValue](attempt: => B)
                          (implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): B = {
    handle(JLevel.DEBUG, attempt)
  }


  def debug[B: ToValue](condition: Condition)
                          (attempt: => B)
                          (implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): B = {
    handleCondition(JLevel.DEBUG, condition, attempt)
  }

  def info[B: ToValue](attempt: => B)
                         (implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): B = {
    handle(JLevel.INFO, attempt)
  }

  def info[B: ToValue](condition: Condition)
                         (attempt: => B)
                         (implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): B = {
    handleCondition(JLevel.INFO, condition, attempt)
  }

  def warn[B: ToValue](attempt: => B)
                         (implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): B = {
    handle(JLevel.WARN, attempt)
  }

  def warn[B: ToValue](condition: Condition)
                         (attempt: => B)
                         (implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): B = {
    handleCondition(JLevel.WARN, condition, attempt)
  }


  def error[B: ToValue](attempt: => B)
                          (implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): B = {
    handle(JLevel.ERROR, attempt)
  }

  def error[B: ToValue](condition: Condition)(
                            attempt: => B
                          )(implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): B = {
    handleCondition(JLevel.ERROR, condition, attempt)
  }

  @inline
  private def sourceInfoFields(line: Line, file: File, enc: Enclosing): Function[FB, FieldBuilderResult] = { fb: FB =>
    fb.sourceCodeFields(line.value, file.value, enc.value)
  }.asJava

  @inline
  private def entering(implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): Function[FB, FieldBuilderResult] = { fb: FB =>
    fb.entering
  }.asJava

  @inline
  private def exiting[B: ToValue](ret: B)(implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): Function[FB, FieldBuilderResult] = { fb: FB =>
    fb.exiting(ToValue(ret))
  }.asJava

  @inline
  private def throwing(ex: Throwable)(implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): Function[FB, FieldBuilderResult] = { fb: FB =>
    fb.throwing(ex)
  }.asJava

  @inline
  private def handle[B: ToValue](
      level: JLevel,
      attempt: => B
  )(implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): B = {
    if (!core.isEnabled(level)) {
      attempt
    } else {
      execute(level, attempt)
    }
  }

  @inline
  private def handleCondition[B: ToValue](
    level: JLevel,
    condition: Condition,
    attempt: => B
  )(implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): B = {
    if (!core.isEnabled(level, condition.asJava)) {
      attempt
    } else {
      execute(level, attempt)
    }
  }

  @inline
  private def execute[B: ToValue](level: JLevel,
                      attempt: => B)(implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args) = {
    val coreWithFields = core.withFields(sourceInfoFields(line, file, enc), fieldBuilder)
    coreWithFields.log(level, fieldBuilder.enteringTemplate, entering, fieldBuilder)

    val result = Try(attempt)
    result match {
      case Success(ret) =>
        coreWithFields.log(level, fieldBuilder.exitingTemplate, exiting(ret), fieldBuilder)
      case Failure(ex)  =>
        coreWithFields.log(level, fieldBuilder.throwingTemplate, throwing(ex), fieldBuilder)
    }
    result.get // rethrow the exception
  }

  @inline
  private def newLogger[T <: TracingFieldBuilder](
      newCoreLogger: CoreLogger = core,
      newFieldBuilder: T = fieldBuilder
  ): TraceLogger[T] =
    new TraceLogger[T](newCoreLogger, newFieldBuilder)
}
