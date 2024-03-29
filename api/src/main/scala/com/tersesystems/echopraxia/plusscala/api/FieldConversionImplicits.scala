package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.Field
import com.tersesystems.echopraxia.plusscala.spi.Utils

trait FieldConversionImplicits { self: ValueTypeClasses =>

  // Convert a tuple into a field.  This does most of the heavy lifting.
  // i.e logger.info("foo" -> foo) becomes logger.info(Field.keyValue("foo", ToValue(foo)))
  implicit def tupleToField[TV: ToValue: ToValueAttributes](tuple: (String, TV)): Field = newField(tuple._1, tuple._2)

  // Convert an object with implicit ToValue and ToName to a field.
  // i.e. logger.info(foo) becomes logger.info(Field.keyValue(ToName[Foo].toName, ToValue(foo)))
  implicit def nameAndValueToField[TV: ToValue: ToName: ToValueAttributes](value: TV): Field =
    newField(implicitly[ToName[TV]].toName(value), value)

  // Creates a field, this is private so it's not exposed to traits that extend this
  @inline
  private def newField[TV: ToValue](name: String, tv: TV)(implicit va: ToValueAttributes[TV]): Field = {
    Utils.newField(name, ToValue(tv), va.toAttributes(va.toValue(tv)))
  }
}
