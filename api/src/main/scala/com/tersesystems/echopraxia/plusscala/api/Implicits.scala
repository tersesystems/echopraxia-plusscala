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

  final implicit class RichArrayValue(value: Value[util.List[Value[_]]]) {

    def +(value: Value[_]): Value[util.List[Value[_]]] = add(value)

    def ++(values: Traversable[Value[_]]): Value[util.List[Value[_]]] = append(values)

    def ++(values: util.Collection[Value[_]]): Value[util.List[Value[_]]] = append(values)
  }

  final implicit class RichObjectValue(value: Value[util.List[Field]]) {

    def +(field: Field): Value[util.List[Field]] = add(field)

    def append(fields: Traversable[Field]): Value[util.List[Field]] = {
      val joinedFields = new util.ArrayList(value.raw)
      for (f <- fields) {
        joinedFields.add(f)
      }
      Value.`object`(joinedFields)
    }

    def ++(fields: Traversable[Field]): Value[util.List[Field]] = append(fields)

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
