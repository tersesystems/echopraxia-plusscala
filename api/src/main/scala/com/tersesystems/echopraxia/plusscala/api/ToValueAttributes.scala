package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.Attributes
import com.tersesystems.echopraxia.api.Value

// Allows custom attributes on fields through implicits
trait ToValueAttributes[-T] {
  def toValue(v: T): Value[_]
  def toAttributes(value: Value[_]): Attributes
}

trait LowPriorityToValueAttributesImplicits {
  // default low priority implicit that gets applied if nothing is found
  implicit def empty[TV]: ToValueAttributes[TV] = new ToValueAttributes[TV] {
    override def toValue(v: TV): Value[_]                  = Value.nullValue()
    override def toAttributes(value: Value[_]): Attributes = Attributes.empty()
  }
}

object ToValueAttributes extends LowPriorityToValueAttributesImplicits {
  def attributes(value: Value[_], ev: ToValueAttributes[_]): Attributes = ev.toAttributes(value)
}
