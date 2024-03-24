package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.Attributes
import com.tersesystems.echopraxia.api.Value

/**
 * This trait resolves options to either the value, or nullValue if `None`.
 */
trait OptionValueTypes { self: ValueTypeClasses =>
  implicit def optionToValue[V: ToValue]: ToValue[Option[V]] = {
    case Some(v) => ToValue(v)
    case None    => Value.nullValue()
  }
  implicit def someToValue[V: ToValue]: ToValue[Some[V]] = v => ToValue(v)
  implicit val noneToValue: ToValue[None.type]           = _ => Value.nullValue()

  implicit def optionToValueAttributes[TV: ToValueAttributes]: ToValueAttributes[Option[TV]] = new ToValueAttributes[Option[TV]] {
    override def toValue(v: Option[TV]): Value[_] = v match {
      case Some(tv) =>
        val ev = implicitly[ToValueAttributes[TV]]
        ev.toValue(tv)
      case None => Value.nullValue()
    }

    override def toAttributes(value: Value[_]): Attributes = implicitly[ToValueAttributes[TV]].toAttributes(value)
  }
}
