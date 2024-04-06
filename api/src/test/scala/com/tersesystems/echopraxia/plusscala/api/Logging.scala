package com.tersesystems.echopraxia.plusscala.api

import java.time.Instant
import com.tersesystems.echopraxia.api.Value
import com.tersesystems.echopraxia.api.Attributes

// This trait should be extended for domain model classes
trait Logging extends LoggingBase with HeterogeneousFieldSupport {
  implicit val instantToValue: ToValue[Instant] = instant => ToValue(instant.toString)

  trait AsValueOnlyAndAbbreviate[T] extends ToValueAttributes[T] {
    def after: Int
    def toValue(v: T): Value[_] = ToValue(v.toString)
    def toAttributes(value: Value[_]): Attributes = {
      val abbreviateAfter = AbbreviateAfter(after)
      val attrs = abbreviateAfter.toAttributes(value)
      attrs.plusAll(AsValueOnly.attributes)
    }
  }

  object AsValueOnlyAndAbbreviate {
    def apply[T](a: Int): AsValueOnlyAndAbbreviate[T] = new AsValueOnlyAndAbbreviate[T]() {
      val after = a
    }
  }
}
