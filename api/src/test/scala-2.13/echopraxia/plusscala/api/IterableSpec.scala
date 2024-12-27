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

// The tests here compile in 2.13 but do not compile in 2.12
class IterableSpec extends AnyWordSpec with Matchers with LoggingBase with HeterogeneousFieldSupport {

  "iterable" should {
    // XXX check array depends on implicit
    // XXX check immutable iterable depends on implicit
    // XXX check mutable iterable depends on implicit
    // XXX check identity implicit

    "work for primitives" in {
      val seq: Seq[Int] = Seq(1, 2, 3)
      val field: Field  = "test" -> seq
      field.name must be("test")
      import scala.collection.JavaConverters._
      field.value must be(Value.array(seq.map(ToValue(_)).asJava))
      field.toString must be("test=[1, 2, 3]")
    }

    "work for objects" in {
      implicit val readableInstant: ToValue[Instant] = (v: Instant) => {
        ToValue(v.toString)
      }
      val instant1     = Instant.ofEpochMilli(0)
      val instant2     = Instant.ofEpochMilli(1000000)
      val field: Field = "test" -> Seq(instant1, instant2)
      field.toString must be("test=[1970-01-01T00:00:00Z, 1970-01-01T00:16:40Z]")
    }

    "work for objects with custom attributes" in {
      implicit val readableInstant: ToValue[Instant] = (v: Instant) => {
        val datetime  = LocalDateTime.ofInstant(v, ZoneOffset.UTC)
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
        ToValue(v.toString).withToStringValue(formatter.format(datetime))
      }

      val instant1     = Instant.ofEpochMilli(0)
      val instant2     = Instant.ofEpochMilli(1000000)
      val field: Field = "test" -> Seq(instant1, instant2)
      field.toString must be("test=[1/1/70, 12:00 AM, 1/1/70, 12:16 AM]")
    }

    "work with set" in {
      implicit val readableInstant: ToValue[Instant] = (v: Instant) => {
        ToValue(v.toString)
      }

      val instant1     = Instant.ofEpochMilli(0)
      val instant2     = Instant.ofEpochMilli(1000000)
      val field: Field = "test" -> Set(instant1, instant2)
      field.toString must be("test=[1970-01-01T00:00:00Z, 1970-01-01T00:16:40Z]")
    }

    "work with fields using ToArrayValue" in {
      val instant1 = Instant.ofEpochMilli(0)
      val instant2 = Instant.ofEpochMilli(1000000)
      implicit val readableInstant: ToValue[Instant] = (v: Instant) => {
        ToValue(v.toString)
      }

      val fields       = Seq[Field]("instant1" -> instant1, "instant2" -> instant2)
      val field: Field = "test" -> ToArrayValue(fields)
      field.toString must be("test=[{instant1=1970-01-01T00:00:00Z}, {instant2=1970-01-01T00:16:40Z}]")
    }

    "work with fields with just plain fields" in {
      val instant1 = Instant.ofEpochMilli(0)
      val instant2 = Instant.ofEpochMilli(1000000)
      implicit val readableInstant: ToValue[Instant] = (v: Instant) => {
        ToValue(v.toString)
      }

      val fields: Seq[Field] = Seq[Field]("instant1" -> instant1, "instant2" -> instant2)
      val field: Field       = "test" -> fields
      field.toString must be("test=[{instant1=1970-01-01T00:00:00Z}, {instant2=1970-01-01T00:16:40Z}]")
    }
  }

}
