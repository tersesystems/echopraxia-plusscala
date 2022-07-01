package com.tersesystems.echopraxia.plusscala.trace

import com.tersesystems.echopraxia.api.{Field, FieldBuilderResult, Value}
import com.tersesystems.echopraxia.plusscala.api.{FieldBuilder, ListToFieldBuilderResultMethods, ValueTypeClasses}
import com.tersesystems.echopraxia.plusscala.trace.DefaultTraceFieldBuilder.{TraceArgumentValues, TraceSignature, string}
import sourcecode._

trait TraceFieldBuilder extends ValueTypeClasses with ListToFieldBuilderResultMethods {

  def sourceFields(implicit line: Line, file: File, enc: Enclosing, args: Args): SourceFields

  def enteringTemplate: String

  def exitingTemplate: String

  def throwingTemplate: String

  def entering(sourceFields: SourceFields): FieldBuilderResult

  def exiting(sourceFields: SourceFields, value: Value[_]): FieldBuilderResult

  def throwing(sourceFields: SourceFields, ex: Throwable): FieldBuilderResult
}

trait DefaultTraceFieldBuilder extends FieldBuilder with TraceFieldBuilder {
  import DefaultTraceFieldBuilder._

  override val enteringTemplate: String = "{}: {} - ({})"
  override val exitingTemplate: String  = "{}: {} - ({}) => {}"
  override val throwingTemplate: String = "{}: {} - ({}) ! {}"

  override def entering(sourceFields: SourceFields): FieldBuilderResult = {
    list(Seq(entryTag) ++ sourceFields.argumentFields)
  }

  override def exiting(sourceFields: SourceFields, retValue: Value[_]): FieldBuilderResult = {
    list(Seq(exitTag) ++ sourceFields.argumentFields :+ value(TraceResult, retValue))
  }

  override def throwing(sourceFields: SourceFields, ex: Throwable): FieldBuilderResult = {
    list(Seq(throwingTag) ++ sourceFields.argumentFields :+ exception(ex))
  }

  override def sourceFields(implicit line: Line, file: File, enc: Enclosing, args: Args): SourceFields =
    new DefaultSourceFields(line, file, enc, args)
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

trait SourceFields {
  def argumentFields: Seq[Field]

  def loggerFields: Seq[Field]
}

class DefaultSourceFields(line: Line, file: File, enc: Enclosing, args: Args) extends SourceFields {
  def signature = s"${enc.value}(${args.value.map(argumentTypes).mkString(",")})"

  def entryArguments(list: Seq[Text[_]]): String = {
    list.map(_.value).mkString(",")
  }

  def argumentTypes(list: Seq[Text[_]]): String = {
    list.map(txt => s"${txt.source}: ${txt.value.getClass.getSimpleName}").mkString(",")
  }

  override lazy val argumentFields: Seq[Field] = Seq(
    string(TraceSignature, signature),
    string(TraceArgumentValues, args.value.map(list => entryArguments(list)).mkString(","))
  )

  override lazy val loggerFields: Seq[Field] = {
    // XXX since sourcecode data is static, we could cache the result given the inputs
    // and save on some allocation.  Or would it be possible to turn this into a macro at
    // and point to constant fields and values?
    Seq(
      Field
        .keyValue(
          "sourcecode",
          Value.`object`(
            Field.keyValue("file", Value.string(file.value)),
            Field.keyValue("line", Value.number(line.value: java.lang.Integer)),
            Field.keyValue("enclosing", Value.string(enc.value))
          )
        )
    )
  }
}
