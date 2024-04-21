package com.tersesystems.echopraxia.plusscala.api

/**
 * Trait defining ToName.
 *
 *
 */
trait NameValueClasses {
  // Provides a default name for a field if not provided
  trait ToName[-T] {
    def toName(t: T): String
  }

  object ToName {

    def apply[T: ToName](t: T): String = implicitly[ToName[T]].toName(t)

    def create[T](name: String): ToName[T] = _ => name
  }
}