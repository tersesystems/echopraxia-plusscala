package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.{Attributes, Field, Value}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.format.{DateTimeFormatter, FormatStyle}
import java.time.{Instant, LocalDateTime, ZoneOffset}
import java.util.UUID

import LoggingBase._

class EitherSpec extends AnyWordSpec with Matchers with LoggingBase {

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
      val field: Field = "test" -> either
      field.name must be("test")
      field.value must be(Value.string("right"))
    }

    "work with a custom attribute" in {
      implicit val uuidToValue: ToValue[UUID] = uuid => ToValue(uuid.toString)
      implicit val instantToValue: ToValue[Instant] = instant => ToValue(instant.toString)

      // Show a human readable toString for instant
      trait ToStringFormat[T] extends ToValueAttribute[T] {
        override def toAttributes(value: Value[_]): Attributes = withAttributes(withStringFormat(value))
      }

      implicit val readableInstant: ToStringFormat[Instant] = (v: Instant) => {
        val datetime = LocalDateTime.ofInstant(v, ZoneOffset.UTC)
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
        ToValue(formatter.format(datetime))
      }

      val either: Either[UUID, Instant] = Right(Instant.ofEpochMilli(0))
      val field: Field = "test" -> either
      field.toString must be("test=1/1/70, 12:00 AM")
    }
  }

}