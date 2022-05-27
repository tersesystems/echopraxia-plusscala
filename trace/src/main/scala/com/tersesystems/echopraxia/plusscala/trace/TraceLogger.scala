package com.tersesystems.echopraxia.plusscala.trace

import com.tersesystems.echopraxia.api.Level.TRACE
import com.tersesystems.echopraxia.api.{CoreLogger, FieldBuilderResult, Utilities, Value}
import com.tersesystems.echopraxia.plusscala.api._
import sourcecode.{Args, Enclosing, File, Line}

import java.util.Objects
import scala.compat.java8.FunctionConverters._
import scala.util.{Failure, Success, Try}

trait TracingFieldBuilder extends SourceCodeFieldBuilder with ValueTypeClasses {
  def entering(args: sourcecode.Args): FieldBuilderResult

  def exiting(value: Value[_]): FieldBuilderResult

  def throwing(ex: Throwable): FieldBuilderResult
}

trait DefaultTracingFieldBuilder extends FieldBuilder with TracingFieldBuilder {
  import DefaultTracingFieldBuilder._

  override def entering(args: sourcecode.Args): FieldBuilderResult = {
    val argsValue: Value.ArrayValue = ToArrayValue(args.value.map { list =>
      ToArrayValue(list.map { txt =>
        keyValue(txt.source, Objects.toString(txt.value))
      })
    })
    list(string(Trace, Entry), keyValue(DefaultTracingFieldBuilder.Args, argsValue))
  }

  override def exiting(value: Value[_]): FieldBuilderResult = {
    list(string(Trace, Exit), keyValue(Result, value))
  }

  override def throwing(ex: Throwable): FieldBuilderResult = {
    list(string(Trace, Throwing), exception(ex))
  }
}

object DefaultTracingFieldBuilder {
  val Trace: String = "trace"
  val Entry: String = "entry"
  val Exit: String = "exit"
  val Throwing: String = "throwing"
  val Args: String = "args"
  val Result: String = "result"
}


class TraceLogger[FB <: TracingFieldBuilder](core: CoreLogger, fieldBuilder: FB)
  extends AbstractLoggerSupport(core, fieldBuilder) with LoggerSupport[FB] {

  @inline
  override def withCondition(condition: Condition): TraceLogger[FB] = {
    condition match {
      case Condition.always =>
        this
      case Condition.never =>
        new TraceLogger[FB](core, fieldBuilder){
          override def trace[B: FB#ToValue](attempt: => B)(implicit line: Line, file: File, enc: Enclosing, args: Args): B = attempt
        }
      case other =>
        newLogger(newCoreLogger = core.withCondition(other.asJava))
    }
  }

  @inline
  override def withFields(f: FB => FieldBuilderResult): TraceLogger[FB] = {
    newLogger(newCoreLogger = core.withFields(f.asJava, fieldBuilder))
  }

  @inline
  override def withThreadContext: TraceLogger[FB] = {
    newLogger(
      newCoreLogger = core.withThreadContext(Utilities.threadContext())
    )
  }

  @inline
  def withFieldBuilder[NEWFB <: TracingFieldBuilder](newFieldBuilder: NEWFB): TraceLogger[NEWFB] = {
    newLogger(newFieldBuilder = newFieldBuilder)
  }

  // XXX Needs some tests
  def trace[B: FB#ToValue](attempt: => B)(implicit line: Line, file: File, enc: Enclosing, args: sourcecode.Args): B = {
    if (! core.isEnabled(TRACE)) {
      attempt
    } else {
      val srcF: FB => FieldBuilderResult = fb => fb.sourceCodeFields(line.value, file.value, enc.value)
      val coreWithFields = core.withFields(srcF.asJava, fieldBuilder)

      val entryF: FB => FieldBuilderResult = fb => fb.entering(args)
      coreWithFields.log(TRACE, "{}: {}", entryF.asJava, fieldBuilder)

      val result = Try(attempt)
      val exitF: FB => FieldBuilderResult = result match {
        case Success(ret) => _.exiting(implicitly[FB#ToValue[B]].toValue(ret))
        case Failure(ex) => _.throwing(ex)
      }
      coreWithFields.log(TRACE, "{}: {}", exitF.asJava, fieldBuilder)
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

