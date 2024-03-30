package com.tersesystems.echopraxia.plusscala.api

/**
 * This trait renders iterables as an array value.
 *
 * This is particularly helpful in the case foo and bar are two different types.
 *
 * We need an explicit ascription to Seq[Field], but the compiler will do the rest for us:
 *
 * {{{
 * logger.info("foo" -> Seq[Field](foo, bar)) // "foo": [foo, bar]
 * }}}
 */
trait HeterogenousFieldSupport { self: ValueTypeClasses =>
  // Render iterables as arrays (user may want to render as object, so this is broken out)
  implicit def iterableToArrayValue[V: ToValue]: ToArrayValue[Iterable[V]] = ToArrayValue.iterableToArrayValue[V]
}
