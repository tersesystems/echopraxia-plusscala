package com.tersesystems.echopraxia.plusscala.api

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers

import java.math.BigInteger

class FieldBuilderSpec extends AnyFunSpec with Matchers {

  describe("FieldBuilder") {

    it("should work with java.lang.Byte") {
      val fb   = FieldBuilder
      val byte = java.lang.Byte.MIN_VALUE
      fb.keyValue("byte", byte)
    }

    it("should work with java.lang.Byte as a tuple") {
      val fb   = FieldBuilder
      val byte = java.lang.Byte.MIN_VALUE
      fb.keyValue("byte" -> byte)
    }

    it("should work with java.lang.Byte using fb.number") {
      val fb   = FieldBuilder
      val byte = java.lang.Byte.MIN_VALUE
      fb.number("byte", byte)
    }

    it("should work with java.lang.Byte using fb.number as tuple") {
      val fb   = FieldBuilder
      val byte = java.lang.Byte.MIN_VALUE
      fb.number("byte" -> byte)
    }

    it("should work with scala.Byte") {
      val fb   = FieldBuilder
      val byte = Byte.MinValue
      fb.keyValue("byte", byte)
    }

    it("should work with scala.Byte as a tuple") {
      val fb   = FieldBuilder
      val byte = Byte.MinValue
      fb.keyValue("byte" -> byte)
    }

    it("should work with scala.Byte using fb.number") {
      val fb   = FieldBuilder
      val byte = Byte.MinValue
      fb.number("byte", byte)
    }

    it("should work with scala.Byte using fb.number as a tuple") {
      val fb   = FieldBuilder
      val byte = Byte.MinValue
      fb.number("byte" -> byte)
    }

    it("should work with java.lang.Short") {
      val fb    = FieldBuilder
      val short = java.lang.Short.MIN_VALUE
      fb.keyValue("short", short)
    }

    it("should work with java.lang.Integer") {
      val fb      = FieldBuilder
      val integer = java.lang.Integer.valueOf(1)
      fb.keyValue("int", integer)
    }

    it("should not die when given a null integer") {
      val fb                         = FieldBuilder
      val integer: java.lang.Integer = null
      fb.keyValue("int", integer)
    }

    it("should work with java.lang.Long") {
      val fb   = FieldBuilder
      val long = java.lang.Long.valueOf(1)
      fb.keyValue("long", long)
    }

    it("should work with java.lang.Float") {
      val fb    = FieldBuilder
      val float = java.lang.Float.valueOf(1)
      fb.keyValue("float", float)
    }

    it("should work with java.lang.Double") {
      val fb     = FieldBuilder
      val double = java.lang.Double.valueOf(1)
      fb.keyValue("double", double)
    }

    it("should work with java.lang.BigInteger") {
      val fb         = FieldBuilder
      val bigInteger = BigInteger.ZERO
      fb.keyValue("bigInteger", bigInteger)
    }

    it("should work with java.lang.BigDecimal") {
      val fb         = FieldBuilder
      val bigDecimal = BigDecimal.valueOf(1)
      fb.keyValue("bigDecimal", bigDecimal)
    }

    it("should work with java.lang.Boolean") {
      val fb   = FieldBuilder
      val bool = java.lang.Boolean.TRUE
      fb.keyValue("bool", bool)
    }

  }

}
