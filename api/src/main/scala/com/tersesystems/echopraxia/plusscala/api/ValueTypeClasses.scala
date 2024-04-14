package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.Value.ObjectValue
import com.tersesystems.echopraxia.api._
import com.tersesystems.echopraxia.spi.PresentationHintAttributes

import scala.annotation.implicitNotFound
import scala.collection.JavaConverters._

import com.tersesystems.echopraxia.api.Attribute
import com.tersesystems.echopraxia.spi.PresentationHintAttributes

import java.lang

import scala.annotation.implicitNotFound

trait ValueTypeClasses {

  /**
   * The ToValue trait, used for turning scala things into Value.
   *
   * @tparam T
   *   the object type
   */
  // noinspection ScalaUnusedSymbol
  @implicitNotFound("Could not find an implicit ToValue[${T}]")
  trait ToValue[-T] {
    def toValue(t: T): Value[_]
  }

  trait ToValueImplicits {
    implicit val valueToValue: ToValue[Value[_]] = identity(_)

    implicit def objectValueToValue[T: ToObjectValue]: ToValue[T] = implicitly[ToObjectValue[T]].toValue(_)
    implicit def arrayValueToValue[T: ToArrayValue]: ToValue[T]   = implicitly[ToArrayValue[T]].toValue(_)

    implicit val stringToStringValue: ToValue[String] = (s: String) => Value.string(s)

    implicit val byteToValue: ToValue[Byte]             = (byte: Byte) => Value.number(byte: java.lang.Byte)
    implicit val shortToValue: ToValue[Short]           = (short: Short) => Value.number(short: java.lang.Short)
    implicit val intToValue: ToValue[Int]               = (int: Int) => Value.number(int: java.lang.Integer)
    implicit val longToValue: ToValue[Long]             = (long: Long) => Value.number(long: java.lang.Long)
    implicit val doubleToValue: ToValue[Double]         = (double: Double) => Value.number(double: java.lang.Double)
    implicit val floatToValue: ToValue[Float]           = (float: Float) => Value.number(float: java.lang.Float)
    implicit val bigIntToValue: ToValue[BigInt]         = (bigInt: BigInt) => Value.number(bigInt.bigInteger)
    implicit val bigDecimalToValue: ToValue[BigDecimal] = (bigDec: BigDecimal) => Value.number(bigDec.bigDecimal)

    implicit val javaByteToValue: ToValue[java.lang.Byte]             = (byte: java.lang.Byte) => Value.number(byte)
    implicit val javaShortToValue: ToValue[java.lang.Short]           = (short: java.lang.Short) => Value.number(short)
    implicit val javaIntegerToValue: ToValue[java.lang.Integer]       = (int: java.lang.Integer) => Value.number(int)
    implicit val javaLongToValue: ToValue[java.lang.Long]             = (long: java.lang.Long) => Value.number(long)
    implicit val javaDoubleToValue: ToValue[java.lang.Double]         = (double: java.lang.Double) => Value.number(double)
    implicit val javaFloatToValue: ToValue[java.lang.Float]           = (float: java.lang.Float) => Value.number(float)
    implicit val javaBigIntegerToValue: ToValue[java.math.BigInteger] = (bigInt: java.math.BigInteger) => Value.number(bigInt)
    implicit val javaBigDecimalToValue: ToValue[java.math.BigDecimal] = (bigDec: java.math.BigDecimal) => Value.number(bigDec)

    implicit val booleanToBoolValue: ToValue[Boolean] = bool => Value.bool(bool)

    implicit val javaBoolToBoolValue: ToValue[java.lang.Boolean] = bool => Value.bool(bool)

    implicit val unitToValue: ToValue[Unit] = _ => ObjectValue.EMPTY

    // Allowing fb.nullValue as fb.keyValue("foo" -> null) is dangerous.
    // use fb.nullField("foo") instead.
    // implicit val nullToValue: ToValue[Nothing] = bool => Value.nullValue()

