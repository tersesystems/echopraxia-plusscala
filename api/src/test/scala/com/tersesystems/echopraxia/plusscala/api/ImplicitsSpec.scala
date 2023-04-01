package com.tersesystems.echopraxia.plusscala.api

import org.scalatest.BeforeAndAfterEach
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers

class ImplicitsSpec extends AnyFunSpec with BeforeAndAfterEach with Matchers {

  object MyFieldBuilder extends FieldBuilder {
    implicit val personToValue: ToValue[Person] = person => ToObjectValue(
      keyValue("name" -> person.name),
      keyValue("age" -> person.age)
    )

    implicit val extraPerson: ToValue[ExtraPerson] = extraPerson => {
      val objectValue = ToValue(extraPerson)(personToValue).asObject
      objectValue + keyValue("citizen" -> extraPerson.citizen)
    }
  }

  class Person(val name: String, val age: Int)
  class ExtraPerson(name: String, age: Int, val citizen: Boolean) extends Person(name, age)

  describe("rich object value") {
    it ("should work with single field") {
      import Implicits._

     val fb = MyFieldBuilder
      val person = new ExtraPerson("eloise", 2, citizen = true)
      val field = fb.keyValue("person" -> person)

      field must not be(null)
    }
  }

}
