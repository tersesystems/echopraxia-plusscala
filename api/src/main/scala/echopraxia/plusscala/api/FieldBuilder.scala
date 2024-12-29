package echopraxia.plusscala.api

import echopraxia.api.*

trait KeyValueFieldBuilder { this: ValueTypeClasses & NameTypeClasses =>
  // ------------------------------------------------------------------
  // keyValue

  def keyValue[N: ToName, V: ToValue](name: N, value: V): Field

  def keyValue[N: ToName, V: ToValue](tuple: (N, V)): Field

  def keyValue[F: ToName: ToValue](field: F): Field

  // ------------------------------------------------------------------
  // value

  def value[N: ToName, V: ToValue](name: N, value: V): Field

  def value[N: ToName, V: ToValue](tuple: (N, V)): Field

  def value[F: ToName: ToValue](field: F): Field
}

trait ExceptionFieldBuilder { this: ValueTypeClasses & NameTypeClasses =>
  def exception[N: ToName](tuple: (N, Throwable)): Field
  def exception[N: ToName](name: N, ex: Throwable): Field
  def exception(ex: Throwable): Field
}

/**
 * A field builder that is enhanced with .
 */
trait ArrayObjFieldBuilder { this: ValueTypeClasses & NameTypeClasses =>
  // ------------------------------------------------------------------
  // array

  def array[N: ToName, AV: ToArrayValue](name: N, value: AV): Field
  def array[N: ToName, AV: ToArrayValue](tuple: (N, AV)): Field

  // ------------------------------------------------------------------
  // object

  def obj[N: ToName, OV: ToObjectValue](name: N, value: OV): Field
  def obj[N: ToName, OV: ToObjectValue](tuple: (N, OV)): Field
}

trait PrimitiveFieldBuilder { this: ValueTypeClasses & NameTypeClasses =>
  // ------------------------------------------------------------------
  // string

  def string[N: ToName](name: N, value: String): Field
  def string[N: ToName](tuple: (N, String)): Field

  // ------------------------------------------------------------------
  // number

  def number[N: ToName, NV: ToValue: Numeric](name: N, value: NV): Field
  def number[N: ToName, NV: ToValue: Numeric](tuple: (N, NV)): Field

  // ------------------------------------------------------------------
  // boolean

  def bool[N: ToName](name: N, value: Boolean): Field
  def bool[N: ToName](tuple: (N, Boolean)): Field
}

trait NullFieldBuilder { this: NameTypeClasses =>
  def nullField[N: ToName](name: N): Field
}

trait SourceCodeFieldBuilder {
  def sourceCode(sourceCode: SourceCode): FieldBuilderResult
}

trait ListFieldBuilder { this: FieldBuilderResultTypeClasses =>
  def list[T: ToFieldBuilderResult](input: T): FieldBuilderResult = ToFieldBuilderResult[T](input)

  def list(fields: Field*): FieldBuilderResult = list(fields)
}

/**
 * A field builder that does not define the field type. Use this if you want to extend / replace DefaultField.
 */
trait FieldBuilderBase
    extends EchopraxiaTypeClasses
    with FieldBuilderResultTypeClasses
    with KeyValueFieldBuilder
    with ArrayObjFieldBuilder
    with PrimitiveFieldBuilder
    with ExceptionFieldBuilder
    with SourceCodeFieldBuilder
    with NullFieldBuilder
    with ListFieldBuilder
    with EchopraxiaToValueImplicits
    with EchopraxiaToNameImplicits
    with LowPriorityImplicits {

  override def keyValue[N: ToName, V: ToValue](name: N, value: V): Field = {
    Utils.newField(ToName(name), ToValue(value), Attributes.empty())
  }

  override def keyValue[F: ToName: ToValue](field: F): Field = {
    Utils.newField(ToName(field), ToValue(field), Attributes.empty())
  }

  override def keyValue[N: ToName, V: ToValue](tuple: (N, V)): Field = {
    Utils.newField(ToName(tuple._1), ToValue(tuple._2), Attributes.empty())
  }

  override def value[N: ToName, V: ToValue](name: N, value: V): Field = {
    Utils.newField(ToName(name), ToValue(value), PresentationHintAttributes.valueOnlyAttributes())
  }

  override def value[N: ToName, V: ToValue](tuple: (N, V)): Field = {
    Utils.newField(ToName(tuple._1), ToValue(tuple._2), PresentationHintAttributes.valueOnlyAttributes)
  }

  override def value[F: ToName: ToValue](field: F): Field = {
    Utils.newField(ToName(field), ToValue(field), PresentationHintAttributes.valueOnlyAttributes())
  }

  override def sourceCode(sourceCode: SourceCode): FieldBuilderResult = keyValue(sourceCode, sourceCode)

  override def nullField[N: ToName](name: N): Field = keyValue(name, Value.nullValue())

  override def array[N: ToName, AV: ToArrayValue](name: N, value: AV): Field = keyValue(name, value)

  override def array[N: ToName, AV: ToArrayValue](tuple: (N, AV)): Field = keyValue(tuple)

  override def obj[N: ToName, OV: ToObjectValue](name: N, value: OV): Field = keyValue(name, value)

  override def obj[N: ToName, OV: ToObjectValue](tuple: (N, OV)): Field = keyValue(tuple)

  override def string[N: ToName](name: N, value: String): Field = keyValue(name, value)

  override def string[N: ToName](tuple: (N, String)): Field = keyValue(tuple)

  override def number[N: ToName, NV: ToValue: Numeric](name: N, value: NV): Field = keyValue(name, value)

  override def number[N: ToName, NV: ToValue: Numeric](tuple: (N, NV)): Field = keyValue(tuple)

  override def bool[N: ToName](name: N, value: Boolean): Field = keyValue(name, value)

  override def bool[N: ToName](tuple: (N, Boolean)): Field = keyValue(tuple)

  override def exception[N: ToName](tuple: (N, Throwable)): Field = keyValue(tuple)

  override def exception[N: ToName](name: N, value: Throwable): Field = keyValue(name, value)

  override def exception(ex: Throwable): Field = keyValue(ex, ex)
}

trait FieldBuilder extends FieldBuilderBase

/**
 * Singleton object for FieldBuilder
 */
object FieldBuilder extends FieldBuilder
