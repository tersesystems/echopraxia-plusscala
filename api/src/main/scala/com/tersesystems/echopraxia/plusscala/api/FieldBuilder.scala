package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api._
import com.tersesystems.echopraxia.spi.{FieldConstants, PresentationHintAttributes}
import com.tersesystems.echopraxia.plusscala.spi.Utils

trait HasName {
  type Name
}

trait HasFieldClass {
  type FieldType <: Field

  protected def fieldClass: Class[_ <: FieldType] // concrete traits have to implement this
}

trait TupleFieldBuilder extends ValueTypeClasses with ListToFieldBuilderResultMethods with HasName with HasFieldClass {
  def keyValue[V: ToValue: ToValueAttributes](tuple: (Name, V)): FieldType

  def value[V: ToValue: ToValueAttributes](tuple: (Name, V)): FieldType

  def exception(tuple: (Name, Throwable)): FieldType = keyValue(tuple)

  def array[AV: ToArrayValue: ToValueAttributes](tuple: (Name, AV)): FieldType = keyValue(tuple)

  def obj[OV: ToObjectValue: ToValueAttributes](tuple: (Name, OV)): FieldType = keyValue(tuple)
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

  def keyValue[V: ToValue: ToValueAttributes](key: Name, value: V): FieldType

  // ------------------------------------------------------------------
  // value

  def value[V: ToValue: ToValueAttributes](key: Name, value: V): FieldType

  // ------------------------------------------------------------------
  // null

  def nullField(name: Name): FieldType = keyValue(name, Value.nullValue())

  // ------------------------------------------------------------------
  // exception

  def exception(name: Name, ex: Throwable): FieldType = keyValue(name, ex)

  def exception(ex: Throwable): FieldType = Field.keyValue(FieldConstants.EXCEPTION, ToValue(ex), fieldClass)

  // ------------------------------------------------------------------
  // array

  def array[AV: ToArrayValue: ToValueAttributes](name: Name, value: AV): FieldType =
    keyValue(name, ToArrayValue[AV](value))

  // ------------------------------------------------------------------
  // object

  def obj[OV: ToObjectValue: ToValueAttributes](name: Name, value: OV): FieldType =
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

  override def keyValue[V: ToValue: ToValueAttributes](key: Name, value: V): FieldType = {
    val ev = implicitly[ToValueAttributes[V]]
    val attributes = ev.toAttributes(ev.toValue(value))
    Utils.newField(key, ToValue(value), attributes, fieldClass)
  }

  override def value[V: ToValue: ToValueAttributes](key: Name, value: V): FieldType =  {
    val ev = implicitly[ToValueAttributes[V]]
    val attributes = ev.toAttributes(ev.toValue(value)).plus(PresentationHintAttributes.asValueOnly())
    Utils.newField(key, ToValue(value), attributes, fieldClass)
  }
}

trait StringNameTupleFieldBuilder extends TupleFieldBuilder {
  override type Name = String

  override def keyValue[V: ToValue: ToValueAttributes](tuple: (Name, V)): FieldType = {
    val ev = implicitly[ToValueAttributes[V]]
    val attributes = ev.toAttributes(ev.toValue(tuple._2))
    Utils.newField(tuple._1, ToValue(tuple._2), attributes, fieldClass)
  }

  override def value[V: ToValue: ToValueAttributes](tuple: (Name, V)): FieldType = {
    val ev = implicitly[ToValueAttributes[V]]
    val attributes = ev.toAttributes(ev.toValue(tuple._2)).plus(PresentationHintAttributes.asValueOnly())
    Utils.newField(tuple._1, ToValue(tuple._2), attributes, fieldClass)
  }
}

trait SourceCodeFieldBuilder extends ValueTypeClasses with SourceCodeImplicits with HasFieldClass {
  def sourceCode(sourceCode: SourceCode): FieldType
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
    with SourceCodeFieldBuilder

/**
 * A field builder that uses PresentationField.
 */
trait PresentationFieldBuilder extends FieldBuilderBase {
  override type FieldType = PresentationField
  override protected def fieldClass: Class[PresentationField] = classOf[PresentationField]

  override def sourceCode(sourceCode: SourceCode): PresentationField = keyValue(ToName(sourceCode), ToValue(sourceCode))
}

/**
 * Singleton object for PresentationFieldBuilder.
 */
object PresentationFieldBuilder extends PresentationFieldBuilder

trait FieldBuilder extends FieldBuilderBase {
  override type FieldType = Field
  override protected def fieldClass: Class[Field] = classOf[Field]

  override def sourceCode(sourceCode: SourceCode): Field = keyValue(ToName(sourceCode), ToValue(sourceCode))
}

/**
 * Singleton object for FieldBuilder
 */
object FieldBuilder extends FieldBuilder
