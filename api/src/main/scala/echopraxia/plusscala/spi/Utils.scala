package echopraxia.plusscala.spi

import echopraxia.api.*

object Utils {

  @inline
  def newField(name: String, value: Value[?], attributes: Attributes): Field = newField(name, value, attributes, classOf[DefaultField])

  @inline
  def newField[F <: Field](name: String, value: Value[?], attributes: Attributes, fieldClass: Class[? <: F]): F = {
    Field.create(name, value, attributes, fieldClass)
  }

}
