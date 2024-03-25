package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.plusscala.spi.Utils
import com.tersesystems.echopraxia.spi.DefaultField
import com.tersesystems.echopraxia.api.Field
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers
import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.predicates.all.NonEmpty

// refined doesn't exist in scala 3
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
    override type Name      = String Refined NonEmpty
    override type FieldType = DefaultField
    protected val fieldClass: Class[DefaultField] = classOf[DefaultField]

    override def keyValue[V: ToValue: ToValueAttributes](key: Name, value: V): DefaultField = Field.keyValue(key.value, ToValue(value), fieldClass)
    override def value[V: ToValue: ToValueAttributes](key: Name, value: V): DefaultField    = Field.value(key.value, ToValue(value), fieldClass)

    override def keyValue[V: ToValue: ToValueAttributes](tuple: (Name, V)): DefaultField = Field.keyValue(tuple._1.value, ToValue(tuple._2), fieldClass)
    override def value[V: ToValue: ToValueAttributes](tuple: (Name, V)): DefaultField    = Field.value(tuple._1.value, ToValue(tuple._2), fieldClass)
  }

  object RefinedFieldBuilder extends RefinedFieldBuilder

}
