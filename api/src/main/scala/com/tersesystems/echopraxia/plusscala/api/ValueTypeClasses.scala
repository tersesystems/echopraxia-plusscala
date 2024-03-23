package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.{Field, Value}
import com.tersesystems.echopraxia.api.Value.ObjectValue

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

    def apply(fields: Field*): Value.ObjectValue = Value.`object`(fields: _*)
  }
}
