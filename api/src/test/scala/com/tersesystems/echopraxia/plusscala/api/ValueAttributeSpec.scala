package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.{Field, Value}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers
import java.util.UUID

class ValueAttributeSpec extends AnyFunSpec with BeforeAndAfterEach with Matchers with Logging {

  describe("ToValueAttribute") {
    it("should compose two different attributes") {
      implicit val abbreviateValueOnly: AsValueOnlyAndAbbreviate[UUID] = AsValueOnlyAndAbbreviate(4)
      implicit val uuidToValue: ToValue[UUID] = uuid => ToValue(uuid.toString)

      val field: Field = "uuid" -> UUID.randomUUID
      field.toString must be("derp")
    }
  }

}
