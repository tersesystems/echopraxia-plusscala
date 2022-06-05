package com.tersesystems.echopraxia.plusscala.trace

import com.tersesystems.echopraxia.api.{Field, FieldBuilderResult, Value}
import com.tersesystems.echopraxia.plusscala.api.{FieldBuilder, SourceCodeFieldBuilder, ValueTypeClasses}
import sourcecode.{Args, Enclosing, File, Line}


trait VerboseTracingFieldBuilder extends SourceCodeFieldBuilder with ValueTypeClasses {

  def enteringTemplate: String

  def exitingTemplate: String

  def throwingTemplate: String

  def entering(implicit line: Line, file: File,  enc: Enclosing, args: Args): FieldBuilderResult

  def exiting(value: Value[_])(implicit line: Line, file: File,  enc: Enclosing, args: Args): FieldBuilderResult

  def throwing(ex: Throwable)(implicit line: Line, file: File,  enc: Enclosing, args: Args): FieldBuilderResult
}

trait DefaultVerboseTracingFieldBuilder extends FieldBuilder with VerboseTracingFieldBuilder {
  import DefaultVerboseTracingFieldBuilder._

  override val enteringTemplate: String = "{} {}: {}"

  override val exitingTemplate: String = "{}: {} => {}"

  override val throwingTemplate: String = "{}: {} ! {}"

  override def entering(implicit line: Line, file: File, enc: Enclosing, args: Args): FieldBuilderResult = {
    list(string(Signature, signature), entryTag)
  }

  override def exiting(retValue: Value[_])(implicit line: Line, file: File,  enc: Enclosing, args: Args): FieldBuilderResult = {
    list(string(Signature, signature), exitTag, keyValue(Result, retValue))
  }

  override def throwing(ex: Throwable)(implicit line: Line, file: File,  enc: Enclosing, args: Args): FieldBuilderResult = {
    list(string(Signature, signature), throwingTag, exception(ex))
  }

  def signature(implicit enc: Enclosing, args: Args) = s"${enc.value}(${args.value.map(_.source)})"
}

object DefaultVerboseTracingFieldBuilder extends DefaultVerboseTracingFieldBuilder {

  val Tag: String       = "tag"
  val Entry: String     = "entry"
  val Exit: String      = "exit"
  val Throwing: String  = "throwing"

  val Result: String    = "result"
  val Signature: String    = "signature"

  val entryTag: Field = keyValue(Tag, Entry)
  val exitTag: Field = keyValue(Tag, Exit)
  val throwingTag: Field = keyValue(Tag, Throwing)
}