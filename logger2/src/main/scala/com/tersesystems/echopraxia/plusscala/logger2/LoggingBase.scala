package com.tersesystems.echopraxia.plusscala.logger2

import com.tersesystems.echopraxia.api._
import com.tersesystems.echopraxia.plusscala.api.{EitherValueTypes, OptionValueTypes, ValueTypeClasses, FutureValueTypes, ToLogTypes}
import com.tersesystems.echopraxia.spi.{EchopraxiaService, FieldConstants, FieldCreator}

import com.tersesystems.echopraxia.plusscala.api.ToName
import com.tersesystems.echopraxia.plusscala.api.ToValueAttribute

// This trait should be extended for domain model classes
trait LoggingBase extends ValueTypeClasses with OptionValueTypes with EitherValueTypes with FutureValueTypes with ToLogTypes {

  // XXX this is awkward
  // if foo and bar are two different types, we need an explicit ascription to Seq[Field]
  // logger.info("foo" -> Seq[Field](foo, bar))
  implicit def iterableToArrayValue[V: ToValue]: ToArrayValue[Iterable[V]] = ToArrayValue.iterableToArrayValue[V]

  // This sets up some names internally and adds implicit ToValueAttributes for better presentation

  // Convert a tuple into a field.  This does most of the heavy lifting.
  // i.e logger.info("foo" -> foo) becomes logger.info(Field.keyValue("foo", ToValue(foo)))
  implicit def tupleToField[TV: ToValue](tuple: (String, TV))(implicit va: ToValueAttribute[TV]): Field = keyValue(tuple._1, tuple._2)

  // Convert an object with implicit ToValue and ToName to a field.
  // i.e. logger.info(foo) becomes logger.info(Field.keyValue(ToName[Foo].toName, ToValue(foo)))
  implicit def nameAndValueToField[TV: ToValue: ToName](value: TV)(implicit va: ToValueAttribute[TV]): Field =
    keyValue(implicitly[ToName[TV]].toName(value), value)

  // All exceptions should use "exception" field constant by default
  implicit def throwableToName[T <: Throwable]: ToName[T] = com.tersesystems.echopraxia.plusscala.api.ToName.create(FieldConstants.EXCEPTION)

  // Creates a field, this is private so it's not exposed to traits that extend this
  private def keyValue[TV: ToValue](name: String, tv: TV)(implicit va: ToValueAttribute[TV]): Field = {
    LoggingBase.fieldCreator.create(name, ToValue(tv), va.toAttributes(va.toValue(tv)))
  }
}

object LoggingBase {
  val fieldCreator: FieldCreator[PresentationField] = EchopraxiaService.getInstance.getFieldCreator(classOf[PresentationField])
}
