package com.tersesystems.echopraxia.plusscala.spi

import echopraxia.api._

object Utils {

  @inline
  def newField(name: String, value: Value[_], attributes: Attributes): Field = newField(name, value, attributes, classOf[DefaultField])

  @inline
  def newField[F <: Field](name: String, value: Value[_], attributes: Attributes, fieldClass: Class[_ <: F]): F = {
    Field.create(name, value, attributes, fieldClass)
  }

}
