package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api._
import com.tersesystems.echopraxia.spi.{EchopraxiaService, FieldConstants, PresentationHintAttributes}

import scala.reflect.runtime.{universe => ru}

import scala.collection.JavaConverters._

import scala.reflect.{ClassTag, classTag}
import com.tersesystems.echopraxia.plusscala.spi.Utils

// This trait should be extended for domain model classes
trait LoggingBase extends ValueTypeClasses with OptionValueTypes with EitherValueTypes {

  // Provides a default name for a field if not provided
  trait ToName[-T] {
    def toName(t: T): String
  }

  object ToName {
    def create[T](name: String): ToName[T] = _ => name
  }

  // Provides easier packaging for ToName and ToValue
  trait ToLog[-TF] {
    def toName: ToName[TF]
    def toValue: ToValue[TF]
  }

  object ToLog {

    def create[TF](name: String, valueFunction: TF => Value[_]): ToLog[TF] = new ToLog[TF] {
      override val toName: ToName[TF]   = ToName.create(name)
      override val toValue: ToValue[TF] = t => valueFunction(t)
    }

    def createFromClass[TF: ClassTag](valueFunction: TF => Value[_]): ToLog[TF] = new ToLog[TF] {
      override val toName: ToName[TF]   = ToName.create(classTag[TF].runtimeClass.getName)
      override val toValue: ToValue[TF] = t => valueFunction(t)
    }
  }

  // Allows custom attributes on fields through implicits
  trait ToValueAttribute[-T] {
    def toValue(v: T): Value[_]

    def toAttributes(value: Value[_]): Attributes
  }

  trait LowPriorityToValueAttributeImplicits {
    // default low priority implicit that gets applied if nothing is found
    implicit def empty[TV]: ToValueAttribute[TV] = new ToValueAttribute[TV] {
      override def toValue(v: TV): Value[_]                  = Value.nullValue()
      override def toAttributes(value: Value[_]): Attributes = Attributes.empty()
    }
  }

  object ToValueAttribute extends LowPriorityToValueAttributeImplicits

  // implicit conversion from a ToLog to a ToValue
  implicit def convertToLogToValue[TL: ToLog]: ToValue[TL] = implicitly[ToLog[TL]].toValue

  // implicit conversion from a ToLog to a ToName
  implicit def convertToLogToName[TL: ToLog]: ToName[TL] = implicitly[ToLog[TL]].toName

  // Convert a tuple into a field.  This does most of the heavy lifting.
  // i.e logger.info("foo" -> foo) becomes logger.info(Field.keyValue("foo", ToValue(foo)))
  implicit def tupleToField[TV: ToValue: ru.TypeTag](tuple: (String, TV)): Field = keyValue(tuple._1, tuple._2)

  // Convert an object with implicit ToValue and ToName to a field.
  // i.e. logger.info(foo) becomes logger.info(Field.keyValue(ToName[Foo].toName, ToValue(foo)))
  implicit def nameAndValueToField[TV: ToValue: ToName: ru.TypeTag](value: TV): Field =
    keyValue(implicitly[ToName[TV]].toName(value), value)

  // All exceptions should use "exception" field constant by default
  implicit def throwableToName[T <: Throwable]: ToName[T] = ToName.create(FieldConstants.EXCEPTION)

  // Creates a field, this is private so it's not exposed to traits that extend this
  private def keyValue[TV: ToValue: ru.TypeTag](name: String, tv: TV): Field = {
    Utils.newField(name, ToValue(tv), ru.typeTag[TV])
  }
}

object LoggingBase {

  def withAttributes(seq: Attribute[_]*): Attributes = {
    Attributes.create(seq.asJava)
  }

  // Add a custom string format attribute using the passed in value
  def withStringFormat(value: Value[_]): Attribute[_] = {
    PresentationHintAttributes.withToStringFormat(new SimpleFieldVisitor() {
      override def visit(f: Field): Field = Field.keyValue(f.name(), value)
    })
  }

  def withDisplayName(name: String): Attribute[_] = PresentationHintAttributes.withDisplayName(name)

  def abbreviateAfter(after: Int): Attribute[_] = PresentationHintAttributes.abbreviateAfter(after)

  def elided: Attribute[_] = PresentationHintAttributes.asElided()

  def asValueOnly: Attribute[_] = PresentationHintAttributes.asValueOnly()

  def asCardinal: Attribute[_] = PresentationHintAttributes.asCardinal()
}
