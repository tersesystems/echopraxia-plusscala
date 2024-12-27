package echopraxia.plusscala.logging.api

import echopraxia.api.Field
import echopraxia.logging.api.{LoggingContext => JLoggingContext}

trait LoggingContext {
  def fields: Seq[Field]

  def argumentFields: Seq[Field]

  def loggerFields: Seq[Field]

  def asJava: JLoggingContext
}
