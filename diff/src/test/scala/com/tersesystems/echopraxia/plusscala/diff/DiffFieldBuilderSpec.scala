package com.tersesystems.echopraxia.plusscala.diff

import com.tersesystems.echopraxia.api.{Field, Value}
import com.tersesystems.echopraxia.plusscala.api._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers

import java.time.Instant
import scala.collection.immutable.Seq
import scala.jdk.CollectionConverters._

class DiffBuilderSpec extends AnyFunSpec with BeforeAndAfterEach with Matchers {

  private val fb = MyFieldBuilder

  describe("logging") {

    it("should diff correctly") {
      val person1 = Person("person1", 13)
      val person2 = Person("person2", 13)
      val diffField = fb.diff("personDiff", person1, person2)

      val obj = diffField.value.asInstanceOf[Value.ArrayValue]
      obj.raw.asScala must contain theSameElementsAs
        Seq(
          Value.`object`(
            Field.keyValue("op", Value.string("replace")),
            Field.keyValue("path", Value.string("/name")),
            Field.keyValue("value", Value.string("person2"))
          )
        )
    }

    //    private def logOrder() = {
    //      val paymentInfo = PaymentInfo("41111111", Instant.now())
    //      val shippingInfo = ShippingInfo("address 1", "address 2")
    //      val sku1 = Sku(232313, "some furniture")
    //      val lineItems = Seq(LineItem(sku1, 1))
    //      val user = User("user1", 2342331)
    //      val order = Order(paymentInfo = paymentInfo, shippingInfo = shippingInfo, lineItems = lineItems, owner = user)
    //      autoLogger.info("{}", _.keyValue("order", order))
    //
    //      autoLogger.info("diff {}", _.diff("diff", order, order.copy(owner = order.owner.copy(name = "user2"))))
    //    }
  }

  trait MyFieldBuilder extends DiffFieldBuilder with FieldBuilder {
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


case class User(name: String, id: Int)

case class Sku(id: Int, description: String)

case class LineItem(sku: Sku, quantity: Int)

case class PaymentInfo(creditCardNumber: String, expirationDate: Instant)

case class ShippingInfo(address1: String, address2: String)

final case class Order(paymentInfo: PaymentInfo, shippingInfo: ShippingInfo, lineItems: Seq[LineItem], owner: User)
