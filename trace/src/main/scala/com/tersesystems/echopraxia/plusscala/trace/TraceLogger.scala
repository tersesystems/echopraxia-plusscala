package com.tersesystems.echopraxia.plusscala.trace

import com.tersesystems.echopraxia.api.Level.TRACE
import com.tersesystems.echopraxia.api.{CoreLogger, Field, FieldBuilderResult, Utilities, Value}
import com.tersesystems.echopraxia.plusscala.api._
import sourcecode.{Args, Enclosing, File, FullName, Line, Text}

import java.util.Objects
import scala.compat.java8.FunctionConverters._
import scala.util.{Failure, Success, Try}

trait TracingFieldBuilder extends SourceCodeFieldBuilder with ValueTypeClasses {
  def entering(method: sourcecode.FullName, args: sourcecode.Args): FieldBuilderResult

  def exiting(method: sourcecode.FullName, value: Value[_]): FieldBuilderResult

  def throwing(method: sourcecode.FullName, ex: Throwable): FieldBuilderResult
}

trait DefaultTracingFieldBuilder extends FieldBuilder with TracingFieldBuilder {
  import DefaultTracingFieldBuilder._

  def argumentField(txt: Text[_]): Field = {
    keyValue(txt.source, Value.string(Objects.toString(txt.value)))
  }

  override def entering(method: sourcecode.FullName, args: sourcecode.Args): FieldBuilderResult = {
    val argsValue = ToArrayValue(args.value.map(list => ToArrayValue(list.map(argumentField))))
    value(Trace, ToObjectValue(keyValue(Method, method.value), keyValue("tag", Entry), keyValue(Arguments, argsValue)))
  }

  override def exiting(method: sourcecode.FullName, retValue: Value[_]): FieldBuilderResult = {
    value(Trace, ToObjectValue(value(Method, method.value), string(Trace, Exit), keyValue(Result, retValue)))
  }

  override def throwing(method: sourcecode.FullName, ex: Throwable): FieldBuilderResult = {
    value(Trace, ToObjectValue(value(Method, method.value), string(Trace, Throwing), exception(ex)))
  }
}

object DefaultTracingFieldBuilder {
  val Trace: String     = "trace"
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

  override def withCondition(condition: Condition): TraceLogger[FB] = {
    condition match {
      case Condition.always =>
        this
      case Condition.never =>
        new TraceLogger[FB](core, fieldBuilder) {
          override def trace[B: FB#ToValue](attempt: => B)(implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): B =
            attempt
        }
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

  // XXX Needs some tests
  def trace[B: FB#ToValue](
      attempt: => B
  )(implicit line: Line, file: File, fullname: sourcecode.FullName, enc: Enclosing, args: sourcecode.Args): B = {
    if (!core.isEnabled(TRACE)) {
      attempt
    } else {
      val srcF: FB => FieldBuilderResult = fb => fb.sourceCodeFields(line.value, file.value, enc.value)
      val coreWithFields                 = core.withFields(srcF.asJava, fieldBuilder)

      val entryF: FB => FieldBuilderResult = fb => fb.entering(fullname, args)
      coreWithFields.log(TRACE, "{}", entryF.asJava, fieldBuilder)

      val result = Try(attempt)
      val exitF: FB => FieldBuilderResult = result match {
        case Success(ret) => _.exiting(fullname, implicitly[FB#ToValue[B]].toValue(ret))
        case Failure(ex)  => _.throwing(fullname, ex)
      }
      coreWithFields.log(TRACE, "{}", exitF.asJava, fieldBuilder)
      result.get // rethrow the exception
    }
  }

  @inline
  private def newLogger[T <: TracingFieldBuilder](
      newCoreLogger: CoreLogger = core,
      newFieldBuilder: T = fieldBuilder
  ): TraceLogger[T] =
    new TraceLogger[T](newCoreLogger, newFieldBuilder)
}
