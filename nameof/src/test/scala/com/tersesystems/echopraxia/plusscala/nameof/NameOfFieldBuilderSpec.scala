package com.tersesystems.echopraxia.plusscala.nameof

import com.tersesystems.echopraxia.api.Field
import com.tersesystems.echopraxia.plusscala.api._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers

import java.time.Instant

class NameOfFieldBuilderSpec extends AnyFunSpec with BeforeAndAfterEach with Matchers {

  private val fb = MyFieldBuilder

  describe("logging") {

    it("should log a person as keyValue") {
      val person = Person("thisperson", 13)
      val field  = fb.nameOfKeyValue(person)

      field.name() must be("person")
    }

    it("should log a person as value") {
      val person = Person("thisperson", 13)
      val field  = fb.nameOfValue(person)

      field.name() must be("person")
    }

  }

  trait MyFieldBuilder extends NameOfFieldBuilder with FieldBuilder {
    // Instant type
    implicit val instantToStringValue: ToValue[Instant] = (t: Instant) => ToValue(t.toString)
    def instant(name: String, i: Instant): Field        = keyValue(name, ToValue(i))
    def instant(tuple: (String, Instant)): Field        = keyValue(tuple)

    // Person type
    implicit val personToObjectValue: ToObjectValue[Person] = (p: Person) =>
      ToObjectValue(
        string("name", p.name),
        number("age", p.age)
      )
    def person(name: String, person: Person): Field = keyValue(name, person)
    def person(tuple: (String, Person)): Field      = keyValue(tuple)
  }

  object MyFieldBuilder extends MyFieldBuilder
}
