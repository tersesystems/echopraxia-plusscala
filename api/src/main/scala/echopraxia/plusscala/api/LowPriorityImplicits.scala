package echopraxia.plusscala.api

import echopraxia.api.Value._
import echopraxia.api._
import echopraxia.api.{FieldBuilderResult => JFieldBuilderResult}

import java.util
import java.util.stream
import java.util.stream.Collectors

trait LowPriorityImplicits {

  final implicit class RichArrayValue(val value: ArrayValue) {
    def +(v: Value[?]): ArrayValue = value.add(v)

    def ++(vs: Traversable[Value[?]]): ArrayValue = addAll(vs)

    def addAll(newValues: Traversable[Value[?]]): ArrayValue = {
      val joinedValues = new util.ArrayList(value.raw())
      for (f <- newValues) {
        joinedValues.add(f)
      }
      Value.array(joinedValues)
    }

    def ++(newValues: util.Collection[Value[?]]): ArrayValue = value.addAll(newValues)
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
