package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.{Field, FieldBuilderResult, Value}
import com.tersesystems.echopraxia.api.Value.ObjectValue

import scala.annotation.implicitNotFound

trait ValueTypeClasses {

  /**
   * The ToValue trait, used for turning scala things into Value.
   *
   * Most of the time you will define this in your own field builder. For example to define `java.time.Instant` you could do this:
   *
   * {{{
   * trait InstantFieldBuilder extends FieldBuilder {
   *   implicit val instantToStringValue: ToValue[Instant] = (t: Instant) => ToValue(t.toString)
   *   def instant(name: String, i: Instant): Field        = keyValue(name, ToValue(i))
   * }
   * }}}
   *
   * This allows you to define an instant field:
   *
   * {{{
   * logger.info("{}", _.instant("instant", Instant.now()))
   * }}}
   *
   * or if you have a heterogeneous array of values you can import the implicit:
   *
   * {{{
   * logger.info("{}", fb => {
   *   import fb._
   *   fb.array("instants" -> Seq(ToValue(Instant.now()), ToValue("string"))
   * })
   * }}}
   *
   * @tparam T
   *   the object type
   */
  // noinspection ScalaUnusedSymbol
  @implicitNotFound("Could not find an implicit ToValue[${T}]")
  trait ToValue[-T] {
    def toValue(t: T): Value[_]
  }

  object ToValue {
    def apply[T: ToValue](t: T): Value[_] = implicitly[ToValue[T]].toValue(t)

    implicit val valueToValue: ToValue[Value[_]] = identity(_)

    implicit def objectValueToValue[T: ToObjectValue]: ToValue[T] = ToObjectValue[T](_)
    implicit def arrayValueToValue[T: ToArrayValue]: ToValue[T]   = ToArrayValue[T](_)

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

  /**
   * ToArrayValue is used when passing an ArrayValue to a field builder.
   *
   * {{{
   * val array: Array[Int] = Array(1, 2, 3)
   * logger.info("{}", fb => fb.array("array", array)
   * }}}
   *
   * @tparam T
   *   the array type.
   */
  // noinspection ScalaUnusedSymbol
  @implicitNotFound("Could not find an implicit ToArrayValue[${T}]")
  trait ToArrayValue[-T] extends ToValue[T] {
    def toValue(t: T): Value.ArrayValue
  }

  object ToArrayValue {

    def apply[T: ToArrayValue](array: T): Value.ArrayValue =
      implicitly[ToArrayValue[T]].toValue(array)

    implicit val identityArrayValue: ToArrayValue[Value.ArrayValue] = identity(_)

    implicit def iterableToArrayValue[V: ToValue]: ToArrayValue[collection.Iterable[V]] =
      iterable => Value.array(iterable.map(ToValue[V]).toArray: _*)

    implicit def immutableIterableToArrayValue[V: ToValue]: ToArrayValue[collection.immutable.Iterable[V]] =
      iterable => Value.array(iterable.map(ToValue[V]).toArray: _*)

    implicit def arrayToArrayValue[V: ToValue]: ToArrayValue[Array[V]] = array => {
      Value.array(array.map(ToValue[V]): _*)
    }

  }

  /**
   * ToObjectValue is used when providing an explicit `object` value to a field builder. Notable when you have a field or fields in a collection.
   *
   * @tparam T
   *   the object type
   */
  // noinspection ScalaUnusedSymbol
  @implicitNotFound("Could not find an implicit ToObjectValue[${T}]")
  trait ToObjectValue[-T] extends ToValue[T] {
    def toValue(t: T): Value.ObjectValue
  }

  object ToObjectValue {

    def apply[T: ToObjectValue](obj: T): Value.ObjectValue =
      implicitly[ToObjectValue[T]].toValue(obj)

    def apply(fields: Field*): Value.ObjectValue = Value.`object`(fields: _*)

    implicit val identityObjectValue: ToObjectValue[Value.ObjectValue] = identity(_)

    implicit val fieldToObjectValue: ToObjectValue[Field] = f => Value.`object`(f)

    implicit val iterableToObjectValue: ToObjectValue[collection.Iterable[Field]] = t => Value.`object`(t.toArray: _*)

    implicit val immutableIterableToObjectValue: ToObjectValue[collection.immutable.Iterable[Field]] =
      t => Value.`object`(t.toArray: _*)
  }
}

/**
 * This trait resolves options to either the value, or nullValue if `None`.
 */
trait OptionValueTypes { self: ValueTypeClasses =>
  implicit def optionToValue[V: ToValue]: ToValue[Option[V]] = {
    case Some(v) => ToValue(v)
    case None    => Value.nullValue()
  }
  implicit def someToValue[V: ToValue]: ToValue[Some[V]] = v => ToValue(v)
  implicit val noneToValue: ToValue[None.type]           = _ => Value.nullValue()
}

/**
 * This trait resolves `Either` directly to the relevant value.
 */
trait EitherValueTypes { self: ValueTypeClasses =>
  implicit def eitherToValue[L: ToValue, R: ToValue]: ToValue[Either[L, R]] = {
    case Left(left)   => ToValue(left)
    case Right(right) => ToValue(right)
  }
  implicit def leftToValue[L: ToValue, R]: ToValue[Left[L, R]]   = v => ToValue(v.left.get)
  implicit def rightToValue[L, R: ToValue]: ToValue[Right[L, R]] = v => ToValue(v.right.get)
}

trait FieldBuilderResultTypeClasses {

  // if using -T here then all the subtypes of iterable also apply
  trait ToFieldBuilderResult[-T] {
    def toResult(input: T): FieldBuilderResult
  }

  trait LowPriorityToFieldBuilderResult {
    implicit def typeClassConversion[T: ToFieldBuilderResult](input: T): FieldBuilderResult =
      ToFieldBuilderResult[T](input)

    implicit val iterableToFieldBuilderResult: ToFieldBuilderResult[Iterable[Field]] =
      iterable => FieldBuilderResult.list(iterable.toArray)

    implicit val iteratorToFieldBuilderResult: ToFieldBuilderResult[Iterator[Field]] = iterator => {
      import scala.jdk.CollectionConverters._
      FieldBuilderResult.list(iterator.asJava)
    }

    // array doesn't seem to be covered by Iterable
    implicit val arrayToFieldBuilderResult: ToFieldBuilderResult[Array[Field]] = FieldBuilderResult.list(_)
  }

  object ToFieldBuilderResult extends LowPriorityToFieldBuilderResult {
    def apply[T: ToFieldBuilderResult](input: T): FieldBuilderResult =
      implicitly[ToFieldBuilderResult[T]].toResult(input)
  }

}

trait ListToFieldBuilderResultMethods extends FieldBuilderResultTypeClasses {

  def list(fields: Field*): FieldBuilderResult = list(fields)

  def list[T: ToFieldBuilderResult](input: T): FieldBuilderResult = ToFieldBuilderResult[T](input)

}