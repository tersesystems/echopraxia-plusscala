package echopraxia.plusscala.api

import echopraxia.api.Field
import echopraxia.api.Value
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers

import scala.collection.JavaConverters._

class ObjectSpec extends AnyFunSpec with Matchers with Logging {

  describe("object") {

    it("work with a single field") {
      val field: Field = "test" -> ("foo" -> "bar": Field)
      field.name must be("test")
      val objectValue: Value.ObjectValue = field.value().asObject()
      val fields: Seq[Field]             = objectValue.raw.asScala.toSeq
      fields.head.name must be("foo")
    }

    it("work with multiple fields with ToObjectValue") {
      val field: Field = "test" -> ToObjectValue("foo" -> "bar", "baz" -> "quux")
      field.name must be("test")
      val objectValue: Value.ObjectValue = field.value().asObject()
      val fields: Seq[Field]             = objectValue.raw.asScala.toSeq
      fields(1).name must be("baz")
    }

  }

}
