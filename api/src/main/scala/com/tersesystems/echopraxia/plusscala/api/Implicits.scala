package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.{
  Field,
  Value,
  Condition => JCondition,
  FieldBuilderResult => JFieldBuilderResult,
  Level => JLevel,
  LoggingContext => JLoggingContext
}

import java.util
import java.util.stream
import java.util.stream.Collectors

trait LowPriorityImplicits {

  final implicit class RichCondition(javaCondition: JCondition) {
    @inline
    def asScala: Condition = Condition((level, context) => javaCondition.test(level.asJava, context.asJava))
  }

  final implicit class RichLoggingContext(context: JLoggingContext) {
    @inline
    def asScala: LoggingContext = new ScalaLoggingContext(context)
  }

  final implicit class RichLevel(level: JLevel) {
    @inline
    def asScala: Level = Level.asScala(level)
  }

  final implicit class RichValue[A](value: Value[A]) {

    def asObject: Value.ObjectValue = value.asInstanceOf[Value.ObjectValue]

    def asArray: Value.ArrayValue     = value.asInstanceOf[Value.ArrayValue]
    def asBoolean: Value.BooleanValue = value.asInstanceOf[Value.BooleanValue]
    def asString: Value.StringValue   = value.asInstanceOf[Value.StringValue]

    def asNumber[N <: Number with Comparable[N]: Numeric]: Value.NumberValue[N] = value.asInstanceOf[Value.NumberValue[N]]
  }

  final implicit class RichArrayValue(values: Value[util.List[Value[_]]]) {
    def add(value: Value[_]): Value[util.List[Value[_]]] = {
      val joinedFields = new util.ArrayList(values.raw())
      joinedFields.add(value)
      Value.array(joinedFields)
    }

    def +(value: Value[_]): Value[util.List[Value[_]]] = add(value)

    def append(newValues: Traversable[Value[_]]): Value[util.List[Value[_]]] = {
      val joinedValues = new util.ArrayList(values.raw())
      for (f <- newValues) {
        joinedValues.add(f)
      }
      Value.array(joinedValues)
    }

    def ++(values: Traversable[Value[_]]): Value[util.List[Value[_]]] = append(values)

    def append(newValues: util.Collection[Value[_]]): Value[util.List[Value[_]]] = {
      val joinedValues = new util.ArrayList(values.raw)
      joinedValues.addAll(newValues)
      Value.array(joinedValues)
    }

    def ++(values: util.Collection[Value[_]]): Value[util.List[Value[_]]] = append(values)
  }

  final implicit class RichObjectValue(value: Value[util.List[Field]]) {
    def add(field: Field): Value[util.List[Field]] = {
      val joinedFields = new util.ArrayList(value.raw)
      joinedFields.add(field)
      Value.`object`(joinedFields)
    }

    def +(field: Field): Value[util.List[Field]] = add(field)

    def append(fields: Traversable[Field]): Value[util.List[Field]] = {
      val joinedFields = new util.ArrayList(value.raw)
      for (f <- fields) {
        joinedFields.add(f)
      }
      Value.`object`(joinedFields)
    }

    def ++(fields: Traversable[Field]): Value[util.List[Field]] = append(fields)

    def append(fields: util.Collection[Field]): Value[util.List[Field]] = {
      val joinedFields = new util.ArrayList(value.raw)
      joinedFields.addAll(fields)
      Value.`object`(joinedFields)
    }

    def ++(fields: util.Collection[Field]): Value[util.List[Field]] = append(fields)
  }

  final implicit class RichFieldBuilderResult(result: JFieldBuilderResult) {
    @inline
    def concat(other: JFieldBuilderResult): JFieldBuilderResult = { () =>
      stream.Stream.concat(result.fields().stream(), other.fields().stream()).collect(Collectors.toList())
    }

    @inline
    def ++(other: JFieldBuilderResult): JFieldBuilderResult = concat(other)
  }

}

object Implicits extends LowPriorityImplicits
