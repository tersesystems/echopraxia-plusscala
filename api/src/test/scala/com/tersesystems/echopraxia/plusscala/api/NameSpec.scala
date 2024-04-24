package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.Field
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID
import java.time.{Instant, ZoneId, ZoneOffset, ZonedDateTime}

class NameSpec extends AnyWordSpec with Matchers with Logging {

  "named fields" should {

    "work with ToName" in {
      implicit val uuidToName: ToName[UUID]   = _ => "uuid"
      implicit val uuidToValue: ToValue[UUID] = uuid => ToValue(uuid.toString)

      val field: Field = UUID.randomUUID
      field.name must be("uuid")
    }

    "work with ToField" in {
      implicit val uuidToField: ToField[UUID] = ToField(_ => "uuid", uuid => ToValue(uuid.toString))

      val field: Field = UUID.randomUUID
      field.name must be("uuid")
    }

    "work with exceptions" in {
      val field: Field = new IllegalStateException()
      field.name must be("exception")
    }

    "work with displayName" in {
      val field: Field = new IllegalStateException()
      field.withDisplayName("derp").toString must be(""""derp"=java.lang.IllegalStateException""")
    }

    "work with option" in {
      implicit val instantName: ToName[ZonedDateTime]           = zdt => zdt.getZone.toString
      implicit val optStringName: ToName[Option[ZonedDateTime]] = ToName.option(ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.of("UTC")))
      implicit val zonedDateTimeToValue: ToValue[ZonedDateTime] = zdt => ToValue(zdt.toString)

      val optInstant: Option[ZonedDateTime] = Some(ZonedDateTime.now(ZoneId.of("America/Los_Angeles")))
      val field: Field                      = optInstant -> optInstant
      field.name must be("America/Los_Angeles")
    }

    "work with none" in {
      implicit val instantName: ToName[ZonedDateTime]           = zdt => zdt.getZone.toString
      implicit val optStringName: ToName[Option[ZonedDateTime]] = ToName.option(ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.of("UTC")))
      implicit val zonedDateTimeToValue: ToValue[ZonedDateTime] = zdt => ToValue(zdt.toString)

      val optInstant: Option[ZonedDateTime] = None
      val field: Field                      = optInstant -> optInstant
      field.name must be("UTC")
    }
  }
}
