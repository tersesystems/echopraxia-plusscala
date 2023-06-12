package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.{DefaultField, Field, FieldConstants, Value}

trait HasName {
  type Name
}

trait HasFieldClass[+F <: Field] {
  protected def fieldClass: Class[_ <: F] // concrete traits have to implement this
}

trait TupleFieldBuilder[+F <: Field] extends ValueTypeClasses with ListToFieldBuilderResultMethods with HasName with HasFieldClass[F] {

  def keyValue[V: ToValue](tuple: (Name, V)): F

  def value[V: ToValue](tuple: (Name, V)): F

  def exception(tuple: (Name, Throwable)): F = keyValue(tuple)

  def array[AV: ToArrayValue](tuple: (Name, AV)): F = keyValue(tuple)

  def obj[OV: ToObjectValue](tuple: (Name, OV)): F = keyValue(tuple)
}

trait PrimitiveTupleFieldBuilder[+F <: Field] extends TupleFieldBuilder[F] {

  def string(tuple: (Name, String)): F = keyValue(tuple)

  def number[N: ToValue: Numeric](tuple: (Name, N)): F = keyValue(tuple)

  def bool(tuple: (Name, Boolean)): F = keyValue(tuple)

}

/**
 * A field builder that is enhanced with ToValue, ToObjectValue, and ToArrayValue.
 */
trait ArgsFieldBuilder[+F <: Field] extends ValueTypeClasses with ListToFieldBuilderResultMethods with HasName with HasFieldClass[F] {
  // ------------------------------------------------------------------
  // keyValue

  def keyValue[V: ToValue](key: Name, value: V): F

  // ------------------------------------------------------------------
  // value

  def value[V: ToValue](key: Name, value: V): F

  // ------------------------------------------------------------------
  // null

  def nullField(name: Name): F = keyValue(name, Value.nullValue())

  // ------------------------------------------------------------------
  // exception

  def exception(name: Name, ex: Throwable): F = keyValue(name, ex)

  def exception(ex: Throwable): F = Field.keyValue(FieldConstants.EXCEPTION, ToValue(ex), fieldClass)

  // ------------------------------------------------------------------
  // array

  def array[AV: ToArrayValue](name: Name, value: AV): F =
    keyValue(name, ToArrayValue[AV](value))

  // ------------------------------------------------------------------
  // object

  def obj[OV: ToObjectValue](name: Name, value: OV): F =
    keyValue(name, ToObjectValue[OV](value))

}

trait PrimitiveArgsFieldBuilder[+F <: Field] extends ArgsFieldBuilder[F] {

  // ------------------------------------------------------------------
  // string

  def string(name: Name, string: String): F = keyValue(name, string)

  // ------------------------------------------------------------------
  // number

  def number[N: ToValue: Numeric](name: Name, number: N): F = keyValue(name, number)

  // ------------------------------------------------------------------
  // boolean

  def bool(name: Name, boolean: Boolean): F = keyValue(name, boolean)
}

trait StringNameArgsFieldBuilder[+F <: Field] extends ArgsFieldBuilder[F] with HasName {
  override type Name = String

  override def keyValue[V: ToValue](key: Name, value: V): F = Field.keyValue(key, ToValue(value), fieldClass)

  override def value[V: ToValue](key: Name, value: V): F = Field.value(key, ToValue(value), fieldClass)
}

trait StringNameTupleFieldBuilder[+F <: Field] extends TupleFieldBuilder[F] {
  override type Name = String

  override def keyValue[V: ToValue](tuple: (Name, V)): F = Field.keyValue(tuple._1, ToValue(tuple._2), fieldClass)

  override def value[V: ToValue](tuple: (Name, V)): F = Field.value(tuple._1, ToValue(tuple._2), fieldClass)

}

/**
 * A field builder that does not define the field type. Use this if you want to extend / replace DefaultField.
 *
 * @tparam F
 *   the field type
 */
trait FieldBuilderBase[+F <: Field]
    extends StringNameArgsFieldBuilder[F]
    with StringNameTupleFieldBuilder[F]
    with PrimitiveTupleFieldBuilder[F]
    with PrimitiveArgsFieldBuilder[F]

/**
 * A field builder that uses DefaultField explicitly. Use this if you want the default behavior.
 */
trait DefaultFieldBuilder extends FieldBuilderBase[DefaultField] {
  override protected def fieldClass: Class[DefaultField] = classOf[DefaultField]
}

/**
 * Singleton object for DefaultFieldBuilder.
 */
object DefaultFieldBuilder extends DefaultFieldBuilder

trait FieldBuilder extends FieldBuilderBase[Field] {
  override protected def fieldClass: Class[Field] = classOf[Field]
}

/**
 * Singleton object for FieldBuilder
 */
object FieldBuilder extends FieldBuilder
