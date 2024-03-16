package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.FieldBuilderResult
import com.tersesystems.echopraxia.api.Field


trait FieldBuilderResultTypeClasses {

  // if using -T here then all the subtypes of iterable also apply
  trait ToFieldBuilderResult[-T] {
    def toResult(input: T): FieldBuilderResult
  }

  trait LowPriorityToFieldBuilderResult {
    implicit def typeClassConversion[T: ToFieldBuilderResult](input: T): FieldBuilderResult =
      ToFieldBuilderResult[T](input)

    implicit val iterableToFieldBuilderResult: ToFieldBuilderResult[Iterable[Field]] =
      iterable => FieldBuilderResult.list(iterable.toArray)

    implicit val iteratorToFieldBuilderResult: ToFieldBuilderResult[Iterator[Field]] = iterator => {
      import scala.jdk.CollectionConverters._
      FieldBuilderResult.list(iterator.asJava)
    }

    // array doesn't seem to be covered by Iterable
    implicit val arrayToFieldBuilderResult: ToFieldBuilderResult[Array[Field]] = FieldBuilderResult.list(_)
  }

  object LowPriorityToFieldBuilderResult {

  }

  object ToFieldBuilderResult extends LowPriorityToFieldBuilderResult {
    def apply[T: ToFieldBuilderResult](input: T): FieldBuilderResult =
      implicitly[ToFieldBuilderResult[T]].toResult(input)
  }

}


trait ListToFieldBuilderResultMethods extends FieldBuilderResultTypeClasses {

  def list(fields: Field*): FieldBuilderResult = list(fields)

  def list[T: ToFieldBuilderResult](input: T): FieldBuilderResult = ToFieldBuilderResult[T](input)

}
