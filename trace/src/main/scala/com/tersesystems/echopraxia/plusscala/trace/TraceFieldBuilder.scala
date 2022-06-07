package com.tersesystems.echopraxia.plusscala.trace

import com.tersesystems.echopraxia.api.{Field, FieldBuilderResult, Value}
import com.tersesystems.echopraxia.plusscala.api.{FieldBuilder, SourceCodeFieldBuilder, ValueTypeClasses}
import sourcecode._

trait TraceFieldBuilder extends SourceCodeFieldBuilder with ValueTypeClasses {

  def enteringTemplate: String

  def exitingTemplate: String

  def throwingTemplate: String

  def entering(implicit line: Line, file: File, enc: Enclosing, args: Args): FieldBuilderResult

  def exiting(value: Value[_])(implicit line: Line, file: File, enc: Enclosing, args: Args): FieldBuilderResult

  def throwing(ex: Throwable)(implicit line: Line, file: File, enc: Enclosing, args: Args): FieldBuilderResult
}

trait DefaultTraceFieldBuilder extends FieldBuilder with TraceFieldBuilder {
  import DefaultTraceFieldBuilder._

  override val enteringTemplate: String = "{}: {}({})"

  override val exitingTemplate: String = "{}: {}({}) => {}"

  override val throwingTemplate: String = "{}: {}({}) ! {}"

  def argumentTypes(list: Seq[Text[_]]): String = {
    list.map(txt => s"${txt.source}: ${txt.value.getClass.getSimpleName}").mkString(",")
  }

  def argumentsValues(implicit args: Args): Field = {
    string(TraceArgumentValues, args.value.map(list => entryArguments(list)).mkString(","))
  }

  def entryArguments(list: Seq[Text[_]]): String = {
    list.map(_.value).mkString(",")
  }

  override def entering(implicit line: Line, file: File, enc: Enclosing, args: Args): FieldBuilderResult = {
    list(string(TraceSignature, signature), entryTag, argumentsValues)
  }

  override def exiting(retValue: Value[_])(implicit line: Line, file: File, enc: Enclosing, args: Args): FieldBuilderResult = {
    list(string(TraceSignature, signature), exitTag, argumentsValues, value(TraceResult, retValue))
  }

  override def throwing(ex: Throwable)(implicit line: Line, file: File, enc: Enclosing, args: Args): FieldBuilderResult = {
    list(string(TraceSignature, signature), throwingTag, exception(ex))
  }

  def signature(implicit enc: Enclosing, args: Args) = s"${enc.value}(${args.value.map(argumentTypes).mkString(",")})"
}

object DefaultTraceFieldBuilder extends DefaultTraceFieldBuilder {

  val TraceTag: String = "traceTag"
  val Entry: String    = "entry"
  val Exit: String     = "exit"
  val Throwing: String = "throwing"

  val TraceResult: String         = "traceResult"
  val TraceSignature: String      = "traceSignature"
  val TraceArgumentValues: String = "traceArgumentsValues"

  val entryTag: Field    = value(TraceTag, Entry)
  val exitTag: Field     = value(TraceTag, Exit)
  val throwingTag: Field = value(TraceTag, Throwing)
}
