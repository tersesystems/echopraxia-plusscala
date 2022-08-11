package com.tersesystems.echopraxia.plusscala.dump


import com.tersesystems.echopraxia.api.{Field, Value}
import com.tersesystems.echopraxia.plusscala.api._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers

import java.time.Instant
import scala.jdk.CollectionConverters._

class DumpBuilderSpec extends AnyFunSpec with BeforeAndAfterEach with Matchers {
  
  private val fb = MyFieldBuilder

  describe("logging") {

    it("should dump the public values") {
      val person = Person("thisperson", 13)
      val value = fb.dumpPublicFields(person)

      value.raw().asScala must contain theSameElementsAs
        Seq(
          Field.keyValue("name", Value.string("thisperson")),
          Field.keyValue("age", Value.string("13"))
        )
    }

  }

  trait MyFieldBuilder extends DumpFieldBuilder with FieldBuilder {
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

case class Person(name: String, age: Int)