    implicit val throwableToValue: ToValue[Throwable] = e => Value.exception(e)
  }

  object ToValue extends ToValueImplicits {
    def apply[T: ToValue](t: T): Value[_] = implicitly[ToValue[T]].toValue(t)
  }

  /**
   * ToArrayValue is used when passing an ArrayValue to a field builder.
   *
   * @tparam T
   *   the array type.
   */
  // noinspection ScalaUnusedSymbol
  @implicitNotFound("Could not find an implicit ToArrayValue[${T}]")
  trait ToArrayValue[-T] extends ToValue[T] {
    def toValue(t: T): Value.ArrayValue
  }

  trait ToArrayValueImplicits {
    implicit val identityArrayValue: ToArrayValue[Value.ArrayValue] = identity(_)

    implicit def iterableToArrayValue[V: ToValue]: ToArrayValue[collection.Iterable[V]] =
      iterable => Value.array(iterable.map(implicitly[ToValue[V]].toValue).toArray: _*)

    implicit def immutableIterableToArrayValue[V: ToValue]: ToArrayValue[collection.immutable.Iterable[V]] =
      iterable => Value.array(iterable.map(implicitly[ToValue[V]].toValue).toArray: _*)

    implicit def arrayToArrayValue[V: ToValue]: ToArrayValue[Array[V]] = array => {
      Value.array(array.map(implicitly[ToValue[V]].toValue): _*)
    }
  }

  object ToArrayValue extends ToArrayValueImplicits {
    def apply[T: ToArrayValue](array: T): Value.ArrayValue =
      implicitly[ToArrayValue[T]].toValue(array)
  }

  /**
   * ToObjectValue is used when providing an explicit `object` value to a field builder.
   *
   * Notable when you have a field or fields in a collection.
   *
   * @tparam T
   *   the object type
   */
  // noinspection ScalaUnusedSymbol
  @implicitNotFound("Could not find an implicit ToObjectValue[${T}]")
  trait ToObjectValue[-T] extends ToValue[T] {
    def toValue(t: T): Value.ObjectValue
  }

  trait ToObjectValueImplicits {
    implicit val identityObjectValue: ToObjectValue[Value.ObjectValue] = identity(_)

    implicit val fieldToObjectValue: ToObjectValue[Field] = f => Value.`object`(f)

    implicit val iterableToObjectValue: ToObjectValue[collection.Iterable[Field]] = t => Value.`object`(t.toArray: _*)

    implicit val immutableIterableToObjectValue: ToObjectValue[collection.immutable.Iterable[Field]] =
      t => Value.`object`(t.toArray: _*)
  }

  object ToObjectValue extends ToObjectValueImplicits {

    def apply[T: ToObjectValue](obj: T): Value.ObjectValue =
      implicitly[ToObjectValue[T]].toValue(obj)

    def apply(field: Field): Value.ObjectValue = Value.`object`(field)

    def apply(fields: Field*): Value.ObjectValue = Value.`object`(fields: _*)
  }

  // Allows custom attributes on fields through implicits
  trait ToValueAttributes[-T] {
    // This is awkward, but we need to convert toValue when we have Iterable[TV]
    def toValue(v: T): Value[_]
    def toAttributes(value: Value[_]): Attributes
  }

  trait LowPriorityToValueAttributesImplicits {
    // default low priority implicit that gets applied if nothing is found
    implicit def empty[TV]: ToValueAttributes[TV] = new ToValueAttributes[TV] {
      override def toValue(v: TV): Value[_]                  = Value.nullValue()
      override def toAttributes(value: Value[_]): Attributes = Attributes.empty()
    }

    implicit def iterableValueFormat[TV: ToValueAttributes]: ToValueAttributes[Iterable[TV]] = new ToValueAttributes[Iterable[TV]]() {
      override def toValue(seq: collection.Iterable[TV]): Value[_] = {
        val list: Seq[Value[_]] = seq.map(el => implicitly[ToValueAttributes[TV]].toValue(el)).toSeq
        Value.array(list.asJava)
      }

      override def toAttributes(value: Value[_]): Attributes = implicitly[ToValueAttributes[TV]].toAttributes(value)
    }
  }

