package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.Attributes
import com.tersesystems.echopraxia.api.Value

/**
 * This trait resolves `Either` directly to the relevant value.
 */
trait EitherValueTypes { self: ValueTypeClasses =>
  implicit def eitherToValue[L: ToValue, R: ToValue]: ToValue[Either[L, R]] = {
    case Left(left)   => ToValue(left)
    case Right(right) => ToValue(right)
  }
  implicit def leftToValue[L: ToValue, R]: ToValue[Left[L, R]]   = v => ToValue(v.left.get)
  implicit def rightToValue[L, R: ToValue]: ToValue[Right[L, R]] = v => ToValue(v.right.get)
}
