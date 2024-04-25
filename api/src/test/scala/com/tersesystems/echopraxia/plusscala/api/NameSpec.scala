package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.Field
import enumeratum.EnumEntry.Snakecase
import enumeratum._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

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
      implicit val zonedName: ToName[ZonedDateTime]           = optZone => optZone.map(_.getZone.toString).orNull
      implicit val zonedDateTimeToValue: ToValue[ZonedDateTime] = zdt => ToValue(zdt.toString)

      val optInstant: Option[ZonedDateTime] = Some(ZonedDateTime.now(ZoneId.of("America/Los_Angeles")))
      val field: Field                      = optInstant -> optInstant
      field.name must be("America/Los_Angeles")
    }

    "works with none" in {
      implicit val instantName: ToName[ZonedDateTime]           = optZone => optZone.map(_.getZone.toString).orNull
      implicit val zonedDateTimeToValue: ToValue[ZonedDateTime] = zdt => ToValue(zdt.toString)

      val optInstant: Option[ZonedDateTime] = None
      val field: Field                      = optInstant -> optInstant
      field.name must be("echopraxia-unknown-1")
    }

    "works with enum" in {
      implicit def enumToName[T <: EnumEntry]: ToName[T] = _.map(_.entryName).orNull

      val field: Field = Names.CreditCard -> "4111 1111 1111 1111"
      field.name() must be("credit_card")
    }
  }

  sealed trait Names extends EnumEntry with Snakecase

  object Names extends Enum[Names] {

    /*
     `findValues` is a protected method that invokes a macro to find all `Greeting` object declarations inside an `Enum`

     You use it to implement the `val values` member
     */
    val values = findValues

    case object ExpirationDate extends Names

    case object Number     extends Names
    case object CreditCard extends Names

    case object Book extends Names

    case object Instant  extends Names
    case object Title    extends Names
    case object Author   extends Names
    case object Category extends Names
    case object Price    extends Names

    case object Store extends Names
  }
}
