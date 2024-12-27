package echopraxia.plusscala.api

import echopraxia.api.Field
import echopraxia.api.Value
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.UUID

class EitherSpec extends AnyWordSpec with Matchers with Logging {

  "either" should {

    "work with Left" in {
      val field: Field = "test" -> Left("left")
      field.name must be("test")
      field.value must be(Value.string("left"))
    }

    "work with Right" in {
      val field: Field = "test" -> Right("right")
      field.name must be("test")
      field.value must be(Value.string("right"))
    }

    "work with Either" in {
      val either: Either[Int, String] = Right("right")
      val field: Field                = "test" -> either
      field.name must be("test")
      field.value must be(Value.string("right"))
    }

    "work with a custom attribute" in {
      implicit val uuidToValue: ToValue[UUID] = uuid => ToValue(uuid.toString)
      implicit val instantToValue: ToValue[Instant] = instant => {
        val datetime  = LocalDateTime.ofInstant(instant, ZoneOffset.UTC)
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
        ToValue(instant.toString).withToStringValue(formatter.format(datetime))
      }

      val either: Either[UUID, Instant] = Right(Instant.ofEpochMilli(0))
      val field: Field                  = "test" -> either
      field.toString must be("test=1/1/70, 12:00 AM")
    }
  }

}
