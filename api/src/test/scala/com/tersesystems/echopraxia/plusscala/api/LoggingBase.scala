package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api._
import com.tersesystems.echopraxia.spi.{EchopraxiaService, FieldConstants, FieldCreator, PresentationHintAttributes}

import scala.collection.JavaConverters._

import scala.language.implicitConversions
import scala.reflect.{ClassTag, classTag}

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
      override val toName: ToName[TF] = ToName.create(name)
      override val toValue: ToValue[TF] = t => valueFunction(t)
    }

    def createFromClass[TF: ClassTag](valueFunction: TF => Value[_]): ToLog[TF] = new ToLog[TF] {
      override val toName: ToName[TF] = ToName.create(classTag[TF].runtimeClass.getName)
      override val toValue: ToValue[TF] = t => valueFunction(t)
    }
  }

  // Allows custom attributes on fields through implicits
  trait ToValueAttribute[-T] {
    def toValue(v: T): Value[_]

    def toAttributes(value: Value[_]): Attributes
  }

  trait LowPriorityToValueAttributeImplicits {
    implicit def optionValueFormat[TV: ToValueAttribute]: ToValueAttribute[Option[TV]] = new ToValueAttribute[Option[TV]] {
      override def toValue(v: Option[TV]): Value[_] = v match {
        case Some(tv) =>
          val ev = implicitly[ToValueAttribute[TV]]
          ev.toValue(tv)
        case None => Value.nullValue()
      }

      override def toAttributes(value: Value[_]): Attributes = implicitly[ToValueAttribute[TV]].toAttributes(value)
    }

    implicit def iterableValueFormat[TV: ToValueAttribute]: ToValueAttribute[Iterable[TV]] = new ToValueAttribute[Iterable[TV]]() {
      override def toValue(seq: collection.Iterable[TV]): Value[_] = {
        val list: Seq[Value[_]] = seq.map(el => implicitly[ToValueAttribute[TV]].toValue(el)).toSeq
        Value.array(list.asJava)
      }

      override def toAttributes(value: Value[_]): Attributes = implicitly[ToValueAttribute[TV]].toAttributes(value)
    }

    implicit def eitherToValueAttribute[TVL: ToValueAttribute, TVR: ToValueAttribute]: ToValueAttribute[Either[TVL, TVR]] = new ToValueAttribute[Either[TVL, TVR]] {
      // This isn't great, but we need to know whether left or right was picked for the attributes
      // and if we have a parameter (either: Either[]) in the method signature then it doesn't
      // pick it up?
      private var optEither: Option[Either[TVL, TVR]] = None

      override def toValue(v: Either[TVL, TVR]): Value[_] = {
        this.optEither = Some(v)
        v match {
          case Left(l) => implicitly[ToValueAttribute[TVL]].toValue(l)
          case Right(r) => implicitly[ToValueAttribute[TVR]].toValue(r)
        }
      }

      override def toAttributes(value: Value[_]): Attributes = {
        // hack hack hack hack
        optEither match {
          case Some(either) =>
            either match {
              case Left(_) =>
                val left = implicitly[ToValueAttribute[TVL]]
                left.toAttributes(value)
              case Right(_) =>
                val right = implicitly[ToValueAttribute[TVR]]
                right.toAttributes(value)
            }
          case None =>
            // should never get here
            Attributes.empty()
        }
      }
    }

    // default low priority implicit that gets applied if nothing is found
    implicit def empty[TV]: ToValueAttribute[TV] = new ToValueAttribute[TV] {
      override def toValue(v: TV): Value[_] = Value.nullValue()
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
