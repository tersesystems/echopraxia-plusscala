package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.{Attributes, Field, Value}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.format.{DateTimeFormatter, FormatStyle}
import java.time.{Instant, LocalDateTime, ZoneOffset}
import java.util.UUID

import LoggingBase._

class EitherSpec extends AnyWordSpec with Matchers with LoggingBase {

  // XXX Is there a way to make this work more easily?
  implicit def eitherToValueAttribute[TVL: ToValueAttribute, TVR: ToValueAttribute]: ToValueAttribute[Either[TVL, TVR]] =
    new ToValueAttribute[Either[TVL, TVR]] {
      // This isn't great, but we need to know whether left or right was picked for the attributes
      // and if we have a parameter (either: Either[]) in the method signature then it doesn't
      // pick it up?
      private var optEither: Option[Either[TVL, TVR]] = None

      override def toValue(v: Either[TVL, TVR]): Value[_] = {
        this.optEither = Some(v)
        v match {
          case Left(l)  => implicitly[ToValueAttribute[TVL]].toValue(l)
          case Right(r) => implicitly[ToValueAttribute[TVR]].toValue(r)
        }
      }

      override def toAttributes(value: Value[_]): Attributes = {
        // hack hack hack hack
        optEither match {
          case Some(either) =>
            either match {
              case Left(_) =>
                val left = implicitly[ToValueAttribute[TVL]]
                left.toAttributes(value)
              case Right(_) =>
                val right = implicitly[ToValueAttribute[TVR]]
                right.toAttributes(value)
            }
          case None =>
            // should never get here
            Attributes.empty()
        }
      }
    }

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
      implicit val uuidToValue: ToValue[UUID]       = uuid => ToValue(uuid.toString)
      implicit val instantToValue: ToValue[Instant] = instant => ToValue(instant.toString)

      // Show a human readable toString for instant
      trait ToStringFormat[T] extends ToValueAttribute[T] {
        override def toAttributes(value: Value[_]): Attributes = withAttributes(withStringFormat(value))
      }

      implicit val readableInstant: ToStringFormat[Instant] = (v: Instant) => {
        val datetime  = LocalDateTime.ofInstant(v, ZoneOffset.UTC)
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
        ToValue(formatter.format(datetime))
      }

      val either: Either[UUID, Instant] = Right(Instant.ofEpochMilli(0))
      val field: Field                  = "test" -> either
      field.toString must be("test=1/1/70, 12:00 AM")
    }
  }

}
