package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.Field
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers
import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.predicates.all.NonEmpty

class RefinedFieldBuilderSpec extends AnyFunSpec with Matchers {

  private val fb = RefinedFieldBuilder

  describe("FieldBuilder") {

    it("should work with java.lang.Byte") {
      val byte = java.lang.Byte.MIN_VALUE
      fb.keyValue(refineMV[NonEmpty]("byte"), byte)
    }

    it("should work with java.lang.Short") {
      val short = java.lang.Short.MIN_VALUE
      fb.keyValue(refineMV[NonEmpty]("short"), short)
    }
  }

  trait RefinedFieldBuilder extends ArgsFieldBuilder with TupleFieldBuilder {
    override type Name = String Refined NonEmpty

    override def keyValue[V: ToValue](key: Name, value: V): Field = Field.keyValue(key.value, ToValue(value))
    override def value[V: ToValue](key: Name, value: V): Field    = Field.keyValue(key.value, ToValue(value))

    override def value[V: ToValue](tuple: (Name, V)): Field    = Field.value(tuple._1.value, ToValue(tuple._2))
    override def keyValue[V: ToValue](tuple: (Name, V)): Field = Field.keyValue(tuple._1.value, ToValue(tuple._2))
  }

  object RefinedFieldBuilder extends RefinedFieldBuilder

}
