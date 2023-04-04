package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.{Field, Value}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers

import java.util

class ImplicitsSpec extends AnyFunSpec with BeforeAndAfterEach with Matchers {

  trait AnimalFieldBuilder extends FieldBuilder {
    // this must be a method so we can call super:
    implicit def animalToValue: ToValue[Animal] = { a =>
      ToObjectValue(keyValue("name" -> a.name), keyValue("color" -> a.color))
    }
  }

  // Special case cat as an animal
  trait CatFieldBuilder extends AnimalFieldBuilder {
    override implicit val animalToValue: ToValue[Animal] = { animal =>
      // call super method
      val animalValue = super.animalToValue.toValue(animal).asObject
      animal match {
        case cat: Cat =>
          animalValue.add(keyValue("goodCat" -> cat.goodCat))
        case _ =>
          animalValue
      }
    }
  }

  object CatFieldBuilder extends CatFieldBuilder

  class Animal(val name: String, val color: String)
  class Cat(name: String, color: String, val goodCat: Boolean) extends Animal(name, color)

  describe("rich ObjectValue") {
    it("should work with a field builder") {
      val fb    = CatFieldBuilder
      val cat   = new Cat("indra", "black", goodCat = true)
      val field = fb.keyValue("cat", cat)

      val objectValue: Value.ObjectValue = field.value().asObject
      val catField                       = objectValue.raw()
      catField.stream().anyMatch(_.name == "goodCat") must be(true)
    }

    it("should add a field") {
      val fb = CatFieldBuilder
      val cat = new Cat("indra", "black", goodCat = true)
      val field = fb.keyValue("cat", cat)
      val objectValue: Value.ObjectValue = field.value().asObject
      val extra = objectValue.add(fb.string("stringField", "foo")).asObject.raw

      extra.stream().anyMatch(_.name == "stringField") must be(true)
    }

    it("should addAll with Seq") {
      val fb                             = CatFieldBuilder
      val cat                            = new Cat("indra", "black", goodCat = true)
      val field                          = fb.keyValue("cat", cat)
      val objectValue: Value.ObjectValue = field.value().asObject
      val fields = Seq(
        fb.string("stringField", "foo"),
        fb.number("numField", 1L)
      )
      val extra = objectValue.addAll(fields).asObject.raw

      extra.stream().anyMatch(_.name == "stringField") must be(true)
    }

    it("should addAll with util.Collections") {
      val fb                             = CatFieldBuilder
      val cat                            = new Cat("indra", "black", goodCat = true)
      val field                          = fb.keyValue("cat", cat)
      val objectValue: Value.ObjectValue = field.value().asObject
      val fields: util.List[Field] = util.Arrays.asList(
        fb.string("stringField", "foo"),
        fb.number("numField", 1L)
      )
      val extra = objectValue.addAll(fields).asObject.raw

      extra.stream().anyMatch(_.name == "stringField") must be(true)
    }
  }

  describe("rich ArrayValue") {
    it("should work with a field builder") {
      val fb        = CatFieldBuilder
      val cat       = new Cat("indra", "black", goodCat = true)
      val cats      = Seq(cat)
      val fields    = fb.array("array", cats).value.asArray.raw
      val catValue  = fields.get(0).asObject
      val catFields = catValue.raw
      catFields.stream().anyMatch(f => f.name == "name" && f.value().raw == "indra") must be(true)
    }

    it("should append extra values") {
      val arrayValue    = Value.array("one", "two")
      val newArrayValue = arrayValue.add(Value.string("three"))
      newArrayValue.raw().size() must be(3)
    }
  }

}
