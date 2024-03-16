package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.Attributes
import com.tersesystems.echopraxia.api.Value

// Allows custom attributes on fields through implicits
trait ToValueAttribute[-T] {
  def toValue(v: T): Value[_]
  def toAttributes(value: Value[_]): Attributes
}

trait LowPriorityToValueAttributeImplicits {
  // default low priority implicit that gets applied if nothing is found
  implicit def empty[TV]: ToValueAttribute[TV] = new ToValueAttribute[TV] {
    override def toValue(v: TV): Value[_]                  = Value.nullValue()
    override def toAttributes(value: Value[_]): Attributes = Attributes.empty()
  }
}

object ToValueAttribute extends LowPriorityToValueAttributeImplicits {
  def attributes(value: Value[_], ev: ToValueAttribute[_]): Attributes = ev.toAttributes(value)
}

