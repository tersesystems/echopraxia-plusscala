package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api._
import com.tersesystems.echopraxia.plusscala.spi.Utils
import com.tersesystems.echopraxia.spi.PresentationHintAttributes

trait HasFieldClass {
  type FieldType <: Field

  protected def fieldClass: Class[_ <: FieldType]
}

trait KeyValueFieldBuilder extends ValueTypeClasses with NameTypeClass with HasFieldClass {
  // ------------------------------------------------------------------
  // keyValue

  def keyValue[N: ToName, V: ToValue](name: N, value: V): FieldType
  def keyValue[N: ToName, V: ToValue](tuple: (N, V)): FieldType

  // ------------------------------------------------------------------
  // value

  def value[N: ToName, V: ToValue](name: N, value: V): FieldType
  def value[N: ToName, V: ToValue](tuple: (N, V)): FieldType
}

trait ExceptionFieldBuilder extends ValueTypeClasses with NameTypeClass with HasFieldClass {
  def exception[N: ToName](tuple: (N, Throwable)): FieldType
  def exception[N: ToName](name: N, ex: Throwable): FieldType
  def exception(ex: Throwable): FieldType
}

/**
 * A field builder that is enhanced with .
 */
trait ArrayObjFieldBuilder extends ValueTypeClasses with NameTypeClass with HasFieldClass {
  // ------------------------------------------------------------------
  // array

  def array[N: ToName, AV: ToArrayValue](name: N, value: AV): FieldType
  def array[N: ToName, AV: ToArrayValue](tuple: (N, AV)): FieldType

  // ------------------------------------------------------------------
  // object

  def obj[N: ToName, OV: ToObjectValue](name: N, value: OV): FieldType
  def obj[N: ToName, OV: ToObjectValue](tuple: (N, OV)): FieldType
}

trait PrimitiveFieldBuilder extends ValueTypeClasses with NameTypeClass with HasFieldClass {
  // ------------------------------------------------------------------
  // string

  def string[N: ToName](name: N, value: String): FieldType
  def string[N: ToName](tuple: (N, String)): FieldType

  // ------------------------------------------------------------------
  // number

  def number[N: ToName, NV: ToValue: Numeric](name: N, value: NV): FieldType
  def number[N: ToName, NV: ToValue: Numeric](tuple: (N, NV)): FieldType

  // ------------------------------------------------------------------
  // boolean

  def bool[N: ToName](name: N, value: Boolean): FieldType
  def bool[N: ToName](tuple: (N, Boolean)): FieldType
}

trait NullFieldBuilder extends ValueTypeClasses with NameTypeClass with HasFieldClass {
  def nullField[N: ToName](name: N): FieldType
}

trait SourceCodeFieldBuilder {
  def sourceCode(sourceCode: SourceCode): FieldBuilderResult
}

trait ListFieldBuilder extends FieldBuilderResultTypeClasses {
  def list[T: ToFieldBuilderResult](input: T): FieldBuilderResult = ToFieldBuilderResult[T](input)

  def list(fields: Field*): FieldBuilderResult = list(fields)
}

/**
 * A field builder that does not define the field type. Use this if you want to extend / replace DefaultField.
 */
trait FieldBuilderBase
    extends KeyValueFieldBuilder
    with ArrayObjFieldBuilder
    with PrimitiveFieldBuilder
    with ExceptionFieldBuilder
    with SourceCodeFieldBuilder
    with NullFieldBuilder
    with ListFieldBuilder {

  override def keyValue[N: ToName, V: ToValue](name: N, value: V): FieldType = {
    Utils.newField(ToName(name), ToValue(value), Attributes.empty(), fieldClass)
  }

  override def value[N: ToName, V: ToValue](name: N, value: V): FieldType = {
    Utils.newField(ToName(name), ToValue(value), PresentationHintAttributes.valueOnlyAttributes(), fieldClass)
  }

  override def keyValue[N: ToName, V: ToValue](tuple: (N, V)): FieldType = {
    Utils.newField(ToName(tuple._1), ToValue(tuple._2), Attributes.empty(), fieldClass)
  }

  override def value[N: ToName, V: ToValue](tuple: (N, V)): FieldType = {
    Utils.newField(ToName(tuple._1), ToValue(tuple._2), PresentationHintAttributes.valueOnlyAttributes, fieldClass)
  }

  override def sourceCode(sourceCode: SourceCode): FieldBuilderResult = keyValue(sourceCode, sourceCode)

  override def nullField[N: ToName](name: N): FieldType = keyValue(name, Value.nullValue())

  override def array[N: ToName, AV: ToArrayValue](name: N, value: AV): FieldType = keyValue(name, value)

  override def array[N: ToName, AV: ToArrayValue](tuple: (N, AV)): FieldType = keyValue(tuple)

  override def obj[N: ToName, OV: ToObjectValue](name: N, value: OV): FieldType = keyValue(name, value)

  override def obj[N: ToName, OV: ToObjectValue](tuple: (N, OV)): FieldType = keyValue(tuple)

  override def string[N: ToName](name: N, value: String): FieldType = keyValue(name, value)

  override def string[N: ToName](tuple: (N, String)): FieldType = keyValue(tuple)

  override def number[N: ToName, NV: ToValue: Numeric](name: N, value: NV): FieldType = keyValue(name, value)

  override def number[N: ToName, NV: ToValue: Numeric](tuple: (N, NV)): FieldType = keyValue(tuple)

  override def bool[N: ToName](name: N, value: Boolean): FieldType = keyValue(name, value)

  override def bool[N: ToName](tuple: (N, Boolean)): FieldType = keyValue(tuple)

  override def exception[N: ToName](tuple: (N, Throwable)): FieldType = keyValue(tuple)

  override def exception[N: ToName](name: N, value: Throwable): FieldType = keyValue(name, value)

  override def exception(ex: Throwable): FieldType = keyValue(ex, ex)
}

/**
 * A field builder that uses PresentationField.
 */
trait PresentationFieldBuilder extends FieldBuilderBase with StringToNameImplicits {
  override type FieldType = PresentationField
  override def fieldClass: Class[FieldType] = classOf[FieldType]
}

/**
 * Singleton object for PresentationFieldBuilder.
 */
object PresentationFieldBuilder extends PresentationFieldBuilder

trait FieldBuilder extends FieldBuilderBase with StringToNameImplicits {
  override type FieldType = Field
  override def fieldClass: Class[FieldType] = classOf[FieldType]
}

/**
 * Singleton object for FieldBuilder
 */
object FieldBuilder extends FieldBuilder
