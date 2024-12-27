package echopraxia.plusscala.logging.api

import echopraxia.api.{Field, FieldBuilderResult}

trait LoggerSupport[FB, LoggerType[_ <: FB]] { self =>

  def withCondition(scalaCondition: Condition): LoggerType[FB]

  def withFields(f: FB => FieldBuilderResult): LoggerType[FB]

  def withFields(fields: => Seq[Field]): LoggerType[FB] = {
    withFields(_ => FieldBuilderResult.list(fields.toArray))
  }

  def withThreadContext: LoggerType[FB]

  def withFieldBuilder[T <: FB](newBuilder: T): LoggerType[T]
}
