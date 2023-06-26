package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api._
import com.tersesystems.echopraxia.spi.{DefaultField, FieldConstants}

trait HasName {
  type Name
}

trait HasFieldClass {
  type FieldType <: Field
  protected def fieldClass: Class[_ <: FieldType] // concrete traits have to implement this
}

trait TupleFieldBuilder extends ValueTypeClasses with ListToFieldBuilderResultMethods with HasName with HasFieldClass {
  def keyValue[V: ToValue](tuple: (Name, V)): FieldType

  def value[V: ToValue](tuple: (Name, V)): FieldType

  def exception(tuple: (Name, Throwable)): FieldType = keyValue(tuple)

  def array[AV: ToArrayValue](tuple: (Name, AV)): FieldType = keyValue(tuple)

  def obj[OV: ToObjectValue](tuple: (Name, OV)): FieldType = keyValue(tuple)
}

trait PrimitiveTupleFieldBuilder extends TupleFieldBuilder {
  def string(tuple: (Name, String)): FieldType = keyValue(tuple)

  def number[N: ToValue: Numeric](tuple: (Name, N)): FieldType = keyValue(tuple)

  def bool(tuple: (Name, Boolean)): FieldType = keyValue(tuple)

}

/**
 * A field builder that is enhanced with ToValue, ToObjectValue, and ToArrayValue.
 */
trait ArgsFieldBuilder extends ValueTypeClasses with ListToFieldBuilderResultMethods with HasName with HasFieldClass {
  // ------------------------------------------------------------------
  // keyValue

  def keyValue[V: ToValue](key: Name, value: V): FieldType

  // ------------------------------------------------------------------
  // value

  def value[V: ToValue](key: Name, value: V): FieldType

  // ------------------------------------------------------------------
  // null

  def nullField(name: Name): FieldType = keyValue(name, Value.nullValue())

  // ------------------------------------------------------------------
  // exception

  def exception(name: Name, ex: Throwable): FieldType = keyValue(name, ex)

  def exception(ex: Throwable): FieldType = Field.keyValue(FieldConstants.EXCEPTION, ToValue(ex), fieldClass)

  // ------------------------------------------------------------------
  // array

  def array[AV: ToArrayValue](name: Name, value: AV): FieldType =
    keyValue(name, ToArrayValue[AV](value))

  // ------------------------------------------------------------------
  // object

  def obj[OV: ToObjectValue](name: Name, value: OV): FieldType =
    keyValue(name, ToObjectValue[OV](value))

}

trait PrimitiveArgsFieldBuilder extends ArgsFieldBuilder {

  // ------------------------------------------------------------------
  // string

  def string(name: Name, string: String): FieldType = keyValue(name, string)

  // ------------------------------------------------------------------
  // number

  def number[N: ToValue: Numeric](name: Name, number: N): FieldType = keyValue(name, number)

  // ------------------------------------------------------------------
  // boolean

  def bool(name: Name, boolean: Boolean): FieldType = keyValue(name, boolean)
}

trait StringNameArgsFieldBuilder extends ArgsFieldBuilder with HasName {
  override type Name = String

  override def keyValue[V: ToValue](key: Name, value: V): FieldType = Field.keyValue(key, ToValue(value), fieldClass)

  override def value[V: ToValue](key: Name, value: V): FieldType = Field.value(key, ToValue(value), fieldClass)
}

trait StringNameTupleFieldBuilder extends TupleFieldBuilder {
  override type Name = String

  override def keyValue[V: ToValue](tuple: (Name, V)): FieldType = Field.keyValue(tuple._1, ToValue(tuple._2), fieldClass)

  override def value[V: ToValue](tuple: (Name, V)): FieldType = Field.value(tuple._1, ToValue(tuple._2), fieldClass)

}

/**
 * A field builder that does not define the field type. Use this if you want to extend / replace DefaultField.
 *
 * @tparam F
 *   the field type
 */
trait FieldBuilderBase
    extends StringNameArgsFieldBuilder
    with StringNameTupleFieldBuilder
    with PrimitiveTupleFieldBuilder
    with PrimitiveArgsFieldBuilder

/**
 * A field builder that uses DefaultField explicitly. Use this if you want the default behavior.
 */
trait DefaultFieldBuilder extends FieldBuilderBase {
  override type FieldType = DefaultField
  override protected def fieldClass: Class[DefaultField] = classOf[DefaultField]
}

/**
 * Singleton object for DefaultFieldBuilder.
 */
object DefaultFieldBuilder extends DefaultFieldBuilder

trait FieldBuilder extends FieldBuilderBase {
  override type FieldType = Field
  override protected def fieldClass: Class[Field] = classOf[Field]
}

/**
 * Singleton object for FieldBuilder
 */
object FieldBuilder extends FieldBuilder
