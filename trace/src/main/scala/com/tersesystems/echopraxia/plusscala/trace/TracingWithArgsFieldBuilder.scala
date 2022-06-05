package com.tersesystems.echopraxia.plusscala.trace

import com.tersesystems.echopraxia.api.{Field, FieldBuilderResult, Value}
import com.tersesystems.echopraxia.plusscala.api.{FieldBuilder, SourceCodeFieldBuilder, ValueTypeClasses}
import sourcecode.{Args, Enclosing, File, Line}


trait TracingWithArgsFieldBuilder extends SourceCodeFieldBuilder with ValueTypeClasses {

  def enteringTemplate: String

  def exitingTemplate: String

  def throwingTemplate: String

  def entering(implicit line: Line, file: File,  enc: Enclosing, args: Args): FieldBuilderResult

  def exiting(value: Value[_])(implicit line: Line, file: File,  enc: Enclosing, args: Args): FieldBuilderResult

  def throwing(ex: Throwable)(implicit line: Line, file: File,  enc: Enclosing, args: Args): FieldBuilderResult
}

trait DefaultTracingWithArgsFieldBuilder extends FieldBuilder with TracingWithArgsFieldBuilder {
  import DefaultTracingWithArgsFieldBuilder._

  override val enteringTemplate: String = "{} {}: {}"

  override val exitingTemplate: String = "{} {}: {} => {}"

  override val throwingTemplate: String = "{} {}: {} ! {}"

  override def entering(implicit line: Line, file: File, enc: Enclosing, args: Args): FieldBuilderResult = {
    val argsValue = ToArrayValue(args.value.map(_.source))
    list(keyValue(DefaultTracingWithArgsFieldBuilder.Enclosing, enc.value), keyValue(Arguments, argsValue), entryTag)
  }

  override def exiting(retValue: Value[_])(implicit line: Line, file: File,  enc: Enclosing, args: Args): FieldBuilderResult = {
    val argsValue = ToArrayValue(args.value.map(_.source))
    list(keyValue(DefaultTracingWithArgsFieldBuilder.Enclosing, enc.value), keyValue(Arguments, argsValue), exitTag, keyValue(Result, retValue))
  }

  override def throwing(ex: Throwable)(implicit line: Line, file: File,  enc: Enclosing, args: Args): FieldBuilderResult = {
    val argsValue = ToArrayValue(args.value.map(_.source))
    list(keyValue(DefaultTracingWithArgsFieldBuilder.Enclosing, enc.value), keyValue(Arguments, argsValue), throwingTag, exception(ex))
  }
}

object DefaultTracingWithArgsFieldBuilder extends DefaultTracingWithArgsFieldBuilder {

  val Tag: String       = "tag"
  val Entry: String     = "entry"
  val Exit: String      = "exit"
  val Throwing: String  = "throwing"

  val Arguments: String = "arguments"
  val Result: String    = "result"
  val Enclosing: String = "enclosing"

  val entryTag: Field = keyValue(Tag, Entry)
  val exitTag: Field = keyValue(Tag, Exit)
  val throwingTag: Field = keyValue(Tag, Throwing)
}