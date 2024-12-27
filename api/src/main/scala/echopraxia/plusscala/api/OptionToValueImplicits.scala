package echopraxia.plusscala.api

import echopraxia.api.Value

/**
 * This trait resolves options to either the value, or nullValue if `None`.
 */
trait OptionToValueImplicits { self: ValueTypeClasses =>
  implicit def optionToValue[V: ToValue]: ToValue[Option[V]] = {
    case Some(v) => ToValue(v)
    case None    => Value.nullValue()
  }
  implicit def someToValue[V: ToValue]: ToValue[Some[V]] = v => ToValue(v)
  implicit val noneToValue: ToValue[None.type]           = _ => Value.nullValue()
}
