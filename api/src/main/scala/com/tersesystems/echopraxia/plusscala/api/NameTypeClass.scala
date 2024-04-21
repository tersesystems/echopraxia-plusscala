package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.spi.FieldConstants

/**
 * Add this trait to get access to the ToName type class.
 */
trait NameTypeClass {
  // this needs to be a dependent type because implicit type resolution only works on a
  // field builder if it's dependent to the type itself.
  trait ToName[-T] {
    def toName(t: T): String
  }

  object ToName {
    implicit def throwableToName[T <: Throwable]: ToName[T] = _ => FieldConstants.EXCEPTION
    implicit val sourceCodeToName: ToName[SourceCode]       = _ => SourceCode.SourceCode

    def apply[T: ToName](t: T): String = implicitly[ToName[T]].toName(t)
  }
}
