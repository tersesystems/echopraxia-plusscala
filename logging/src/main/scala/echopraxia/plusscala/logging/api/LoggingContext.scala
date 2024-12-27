package echopraxia.plusscala.logging.api

import echopraxia.api.Field
import echopraxia.logging.api.{LoggingContext => JLoggingContext}

trait LoggingContext {
  def fields: scala.collection.immutable.Seq[Field]

  def argumentFields: scala.collection.immutable.Seq[Field]

  def loggerFields: scala.collection.immutable.Seq[Field]

  def asJava: JLoggingContext
}
