package com.tersesystems.echopraxia.plusscala.logger2

// Provides a default name for a field if not provided
trait ToName[-T] {
  def toName(t: T): String
}

object ToName {
  def create[T](name: String): ToName[T] = _ => name
}
