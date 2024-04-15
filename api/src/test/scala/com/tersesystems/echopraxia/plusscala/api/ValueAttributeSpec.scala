package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.{Attributes, Field, PresentationField, Value}
import com.tersesystems.echopraxia.spi.PresentationHintAttributes
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers

import java.util.Base64
import java.util.UUID

class ValueAttributeSpec extends AnyFunSpec with BeforeAndAfterEach with Matchers with Logging {

  describe("AbbreviateAfter") {
    it("should abbreviate a string") {
      implicit val uuidToValue: ToValue[UUID]            = uuid => ToValue(uuid.toString).abbreviateAfter(4)

      val field: Field = "uuid" -> UUID.fromString("eb1497ad-e3c1-45a3-8305-9d394a72afbe")
      field.toString must be("uuid=eb14...")
    }

    it("should abbreviate an array") {
      val field: Field = "array" -> ToArrayValue(Seq(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)).abbreviateAfter(4)
      field.asInstanceOf[PresentationField].toString must be("array=[1, 2, 3, 4...]")
    }

    it("should abbreviate values in array") {
      // if this doesn't work, it's a problem with the underlying attribute
      implicit val uuidToValue: ToValue[UUID]            = uuid => ToValue(uuid.toString).abbreviateAfter(4)

      val uuid         = UUID.fromString("eb1497ad-e3c1-45a3-8305-9d394a72afbe")
      val field: Field = "uuids" -> Seq(uuid, uuid)
      field.toString must be("uuids=[eb14..., eb14...]")
    }
  }

  describe("Elided") {
    it("should elide a field") {
      implicit val emailToField: ToField[Email]       = ToField[Email](_ => "email", c => ToValue(c.value))
      implicit val payloadToField: ToField[Payload]   = ToField[Payload](_ => "payload", c => ToValue(Base64.getEncoder.encodeToString(c.value)))
      implicit val downloadToField: ToField[Download] = ToField[Download](_ => "download", c => {
        val payloadField: Field = c.payload
        ToObjectValue(c.email, payloadField.asInstanceOf[PresentationField].asElided())
      })

      val download     = Download(new Email("user@example.org"), new Payload(Array.emptyByteArray))
      val field: Field = "download" -> download
      field.toString must be("download={email=user@example.org}")
    }

    it("should elide the value in seq") {
      implicit val emailToField: ToField[Email]       = ToField[Email](_ => "email", c => ToValue(c.value))
      implicit val payloadToField: ToField[Payload]   = ToField[Payload](_ => "payload", c => ToValue(c.value))
      implicit val downloadToField: ToField[Download] = ToField[Download](_ => "download", c => {
        val payloadField: Field = c.payload
        ToObjectValue(c.email, payloadField.asInstanceOf[PresentationField].asElided())
      })

      val download     = Download(new Email("user@example.org"), new Payload(Array.emptyByteArray))
      val field: Field = "downloads" -> Seq(download, download)
      field.toString must be("downloads=[{email=user@example.org}, {email=user@example.org}]")
    }
  }

  describe("AsCardinal") {
    it("should cardinal an array of bytes") {
      implicit val emailToField: ToField[Email]           = ToField[Email](_ => "email", c => ToValue(c.value))
      implicit val payloadToField: ToField[Payload]       = ToField[Payload](_ => "payload", c => ToArrayValue(c.value).asCardinal)
      implicit val downloadToField: ToField[Download]     = ToField[Download](_ => "download", c => ToObjectValue(c.email, c.payload))

      val download     = Download(new Email("user@example.org"), Payload(Array.emptyByteArray))
      val field: Field = "download" -> download
      val payloadField: Field = Payload(Array.emptyByteArray)
      payloadField.toString must be("payload=|0|")
      val fieldString = field.toString
      fieldString must be("download={email=user@example.org, payload=|0|}")
    }

    it("should cardinal a string") {
      implicit val uuidToValue: ToValue[UUID]       = uuid => ToValue(uuid.toString).asCardinal

      val uuid         = UUID.fromString("eb1497ad-e3c1-45a3-8305-9d394a72afbe")
      val field: Field = "uuid" -> uuid
      field.toString must be("uuid=|36|")
    }

    it("should cardinal a seq of strings") {
      implicit val uuidToValue: ToValue[UUID]       = uuid => ToValue(uuid.toString).asCardinal

      val uuid         = UUID.fromString("eb1497ad-e3c1-45a3-8305-9d394a72afbe")
      val field: Field = "seq" -> Seq(uuid, uuid)
      field.toString must be("seq=[|36|, |36|]")
    }
  }
}

final case class Payload(value: Array[Byte]) extends AnyVal
final case class Email(value: String)        extends AnyVal
final case class Download(email: Email, payload: Payload)
