package com.tersesystems.echopraxia.plusscala.flow

import com.tersesystems.echopraxia.api.{Field, FieldBuilderResult, Value}
import com.tersesystems.echopraxia.plusscala.api._

trait FlowFieldBuilder extends SourceCodeFieldBuilder with ValueTypeClasses {

  def enteringTemplate: String

  def exitingTemplate: String

  def throwingTemplate: String

  def entering: FieldBuilderResult

  def exiting(value: Value[_]): FieldBuilderResult

  def throwing(ex: Throwable): FieldBuilderResult
}

trait DefaultFlowFieldBuilder extends FieldBuilder with FlowFieldBuilder {
  import DefaultFlowFieldBuilder._

  override val enteringTemplate: String = "{}"

  override val exitingTemplate: String = "{} => {}"

  override val throwingTemplate: String = "{} ! {}"

  override def entering: FieldBuilderResult = {
    keyValue(Tag, Entry)
  }

  override def exiting(retValue: Value[_]): FieldBuilderResult = {
    list(exitTag, keyValue(Result, retValue))
  }

  override def throwing(ex: Throwable): FieldBuilderResult = {
    list(throwingTag, exception(ex))
  }
}

object DefaultFlowFieldBuilder extends DefaultFlowFieldBuilder {
  val Tag: String   = "flowTag"
  val Entry: String = "entry"
  val Exit: String  = "exit"

  val Throwing: String = "throwing"
  val Result: String   = "result"

  val entryTag: Field    = keyValue(Tag, Entry)
  val exitTag: Field     = keyValue(Tag, Exit)
  val throwingTag: Field = keyValue(Tag, Throwing)
}
