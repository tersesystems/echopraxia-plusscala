package echopraxia.plusscala.api

import echopraxia.api.*

private[api] object Utils {

  @inline
  def newField(name: String, value: Value[?], attributes: Attributes): Field = {
    Field.create(name, value, attributes, classOf[DefaultField])
  }

}
