package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api._

import scala.annotation.implicitNotFound

trait ValueTypeClasses {

  /**
   * The ToValue trait, used for turning scala things into Value.
   *
   * Most of the time you will define this in your own field builder.
   * For example to define `java.time.Instant` you could do this:
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
   * @tparam T the object type
   */
  // noinspection ScalaUnusedSymbol
  @implicitNotFound("Could not find an implicit ToValue[${T}]")
  trait ToValue[-T] {
    def toValue(t: T): Value[_]
  }

  object ToValue {
    def apply[T: ToValue](t: T): Value[_] = implicitly[ToValue[T]].toValue(t)

    implicit val valueToValue: ToValue[Value[_]] = identity

    implicit def objectValueToValue[T: ToObjectValue]: ToValue[T] = ToObjectValue[T]
    implicit def arrayValueToValue[T: ToArrayValue]: ToValue[T]   = ToArrayValue[T]

    implicit val stringToStringValue: ToValue[String] = (s: String) => Value.string(s)

    implicit val byteToValue: ToValue[Byte]             = (byte: Byte) => Value.number(byte: java.lang.Byte)
    implicit val shortToValue: ToValue[Short]           = (short: Short) => Value.number(short: java.lang.Short)
    implicit val intToValue: ToValue[Int]               = (int: Int) => Value.number(int: java.lang.Integer)
    implicit val longToValue: ToValue[Long]             = (long: Long) => Value.number(long: java.lang.Long)
    implicit val doubleToValue: ToValue[Double]         = (double: Double) => Value.number(double: java.lang.Double)
    implicit val floatToValue: ToValue[Float]           = (float: Float) => Value.number(float: java.lang.Float)
    implicit val bigIntToValue: ToValue[BigInt]         = (bigInt: BigInt) => Value.number(bigInt.bigInteger)
    implicit val bigDecimalToValue: ToValue[BigDecimal] = (bigDec: BigDecimal) => Value.number(bigDec.bigDecimal)

    implicit val booleanToBoolValue: ToValue[Boolean]            = bool => Value.bool(bool)
    implicit val javaBoolToBoolValue: ToValue[java.lang.Boolean] = bool => Value.bool(bool)

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
   * @tparam T the array type.
   */
  // noinspection ScalaUnusedSymbol
  @implicitNotFound("Could not find an implicit ToArrayValue[${T}]")
  trait ToArrayValue[-T] {
    def toArrayValue(t: T): Value.ArrayValue
  }

  object ToArrayValue {

    def apply[T: ToArrayValue](array: T): Value.ArrayValue =
      implicitly[ToArrayValue[T]].toArrayValue(array)

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
   * ToObjectValue is used when providing an explicit `object` value to
   * a field builder.  Notable when you have a field or fields in a collection.
   *
   * @tparam T the object type
   */
  // noinspection ScalaUnusedSymbol
  @implicitNotFound("Could not find an implicit ToObjectValue[${T}]")
  trait ToObjectValue[-T] {
    def toObjectValue(t: T): Value.ObjectValue
  }

  object ToObjectValue {

    def apply[T: ToObjectValue](obj: T): Value.ObjectValue =
      implicitly[ToObjectValue[T]].toObjectValue(obj)

    def apply(fields: Field*): Value.ObjectValue = Value.`object`(fields: _*)

    implicit val identityObjectValue: ToObjectValue[Value.ObjectValue] = identity(_)

    implicit val fieldToObjectValue: ToObjectValue[Field] = f => Value.`object`(f)

    implicit val iterableToObjectValue: ToObjectValue[collection.Iterable[Field]] = t => Value.`object`(t.toArray: _*)

    implicit val immutableIterableToObjectValue: ToObjectValue[collection.immutable.Iterable[Field]] =
      t => Value.`object`(t.toArray: _*)
  }

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
      new ToFieldBuilderResult[Iterable[Field]] {
        override def toResult(iterable: Iterable[Field]): FieldBuilderResult =
          FieldBuilderResult.list(iterable.toArray)
      }

    implicit val iteratorToFieldBuilderResult: ToFieldBuilderResult[Iterator[Field]] =
      new ToFieldBuilderResult[Iterator[Field]] {
        import scala.jdk.CollectionConverters._
        override def toResult(iterator: Iterator[Field]): FieldBuilderResult =
          FieldBuilderResult.list(iterator.asJava)
      }

    // array doesn't seem to be covered by Iterable
    implicit val arrayToFieldBuilderResult: ToFieldBuilderResult[Array[Field]] =
      new ToFieldBuilderResult[Array[Field]] {
        override def toResult(array: Array[Field]): FieldBuilderResult =
          FieldBuilderResult.list(array)
      }
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

trait TupleFieldBuilder extends ValueTypeClasses with ListToFieldBuilderResultMethods {
  protected def keyValue[V: ToValue](tuple: (String, V)): Field = Field.keyValue(tuple._1, ToValue(tuple._2))

  protected def value[V: ToValue](tuple: (String, V)): Field = Field.value(tuple._1, ToValue(tuple._2))

  def string(tuple: (String, String)): Field = value(tuple)

  def number[N: ToValue: Numeric](tuple: (String, N)): Field = value(tuple)

  def bool(tuple: (String, Boolean)): Field = value(tuple)

  def exception(tuple: (String, Throwable)): Field = keyValue(tuple)

  def array[AV: ToArrayValue](tuple: (String, AV)): Field = keyValue(tuple)

  def obj[OV: ToObjectValue](tuple: (String, OV)): Field = keyValue(tuple)
}

/**
 * A field builder that is enhanced with ToValue, ToObjectValue, and ToArrayValue.
 */
trait ArgsFieldBuilder extends ValueTypeClasses with ListToFieldBuilderResultMethods {

  // ------------------------------------------------------------------
  // keyValue

  protected def keyValue[V: ToValue](key: String, value: V): Field = Field.keyValue(key, ToValue(value))

  // ------------------------------------------------------------------
  // value

  protected def value[V: ToValue](key: String, value: V): Field = Field.value(key, ToValue(value))

  // ------------------------------------------------------------------
  // string

  def string(name: String, string: String): Field = value(name, string)

  // ------------------------------------------------------------------
  // number

  def number(name: String, number: Byte): Field = value(name, number)

  def number(name: String, number: Short): Field = value(name, number)

  def number(name: String, number: Int): Field = value(name, number)

  def number(name: String, number: Long): Field = value(name, number)

  def number(name: String, number: Float): Field = value(name, number)

  def number(name: String, number: Double): Field = value(name, number)

  def number(name: String, number: BigDecimal): Field = value(name, number)

  def number(name: String, number: BigInt): Field = value(name, number)

  // ------------------------------------------------------------------
  // boolean

  def bool(name: String, boolean: Boolean): Field = value(name, boolean)

  // ------------------------------------------------------------------
  // null

  def nullField(name: String): Field = keyValue(name, Value.nullValue())

  // ------------------------------------------------------------------
  // exception

  def exception(ex: Throwable): Field               = value(FieldConstants.EXCEPTION, ex)
  def exception(name: String, ex: Throwable): Field = keyValue(name, ex)

  // ------------------------------------------------------------------
  // array

  def array[AV: ToArrayValue](name: String, value: AV): Field =
    keyValue(name, ToArrayValue[AV](value))

  // ------------------------------------------------------------------
  // object

  def obj[OV: ToObjectValue](name: String, value: OV): Field =
    keyValue(name, ToObjectValue[OV](value))

}

trait FieldBuilder extends TupleFieldBuilder with ArgsFieldBuilder

object FieldBuilder extends FieldBuilder
