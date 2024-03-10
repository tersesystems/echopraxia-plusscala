package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.{Attributes, Field, Value}
import com.tersesystems.echopraxia.plusscala.api.LoggingBase._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.util.UUID

class NameSpec extends AnyWordSpec with Matchers with LoggingBase {

  trait ToDerp[T] extends ToValueAttribute[T] {
    override def toAttributes(value: Value[_]): Attributes = withAttributes(withDisplayName("derp"))
  }

  "named fields" should {

    "work with ToName" in {
      implicit val uuidToName: ToName[UUID] = ToName.create("uuid")
      implicit val uuidToValue: ToValue[UUID] = uuid => ToValue(uuid.toString)

      val field: Field = UUID.randomUUID
      field.name must be("uuid")
    }

    "work with ToLog" in {
      implicit val uuidToLog: ToLog[UUID] = ToLog.create("uuid", uuid => ToValue(uuid.toString))

      val field: Field = UUID.randomUUID
      field.name must be("uuid")
    }

    "work with exceptions" in {
      val field: Field = new IllegalStateException()
      field.name must be("exception")
    }

    "work with displayName" in {
      implicit def exceptionToDerp[T <: Throwable]: ToDerp[T] = e => ToValue(e)

      val field: Field = new IllegalStateException()
      field.toString must be(""""derp"=java.lang.IllegalStateException""")
    }
  }
}
