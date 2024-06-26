package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.Field
import com.tersesystems.echopraxia.api.PresentationField
import com.tersesystems.echopraxia.api.Value
import com.tersesystems.echopraxia.api.Value.ArrayValue
import com.tersesystems.echopraxia.api.Value.ObjectValue
import com.tersesystems.echopraxia.api.{Condition => JCondition}
import com.tersesystems.echopraxia.api.{FieldBuilderResult => JFieldBuilderResult}
import com.tersesystems.echopraxia.api.{Level => JLevel}
import com.tersesystems.echopraxia.api.{LoggingContext => JLoggingContext}

import java.util
import java.util.stream
import java.util.stream.Collectors

trait LowPriorityImplicits {
  implicit def fieldToPresentationField(field: Field): PresentationField = field.asInstanceOf[PresentationField]

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

  final implicit class RichArrayValue(val value: ArrayValue) {
    def +(v: Value[_]): ArrayValue = value.add(v)

    def ++(vs: Traversable[Value[_]]): ArrayValue = addAll(vs)

    def addAll(newValues: Traversable[Value[_]]): ArrayValue = {
      val joinedValues = new util.ArrayList(value.raw())
      for (f <- newValues) {
        joinedValues.add(f)
      }
      Value.array(joinedValues)
    }

    def ++(newValues: util.Collection[Value[_]]): ArrayValue = value.addAll(newValues)
  }

  final implicit class RichObjectValue(value: ObjectValue) {
    def +(field: Field): ObjectValue = value.add(field)

    def addAll(fields: Traversable[Field]): ObjectValue = {
      val joinedFields = new util.ArrayList(value.raw)
      for (f <- fields) {
        joinedFields.add(f)
      }
      Value.`object`(joinedFields)
    }

    def ++(fields: Traversable[Field]): ObjectValue = addAll(fields)

    def ++(fields: util.Collection[Field]): ObjectValue = value.addAll(fields)
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
