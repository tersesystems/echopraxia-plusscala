package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.Field
import com.tersesystems.echopraxia.api.Value

import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

trait FutureValueTypes { self: ValueTypeClasses =>
  implicit def futureToValue[T: ToValue]: ToValue[Future[T]] = { f =>
    f.value match {
      case Some(value) =>
        value match {
          case Failure(exception) => ToObjectValue(Field.keyValue("completed", ToValue(true)), Field.keyValue("failure", ToValue(exception)))
          case Success(value)     => ToObjectValue(Field.keyValue("completed", ToValue(true)), Field.keyValue("success", ToValue(value)))
        }
      case None =>
        Value.`object`(Field.keyValue("completed", ToValue(false)))
    }
  }

  // Don't define name, as this can be very different depending on the field name requirements (Elasticsearch in particular)
}
