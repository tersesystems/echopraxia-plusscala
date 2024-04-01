package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.Field
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.util.UUID

class NameSpec extends AnyWordSpec with Matchers with Logging {

  trait ToDerp[T] extends WithDisplayName[T] {
    override val displayName = "derp"
  }

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
      implicit def exceptionToDerp[T <: Throwable]: ToDerp[T] = e => ToValue(e)

      val field: Field = new IllegalStateException()
      field.toString must be(""""derp"=java.lang.IllegalStateException""")
    }
  }
}
