package echopraxia.plusscala.trace

import echopraxia.api.Field
import echopraxia.api.FieldBuilderResult
import echopraxia.api.Value
import echopraxia.plusscala.api.FieldBuilder
import echopraxia.plusscala.api.FieldBuilderResultTypeClasses
import echopraxia.plusscala.api.ListFieldBuilder
import echopraxia.plusscala.api.SourceCode
import echopraxia.plusscala.api.ValueTypeClasses
import sourcecode.*

import scala.jdk.CollectionConverters.CollectionHasAsScala

import DefaultTraceFieldBuilder.TraceArgumentValues
import DefaultTraceFieldBuilder.TraceSignature
import DefaultTraceFieldBuilder.string

trait TraceFieldBuilder extends ValueTypeClasses with FieldBuilderResultTypeClasses with ListFieldBuilder {

  def sourceFields(implicit line: Line, file: File, enc: Enclosing, args: Args): SourceFields

  def enteringTemplate: String

  def exitingTemplate: String

  def throwingTemplate: String

  def entering(sourceFields: SourceFields): FieldBuilderResult

  def exiting(sourceFields: SourceFields, value: Value[?]): FieldBuilderResult

  def throwing(sourceFields: SourceFields, ex: Throwable): FieldBuilderResult
}

trait DefaultTraceFieldBuilder extends FieldBuilder with TraceFieldBuilder {

  override val enteringTemplate: String = "{}: {} - ({})"
  override val exitingTemplate: String  = "{}: {} - ({}) => {}"
  override val throwingTemplate: String = "{}: {} - ({}) ! {}"

  override def entering(sourceFields: SourceFields): FieldBuilderResult = {
    list(Seq(DefaultTraceFieldBuilder.entryTag) ++ sourceFields.argumentFields)
  }

  override def exiting(sourceFields: SourceFields, retValue: Value[?]): FieldBuilderResult = {
    list(Seq(DefaultTraceFieldBuilder.exitTag) ++ sourceFields.argumentFields :+ value(DefaultTraceFieldBuilder.TraceResult, retValue))
  }

  override def throwing(sourceFields: SourceFields, ex: Throwable): FieldBuilderResult = {
    list(Seq(DefaultTraceFieldBuilder.throwingTag) ++ sourceFields.argumentFields :+ exception(ex))
  }

  override def sourceFields(implicit line: Line, file: File, enc: Enclosing, args: Args): SourceFields =
    new DefaultSourceFields(SourceCode(line, file, enc), args)
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

class DefaultSourceFields(sc: SourceCode, args: Args) extends SourceFields {
  def signature: String = s"${sc.enclosing.value}(${args.value.map(argumentTypes).mkString(",")})"

  def entryArguments(list: Seq[Text[?]]): String = {
    list.map(_.value).mkString(",")
  }

  def argumentTypes(list: Seq[Text[?]]): String = {
    list.map(txt => s"${txt.source}: ${txt.value.getClass.getSimpleName}").mkString(",")
  }

  override lazy val argumentFields: Seq[Field] = Seq(
    string(TraceSignature, signature),
    string(TraceArgumentValues, args.value.map(list => entryArguments(list)).mkString(","))
  )

  override lazy val loggerFields: Seq[Field] = {
    val fb = FieldBuilder
    fb.sourceCode(sc).fields().asScala.toSeq
  }
}