  object ToValueAttributes extends LowPriorityToValueAttributesImplicits {
    def attributes(value: Value[_], ev: ToValueAttributes[_]): Attributes = ev.toAttributes(value)
  }

  /**
   * This changes how the field renders in line format, useful for rendering complex objects with a summary.
   *
   * {{{
   * class Foo {
   *   def debugString: String = ...
   * }
   * implicit val fooToDebugString: ToStringValue[Foo] = foo => ToValue(foo.debugString)
   * }}}
   *
   * @tparam T
   *   the type.
   */
  trait ToStringValue[-T] extends ToValueAttributes[T] {
    override def toAttributes(value: Value[_]): Attributes = Attributes.create(ToStringValue.withToStringValue(value))
  }

  object ToStringValue {
    // Add a custom string value to render
    def withToStringValue(value: Value[_]): Attribute[_] = {
      PresentationHintAttributes.withToStringValue(value.toString())
    }
  }

  /**
   * This changes the display name in line format, useful for human representation.
   *
   * Only applicable to fields.
   *
   * @tparam T
   *   the type param
   */
  trait WithDisplayName[-T] extends ToValueAttributes[T] {
    def displayName: String
    override def toValue(v: T): Value[_]                   = Value.nullValue()
    override def toAttributes(value: Value[_]): Attributes = Attributes.create(PresentationHintAttributes.withDisplayName(displayName))
  }

  object WithDisplayName {
    def apply[T](name: String): WithDisplayName[T] = new WithDisplayName[T]() {
      val displayName: String = name
    }
  }

  /**
   * This abbreviates a string or Seq to abbreviate after the leading elements line format.
   *
   * @tparam T
   *   the type param
   */
  trait AbbreviateAfter[-T] extends ToValueAttributes[T] {
    def after: Int
    override def toValue(v: T): Value[_]                   = Value.nullValue()
    override def toAttributes(value: Value[_]): Attributes = Attributes.create(PresentationHintAttributes.abbreviateAfter(after))
  }

  object AbbreviateAfter {
    def apply[T](a: Int): AbbreviateAfter[T] = new AbbreviateAfter[T]() {
      override val after: Int = a
    }
  }

  /**
   * This elides (does not display) the given field in line format.
   *
   * @tparam T
   *   the type param
   */
  final class Elided[-T] extends ToValueAttributes[T] {
    override def toValue(v: T): Value[_]                   = Value.nullValue()
    override def toAttributes(value: Value[_]): Attributes = Elided.attributes
  }

  object Elided {
    val attributes: Attributes = Attributes.create(PresentationHintAttributes.asElided())
    def apply[T]: Elided[T]    = new Elided[T]()
  }

  /**
   * This class presents the field as "value only" in line format, without the key.
   *
   * Only applicable to fields.
   *
   * @tparam T
   *   the type param
   */
  final class AsValueOnly[-T] extends ToValueAttributes[T] {
    override def toValue(v: T): Value[_]                   = Value.nullValue()
    override def toAttributes(value: Value[_]): Attributes = AsValueOnly.attributes
  }

  object AsValueOnly {
    val attributes: Attributes = Attributes.create(PresentationHintAttributes.asValueOnly())

    def apply[T]: AsValueOnly[T] = new AsValueOnly[T]()
  }

  /**
   * This presents a string or seq as a cardinal in line format, showing the number of elements.
   *
   * @tparam T
   *   the type param
   */
  final class AsCardinal[-T] extends ToValueAttributes[T] {
    override def toValue(v: T): Value[_]                   = Value.nullValue()
    override def toAttributes(value: Value[_]): Attributes = AsCardinal.attributes
  }

  object AsCardinal {
    val attributes: Attributes  = Attributes.create(PresentationHintAttributes.asCardinal())
    def apply[T]: AsCardinal[T] = new AsCardinal[T]()
  }
}
