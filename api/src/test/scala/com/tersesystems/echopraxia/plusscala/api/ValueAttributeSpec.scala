package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.{Field, Value}
import com.tersesystems.echopraxia.api.Attributes

import org.scalatest.BeforeAndAfterEach
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers

import java.util.{Base64, UUID}

class ValueAttributeSpec extends AnyFunSpec with BeforeAndAfterEach with Matchers with Logging {

  trait AsValueOnlyAndAbbreviate[T] extends ToValueAttributes[T] {
    def after: Int
    def toValue(v: T): Value[_] = ToValue(v.toString)
    def toAttributes(value: Value[_]): Attributes = {
      val abbreviateAfter = AbbreviateAfter(after)
      val attrs           = abbreviateAfter.toAttributes(value)
      attrs.plusAll(AsValueOnly.attributes)
    }
  }

  object AsValueOnlyAndAbbreviate {
    def apply[T](a: Int): AsValueOnlyAndAbbreviate[T] = new AsValueOnlyAndAbbreviate[T]() {
      val after = a
    }
  }

  describe("AbbreviateAfter") {
    it("should abbreviate a string") {
      implicit val abbreviateUUID: AbbreviateAfter[UUID] = AbbreviateAfter(4)
      implicit val uuidToValue: ToValue[UUID]            = uuid => ToValue(uuid.toString)

      val field: Field = "uuid" -> UUID.fromString("eb1497ad-e3c1-45a3-8305-9d394a72afbe")
      field.toString must be("uuid=eb14...")
    }

    it("should abbreviate an array") {
      implicit val abbreviateUUID: AbbreviateAfter[Seq[Int]] = AbbreviateAfter(4)

      val field: Field = "array" -> Seq(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
      field.toString must be("array=[1, 2, 3, 4...]")
    }
  }

  describe("Elided") {
    it("should elide a field") {
      implicit val payloadAsElided: Elided[Payload]   = Elided[Payload]
      implicit val emailToField: ToField[Email]       = ToField[Email](_ => "email", c => ToValue(c.value))
      implicit val payloadToField: ToField[Payload]   = ToField[Payload](_ => "payload", c => ToValue(Base64.getEncoder.encodeToString(c.value)))
      implicit val downloadToField: ToField[Download] = ToField[Download](_ => "download", c => ToObjectValue(c.email, c.payload))

      val download     = Download(new Email("user@example.org"), new Payload(Array.emptyByteArray))
      val field: Field = "download" -> download
      field.toString must be("download={email=user@example.org}")
    }
  }

  describe("AsCardinal") {
    it("should cardinal an array of bytes") {
      implicit val payloadAsCardinal: AsCardinal[Payload] = AsCardinal[Payload]
      implicit val emailToField: ToField[Email]           = ToField[Email](_ => "email", c => ToValue(c.value))
      implicit val payloadToField: ToField[Payload]       = ToField[Payload](_ => "payload", c => ToValue(Base64.getEncoder.encodeToString(c.value)))
      implicit val downloadToField: ToField[Download]     = ToField[Download](_ => "download", c => ToObjectValue(c.email, c.payload))

      val download     = Download(new Email("user@example.org"), new Payload(Array.emptyByteArray))
      val field: Field = "download" -> download
      field.toString must be("download={email=user@example.org, payload=|0|}")
    }
  }

  describe("ToValueAttribute") {
    it("should compose two different attributes") {
      implicit val abbreviateValueOnly: AsValueOnlyAndAbbreviate[UUID] = AsValueOnlyAndAbbreviate(4)
      implicit val uuidToValue: ToValue[UUID]                          = uuid => ToValue(uuid.toString)

      val field: Field = "uuid" -> UUID.fromString("eb1497ad-e3c1-45a3-8305-9d394a72afbe")
      field.toString must be("eb14...")
    }
  }

  describe("AsValueOnly") {
    it("should render only the value") {
      implicit val uuidAsValueOnly: AsValueOnly[UUID] = AsValueOnly[UUID]
      implicit val uuidToValue: ToValue[UUID]         = uuid => ToValue(uuid.toString)

      val field: Field = "uuid" -> UUID.fromString("eb1497ad-e3c1-45a3-8305-9d394a72afbe")
      field.toString must be("eb1497ad-e3c1-45a3-8305-9d394a72afbe")
    }

    it("should work with arrays") {
      // XXX do we want this behavior?
      implicit val intAsValueOnly: AsValueOnly[Int] = AsValueOnly[Int]

      val field = ("tuple" -> Seq(1, 2))
      field.toString must be("[1, 2]")
    }
  }

  describe("WithDisplayName") {
    it("should render with a display name") {
      implicit val uuidDisplayName: WithDisplayName[UUID] = WithDisplayName[UUID]("unique id")
      implicit val uuidToValue: ToValue[UUID]             = uuid => ToValue(uuid.toString)

      val field: Field = "uuid" -> UUID.fromString("eb1497ad-e3c1-45a3-8305-9d394a72afbe")
      field.toString must be("\"unique id\"=eb1497ad-e3c1-45a3-8305-9d394a72afbe")
    }
  }

}

final case class Payload(value: Array[Byte]) extends AnyVal
final case class Email(value: String)        extends AnyVal
final case class Download(email: Email, payload: Payload)
