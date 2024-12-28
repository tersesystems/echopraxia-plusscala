package echopraxia.plusscala.api

import eu.timepit.refined.*
import eu.timepit.refined.api.Refined
import eu.timepit.refined.predicates.all.NonEmpty
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers

class RefinedFieldBuilderSpec extends AnyFunSpec with Matchers {

  private val fb = RefinedFieldBuilder

  describe("FieldBuilder") {

    it("should work with java.lang.Byte") {
      val byte     = java.lang.Byte.MIN_VALUE
      val byteName = refineV[NonEmpty]("byte")
      fb.keyValue(byteName, byte)
    }

    it("should work with java.lang.Short") {
      val short     = java.lang.Short.MIN_VALUE
      val shortName = refineV[NonEmpty]("short")
      fb.keyValue(shortName, short)
    }
  }

  trait RefinedFieldBuilder extends FieldBuilderBase {
    type Name = String Refined NonEmpty

    implicit val refinedToName: ToName[Name] = _.map(_.value).orNull
  }

  object RefinedFieldBuilder extends RefinedFieldBuilder

}
