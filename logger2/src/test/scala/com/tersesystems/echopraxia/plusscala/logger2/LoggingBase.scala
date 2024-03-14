package com.tersesystems.echopraxia.plusscala.logger2

import com.tersesystems.echopraxia.api._
import com.tersesystems.echopraxia.plusscala.api.{EitherValueTypes, OptionValueTypes, ValueTypeClasses}
import com.tersesystems.echopraxia.spi.{EchopraxiaService, FieldConstants, FieldCreator}

import scala.reflect.{ClassTag, classTag}
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

trait FutureValueTypes { self: ValueTypeClasses =>
  implicit def futureToValue[T: ToValue]: ToValue[Future[T]] = { f =>
    f.value match {
      case Some(value) =>
        value match {
          case Failure(exception) => ToObjectValue(Field.keyValue("completed", ToValue(true)), Field.keyValue("failure", ToValue(exception)))
          case Success(value)     => ToObjectValue(Field.keyValue("completed", ToValue(true)), Field.keyValue("success", ToValue(value)))
        }
      case None =>
        Value.`object`(Field.keyValue("completed", ToValue(false)))
    }
  }

  // Don't define name, as this can be very different depending on the field name requirements (Elasticsearch in particular)
}

trait ToLogTypes { self: ValueTypeClasses =>
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
  }

  // implicit conversion from a ToLog to a ToValue
  implicit def convertToLogToValue[TL: ToLog]: ToValue[TL] = implicitly[ToLog[TL]].toValue

  // implicit conversion from a ToLog to a ToName
  implicit def convertToLogToName[TL: ToLog]: ToName[TL] = implicitly[ToLog[TL]].toName
}

trait ToValueAttributeTypes {

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
}

// This trait should be extended for domain model classes
trait LoggingBase
    extends ValueTypeClasses
    with OptionValueTypes
    with EitherValueTypes
    with FutureValueTypes
    with ToLogTypes
    with ToValueAttributeTypes {

  // XXX This should be something the framework does for us
  implicit def iterableToArrayValue[V: ToValue]: ToArrayValue[Iterable[V]] = ToArrayValue.iterableToArrayValue[V]

  // Convert a tuple into a field.  This does most of the heavy lifting.
  // i.e logger.info("foo" -> foo) becomes logger.info(Field.keyValue("foo", ToValue(foo)))
  implicit def tupleToField[TV: ToValue](tuple: (String, TV))(implicit va: ToValueAttribute[TV]): Field = keyValue(tuple._1, tuple._2)

  // Convert an object with implicit ToValue and ToName to a field.
  // i.e. logger.info(foo) becomes logger.info(Field.keyValue(ToName[Foo].toName, ToValue(foo)))
  implicit def nameAndValueToField[TV: ToValue: ToName](value: TV)(implicit va: ToValueAttribute[TV]): Field =
    keyValue(implicitly[ToName[TV]].toName(value), value)

  // All exceptions should use "exception" field constant by default
  implicit def throwableToName[T <: Throwable]: ToName[T] = ToName.create(FieldConstants.EXCEPTION)

  // Creates a field, this is private so it's not exposed to traits that extend this
  private def keyValue[TV: ToValue](name: String, tv: TV)(implicit va: ToValueAttribute[TV]): Field = {
    LoggingBase.fieldCreator.create(name, ToValue(tv), va.toAttributes(va.toValue(tv)))
  }
}

object LoggingBase {
  val fieldCreator: FieldCreator[PresentationField] = EchopraxiaService.getInstance.getFieldCreator(classOf[PresentationField])
}
