package com.tersesystems.echopraxia.plusscala.trace

import com.tersesystems.echopraxia.api.{CoreLogger, FieldBuilderResult, Utilities}
import com.tersesystems.echopraxia.api.Level.TRACE
import com.tersesystems.echopraxia.plusscala.api._
import sourcecode.{Enclosing, File, Line}

import java.util.Objects
import scala.compat.java8.FunctionConverters._
import scala.util.{Failure, Success, Try}

trait ArgumentFieldBuilder {
  def argumentFields(args: sourcecode.Args): FieldBuilderResult
}

trait DefaultArgumentFieldBuilder extends ArgumentFieldBuilder { self: FieldBuilder =>
  override def argumentFields(args: sourcecode.Args): FieldBuilderResult = {
    val argsValue = ToArrayValue(args.value.map { list =>
      ToArrayValue(list.map { txt =>
        keyValue(txt.source, Objects.toString(txt.value))
      })
    })
    array("args", argsValue)
  }
}

class TraceLogger[FB <: FieldBuilder with ArgumentFieldBuilder](core: CoreLogger, fieldBuilder: FB)
  extends AbstractLoggerSupport(core, fieldBuilder) with LoggerSupport[FB] {

  @inline
  override def withCondition(condition: Condition): TraceLogger[FB] = {
    condition match {
      case Condition.always =>
        this
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
  def withFieldBuilder[NEWFB <: FieldBuilder with ArgumentFieldBuilder](newFieldBuilder: NEWFB): TraceLogger[NEWFB] = {
    newLogger(newFieldBuilder = newFieldBuilder)
  }

  // XXX needs better control over the fields used to indicate entry state.
  // XXX Needs some tests
  // XXX Needs to handle conditions?  Shortcut on never condition?
  // XXX trace entry/exit/throwing needs to be handled through field builder
  def trace[B: FB#ToValue](attempt: => B)(implicit line: Line, file: File, enc: Enclosing, args: sourcecode.Args): B = {
    val srcF: FB => FieldBuilderResult = fb => fb.sourceCodeFields(line.value, file.value, enc.value)
    val coreWithFields = core.withFields(srcF.asJava, fieldBuilder)
    val entryF: FB => FieldBuilderResult = fb => fb.string("trace", "entry") ++ fb.argumentFields(args)
    coreWithFields.log(TRACE, "{}: {}", entryF.asJava, fieldBuilder)
    val result = Try(attempt)
    result match {
      case Success(ret) =>
        val f: FB => FieldBuilderResult = fb => {
          val retValue = implicitly[FB#ToValue[B]].toValue(ret)
          fb.string("trace", "exit") ++ fb.keyValue("result", retValue)
        }
        coreWithFields.log(TRACE, "{}: {}", f.asJava, fieldBuilder)
      case Failure(ex) =>
        val f: FB => FieldBuilderResult = fb => fb.string("trace", "throwing") ++ fb.exception(ex)
        core.log(TRACE, "{}: {}", f.asJava, fieldBuilder)
    }
    result.get // rethrow the exception
  }

  @inline
  private def newLogger[T <: FieldBuilder with ArgumentFieldBuilder](
                                                      newCoreLogger: CoreLogger = core,
                                                      newFieldBuilder: T = fieldBuilder
                                                    ): TraceLogger[T] =
    new TraceLogger[T](newCoreLogger, newFieldBuilder)

}
