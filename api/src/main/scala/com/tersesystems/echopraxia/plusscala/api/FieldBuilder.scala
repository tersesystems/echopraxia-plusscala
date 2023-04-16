package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.Value.ObjectValue
import com.tersesystems.echopraxia.api._

import scala.annotation.implicitNotFound

trait HasName {
  type Name
}

trait TupleFieldBuilder extends ValueTypeClasses with ListToFieldBuilderResultMethods with HasName {
  def keyValue[V: ToValue](tuple: (Name, V)): Field

  def value[V: ToValue](tuple: (Name, V)): Field

  def exception(tuple: (Name, Throwable)): Field = keyValue(tuple)

  def array[AV: ToArrayValue](tuple: (Name, AV)): Field = keyValue(tuple)

  def obj[OV: ToObjectValue](tuple: (Name, OV)): Field = keyValue(tuple)
}

trait PrimitiveTupleFieldBuilder extends TupleFieldBuilder {

  def string(tuple: (Name, String)): Field = value(tuple)

  def number[N: ToValue: Numeric](tuple: (Name, N)): Field = value(tuple)

  def bool(tuple: (Name, Boolean)): Field = value(tuple)

}

/**
 * A field builder that is enhanced with ToValue, ToObjectValue, and ToArrayValue.
 */
trait ArgsFieldBuilder extends ValueTypeClasses with ListToFieldBuilderResultMethods with HasName {

  // ------------------------------------------------------------------
  // keyValue

  def keyValue[V: ToValue](key: Name, value: V): Field

  // ------------------------------------------------------------------
  // value

  def value[V: ToValue](key: Name, value: V): Field

  // ------------------------------------------------------------------
  // null

  def nullField(name: Name): Field = keyValue(name, Value.nullValue())

  // ------------------------------------------------------------------
  // exception

  def exception(ex: Throwable): Field = Field.value(FieldConstants.EXCEPTION, Value.exception(ex))

  def exception(name: Name, ex: Throwable): Field = keyValue(name, ex)

  // ------------------------------------------------------------------
  // array

  def array[AV: ToArrayValue](name: Name, value: AV): Field =
    keyValue(name, ToArrayValue[AV](value))

  // ------------------------------------------------------------------
  // object

  def obj[OV: ToObjectValue](name: Name, value: OV): Field =
    keyValue(name, ToObjectValue[OV](value))

}

trait PrimitiveArgsFieldBuilder extends ArgsFieldBuilder {

  // ------------------------------------------------------------------
  // string

  def string(name: Name, string: String): Field = value(name, string)

  // ------------------------------------------------------------------
  // number

  def number[N: ToValue: Numeric](name: Name, number: N): Field = value(name, number)

  // ------------------------------------------------------------------
  // boolean

  def bool(name: Name, boolean: Boolean): Field = value(name, boolean)
}

trait StringNameArgsFieldBuilder extends ArgsFieldBuilder with HasName {
  override type Name = String

  override def keyValue[V: ToValue](key: Name, value: V): Field = Field.keyValue(key, ToValue(value))

  override def value[V: ToValue](key: Name, value: V): Field = Field.value(key, ToValue(value))
}

trait StringNameTupleFieldBuilder extends TupleFieldBuilder {
  override type Name = String

  override def keyValue[V: ToValue](tuple: (Name, V)): Field = Field.keyValue(tuple._1, ToValue(tuple._2))

  override def value[V: ToValue](tuple: (Name, V)): Field = Field.value(tuple._1, ToValue(tuple._2))
}

trait FieldBuilder extends StringNameArgsFieldBuilder with StringNameTupleFieldBuilder with PrimitiveTupleFieldBuilder with PrimitiveArgsFieldBuilder

object FieldBuilder extends FieldBuilder
