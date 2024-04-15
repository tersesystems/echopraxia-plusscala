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

  // scala 3 requires explicit Left and Right
  implicit def leftToValueAttributes[TVL: ToFieldAttributes]: ToFieldAttributes[Left[TVL, _]] = new ToFieldAttributes[Left[TVL, _]] {
    override def toValue(l: Left[TVL, _]): Value[_] = implicitly[ToFieldAttributes[TVL]].toValue(l.value)
    override def toAttributes(value: Value[_]): Attributes = {
      val left = implicitly[ToFieldAttributes[TVL]]
      left.toAttributes(value)
    }
  }

  implicit def rightToValueAttributes[TVR: ToFieldAttributes]: ToFieldAttributes[Right[_, TVR]] = new ToFieldAttributes[Right[_, TVR]] {
    override def toValue(l: Right[_, TVR]): Value[_] = implicitly[ToFieldAttributes[TVR]].toValue(l.value)
    override def toAttributes(value: Value[_]): Attributes = {
      val right = implicitly[ToFieldAttributes[TVR]]
      right.toAttributes(value)
    }
  }

  // // XXX Is there a way to make this work more easily?
  implicit def eitherToValueAttribute[TVL: ToFieldAttributes, TVR: ToFieldAttributes]: ToFieldAttributes[Either[TVL, TVR]] =
    new ToFieldAttributes[Either[TVL, TVR]] {
      // This isn't great, but we need to know whether left or right was picked for the attributes
      // and if we have a parameter (either: Either[]) in the method signature then it doesn't
      // pick it up?
      private var optEither: Option[Either[TVL, TVR]] = None

      override def toValue(v: Either[TVL, TVR]): Value[_] = {
        this.optEither = Some(v)
        v match {
          case Left(l)  => implicitly[ToFieldAttributes[TVL]].toValue(l)
          case Right(r) => implicitly[ToFieldAttributes[TVR]].toValue(r)
        }
      }

      override def toAttributes(value: Value[_]): Attributes = {
        // hack hack hack hack
        optEither match {
          case Some(either) =>
            either match {
              case Left(_) =>
                val left = implicitly[ToFieldAttributes[TVL]]
                left.toAttributes(value)
              case Right(_) =>
                val right = implicitly[ToFieldAttributes[TVR]]
                right.toAttributes(value)
            }
          case None =>
            // should never get here
            Attributes.empty()
        }
      }
    }
}
