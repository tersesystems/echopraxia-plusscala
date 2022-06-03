package com.tersesystems.echopraxia.plusscala.trace

import com.tersesystems.echopraxia.api.{Field, FieldBuilderResult, Value}
import com.tersesystems.echopraxia.plusscala.api._
import sourcecode._

import java.util.Objects

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

  override val enteringTemplate: String = "{}: "

  override val exitingTemplate: String = "{}"

  override val throwingTemplate: String = "{}"

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
  val Tag: String       = "tag"
  val Entry: String     = "entry"
  val Exit: String      = "exit"
  val Throwing: String  = "throwing"
  val Arguments: String = "arguments"
  val Result: String    = "result"
  val Method            = "method"
}
