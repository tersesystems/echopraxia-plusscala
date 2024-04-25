package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.Attributes
import com.tersesystems.echopraxia.api.Field
import com.tersesystems.echopraxia.plusscala.spi.Utils

trait FieldConversionImplicits { self: ValueTypeClasses with NameTypeClass =>

  // Convert a tuple into a field.  This does most of the heavy lifting.
  // i.e logger.info("foo" -> foo) becomes logger.info(Field.keyValue("foo", ToValue(foo)))
  implicit def tupleToField[TN: ToName, TV: ToValue](tuple: (TN, TV)): Field = newField(tuple._1, tuple._2)

  // Convert an object with implicit ToValue and ToName to a field.
  // i.e. logger.info(foo) becomes logger.info(Field.keyValue(ToName[Foo].toName, ToValue(foo)))
  implicit def nameAndValueToField[TV: ToName: ToValue](value: TV): Field = newField(value, value)

  // Creates a field, this is private so it's not exposed to traits that extend this
  @inline
  private def newField[TN: ToName, TV: ToValue](name: TN, tv: TV): Field = {
    Utils.newField(implicitly[ToName[TN]].toName(Option(name)), ToValue(tv), Attributes.empty)
  }
}
