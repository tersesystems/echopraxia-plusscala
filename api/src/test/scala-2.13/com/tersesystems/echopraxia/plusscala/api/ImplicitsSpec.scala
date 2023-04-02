package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.Value
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers

import scala.jdk.CollectionConverters.iterableAsScalaIterableConverter

class ImplicitsSpec extends AnyFunSpec with BeforeAndAfterEach with Matchers {

  object MyFieldBuilder extends FieldBuilder {
    implicit val personToValue: ToValue[Person] = person => ToObjectValue(
      keyValue("name" -> person.name),
      keyValue("age" -> person.age)
    )

    implicit val extraPerson: ToValue[ExtraPerson] = extraPerson => {
      val objectValue = ToValue(extraPerson).asObject
      objectValue + keyValue("citizen" -> extraPerson.citizen)
    }
  }

  class Person(val name: String, val age: Int)
  class ExtraPerson(name: String, age: Int, val citizen: Boolean) extends Person(name, age)

  describe("rich object value") {
    it ("should work with single field") {
     val fb = MyFieldBuilder
      val person = new ExtraPerson("eloise", 2, citizen = true)
      val field = fb.keyValue("person" -> person)

      val objectValue: Value.ObjectValue = field.value().asObject
      val fields: Map[String, Value[_]] = objectValue.raw.asScala.map(f => f.name -> f.value).toMap
      val citizen: Boolean = fields("citizen").asBoolean.raw
      citizen must be(true)
    }
  }

}
