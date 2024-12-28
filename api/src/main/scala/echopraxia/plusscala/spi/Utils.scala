package echopraxia.plusscala.spi

import echopraxia.api.*

object Utils {

  @inline
  def newField(name: String, value: Value[?], attributes: Attributes): Field = {
    Field.create(name, value, attributes, classOf[DefaultField])
  }

}
