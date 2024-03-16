package com.tersesystems.echopraxia.plusscala.api

/**
  * If foo and bar are two different types, we need an explicit ascription to Seq[Field].
  *
  * {{{
  * logger.info("foo" -> Seq[Field](foo, bar))
  * }}}
  */
trait IterableToArrayValueImplicit { self: ValueTypeClasses =>
  implicit def iterableToArrayValue[V: ToValue]: ToArrayValue[Iterable[V]] = ToArrayValue.iterableToArrayValue[V]
}
