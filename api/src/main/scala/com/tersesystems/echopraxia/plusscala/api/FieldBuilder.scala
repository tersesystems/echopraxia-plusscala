package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api._
import com.tersesystems.echopraxia.plusscala.spi.Utils
import com.tersesystems.echopraxia.spi.PresentationHintAttributes

trait HasFieldClass {
  type FieldType <: Field

  protected def fieldClass: Class[_ <: FieldType] // concrete traits have to implement this
}

trait TupleFieldBuilder extends ValueTypeClasses with NameTypeClass with ListToFieldBuilderResultMethods with HasFieldClass {
  def keyValue[V: ToValue](tuple: (String, V)): FieldType

  def value[V: ToValue](tuple: (String, V)): FieldType

  def exception(tuple: (String, Throwable)): FieldType = keyValue(tuple)

  def array[AV: ToArrayValue](tuple: (String, AV)): FieldType = keyValue(tuple)

  def obj[OV: ToObjectValue](tuple: (String, OV)): FieldType = keyValue(tuple)
}

trait PrimitiveTupleFieldBuilder extends TupleFieldBuilder {
  def string(tuple: (String, String)): FieldType = keyValue(tuple)

  def number[N: ToValue: Numeric](tuple: (String, N)): FieldType = keyValue(tuple)

  def bool(tuple: (String, Boolean)): FieldType = keyValue(tuple)

}

/**
 * A field builder that is enhanced with ToValue, ToObjectValue, and ToArrayValue.
 */
trait ArgsFieldBuilder extends ValueTypeClasses with NameTypeClass with ListToFieldBuilderResultMethods with HasFieldClass {
  // ------------------------------------------------------------------
  // keyValue

  def keyValue[V: ToValue](name: String, value: V): FieldType

  // ------------------------------------------------------------------
  // value

  def value[V: ToValue](name: String, value: V): FieldType

  // ------------------------------------------------------------------
  // null

  def nullField(name: String): FieldType = keyValue(name, Value.nullValue())

  // ------------------------------------------------------------------
  // exception

  def exception(name: String, ex: Throwable): FieldType = keyValue(name, ex)

  def exception(ex: Throwable): FieldType = Field.keyValue(ToName(ex), ToValue(ex), fieldClass)

  // ------------------------------------------------------------------
  // array

  def array[AV: ToArrayValue](name: String, value: AV): FieldType = keyValue(name, value)

  // ------------------------------------------------------------------
  // object

  def obj[OV: ToObjectValue](name: String, value: OV): FieldType = keyValue(name, value)

}

trait PrimitiveArgsFieldBuilder extends ArgsFieldBuilder {

  // ------------------------------------------------------------------
  // string

  def string(name: String, string: String): FieldType = keyValue(name, string)

  // ------------------------------------------------------------------
  // number

  def number[N: ToValue: Numeric](name: String, number: N): FieldType = keyValue(name, number)

  // ------------------------------------------------------------------
  // boolean

  def bool(name: String, boolean: Boolean): FieldType = keyValue(name, boolean)
}

trait SourceCodeFieldBuilder extends HasFieldClass {
  def sourceCode(sourceCode: SourceCode): FieldType
}

/**
 * A field builder that does not define the field type. Use this if you want to extend / replace DefaultField.
 */
trait FieldBuilderBase
    extends ArgsFieldBuilder
    with TupleFieldBuilder
    with PrimitiveTupleFieldBuilder
    with PrimitiveArgsFieldBuilder
    with SourceCodeFieldBuilder {

  override def keyValue[V: ToValue](name: String, value: V): FieldType = {
    Utils.newField(name, ToValue(value), Attributes.empty(), fieldClass)
  }

  override def value[V: ToValue](name: String, value: V): FieldType = {
    Utils.newField(name, ToValue(value), PresentationHintAttributes.valueOnlyAttributes(), fieldClass)
  }

  override def keyValue[V: ToValue](tuple: (String, V)): FieldType = {
    Utils.newField(tuple._1, ToValue(tuple._2), Attributes.empty(), fieldClass)
  }

  override def value[V: ToValue](tuple: (String, V)): FieldType = {
    Utils.newField(tuple._1, ToValue(tuple._2), PresentationHintAttributes.valueOnlyAttributes, fieldClass)
  }

  override def sourceCode(sourceCode: SourceCode): FieldType = {
    keyValue(ToName(sourceCode), ToValue(sourceCode))
  }
}

/**
 * A field builder that uses PresentationField.
 */
trait PresentationFieldBuilder extends FieldBuilderBase {
  override type FieldType = PresentationField
  override protected def fieldClass: Class[PresentationField] = classOf[PresentationField]
}

/**
 * Singleton object for PresentationFieldBuilder.
 */
object PresentationFieldBuilder extends PresentationFieldBuilder

trait FieldBuilder extends FieldBuilderBase {
  override type FieldType = Field
  override protected def fieldClass: Class[Field] = classOf[Field]
}

/**
 * Singleton object for FieldBuilder
 */
object FieldBuilder extends FieldBuilder
