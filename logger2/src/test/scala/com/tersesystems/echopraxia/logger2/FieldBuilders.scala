package com.tersesystems.echopraxia.logger2

import com.tersesystems.echopraxia.plusscala.api._
import com.tersesystems.echopraxia.spi.FieldConstants

import java.time.Instant

trait BookFieldBuilder extends PresentationFieldBuilder {

  implicit val instantToStringValue: ToValue[Instant] = (t: Instant) => ToValue(t.toString)

  implicit val bookToObjectValue: ToObjectValue[Book] = { book =>
    ToObjectValue(
      keyValue("category", book.category),
      keyValue("author", book.author),
      keyValue("title", book.title),
      keyValue("price", book.price)
    )
  }

  def apply[V: ToValue](tuple: (String, V)) = keyValue(tuple)
  def apply[V: ToValue](key: String, value: V) = keyValue(key, value)
  def apply(e: Throwable) = keyValue(FieldConstants.EXCEPTION, e)
}

object BookFieldBuilder extends BookFieldBuilder

