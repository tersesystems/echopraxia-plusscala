package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api._
import com.tersesystems.echopraxia.spi.{EchopraxiaService, FieldConstants, FieldCreator, PresentationHintAttributes}

// This trait should be extended for domain model classes
trait Logging extends LoggingBase {

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

  // Convert a tuple into a field.  This does most of the heavy lifting.
  // i.e logger.info("foo" -> foo) becomes logger.info(Field.keyValue("foo", ToValue(foo)))
  implicit def tupleToFieldWithAttribute[TV: ToValue](tuple: (String, TV))(implicit va: ToValueAttribute[TV]): Field = keyValue(tuple._1, tuple._2)

  // Convert an object with implicit ToValue and ToName to a field.
  // i.e. logger.info(foo) becomes logger.info(Field.keyValue(ToName[Foo].toName, ToValue(foo)))
  implicit def nameAndValueToFieldWithAttribute[TV: ToValue: ToName](value: TV)(implicit va: ToValueAttribute[TV]): Field =
    keyValue(implicitly[ToName[TV]].toName(value), value)

  // Creates a field, this is private so it's not exposed to traits that extend this
  private def keyValue[TV: ToValue](name: String, tv: TV)(implicit va: ToValueAttribute[TV]): Field = {
    Logging.fieldCreator.create(name, ToValue(tv), va.toAttributes(va.toValue(tv)))
  }
}

object Logging {
  val fieldCreator: FieldCreator[PresentationField] = EchopraxiaService.getInstance.getFieldCreator(classOf[PresentationField])

  def withAttributes(seq: Attribute[_]*): Attributes = {
    import scala.collection.JavaConverters._
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
