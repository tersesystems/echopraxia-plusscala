package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.Value
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers

class ImplicitsSpec extends AnyFunSpec with BeforeAndAfterEach with Matchers {

  // If in same module...
  trait CatAwareFieldBuilder extends FieldBuilder {
    implicit def animalValue[A <: Animal]: ToValue[A] = { p: A =>
      val fields = Seq(keyValue("name" -> p.name), keyValue("color" -> p.color))
      p match {
        case cat: Cat =>
          ToObjectValue(fields :+ keyValue("goodCat" -> cat.goodCat))
        case _ =>
          ToObjectValue(fields)
      }
    }
  }

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

  describe("rich object value") {
    it("should work with single field") {
      val fb    = CatFieldBuilder
      val cat   = new Cat("indra", "black", goodCat = true)
      val field = fb.keyValue("cat", cat)

      val objectValue: Value.ObjectValue = field.value().asObject
      val catField                       = objectValue.raw()
      catField.stream().anyMatch(_.name == "goodCat") must be(true)
    }
  }

}
