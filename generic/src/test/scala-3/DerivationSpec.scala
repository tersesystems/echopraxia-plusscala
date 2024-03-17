package com.tersesystems.echopraxia.plusscala.generic

import com.tersesystems.echopraxia.plusscala.LoggerFactory
import com.tersesystems.echopraxia.plusscala.api._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers

import java.time.Instant
import scala.collection.immutable.Seq

case object SomeObject

case class SomeId(raw: Int) extends AnyVal

case class User(name: String, id: Int)
case class Sku(id: Int, description: String)
case class LineItem(sku: Sku, quantity: Int)
case class PaymentInfo(creditCardNumber: String, expirationDate: Instant)
case class ShippingInfo(address1: String, address2: String)
final case class Order(paymentInfo: PaymentInfo, shippingInfo: ShippingInfo, lineItems: Seq[LineItem], owner: User)

class DerivationSpec extends AnyFunSpec with BeforeAndAfterEach with Matchers {

  trait AutoFieldBuilder extends FieldBuilder with AutoDerivation {
    implicit val instantToValue: ToValue[Instant] = instant => ToValue(instant.toString)
  }
  object AutoFieldBuilder extends AutoFieldBuilder

  trait SemiAutoFieldBuilder extends FieldBuilder with SemiAutoDerivation {
    implicit val instantToValue: ToValue[Instant] = instant => ToValue(instant.toString)

    // XXX there is an NPE bug in Magnolia -- the Instant implicit must be defined
    // BEFORE the payment info implicit, or the resolve will fail at runtime
    // an easy way to get around this is to define everything with lazy val
    // and that will delay initialization long enough.
    // https://github.com/softwaremill/magnolia/issues/402
    implicit lazy val userToValue: ToValue[User]                 = gen[User]
    implicit lazy val lineItemToValue: ToValue[LineItem]         = gen[LineItem]
    implicit lazy val skuToValue: ToValue[Sku]                   = gen[Sku]
    implicit lazy val paymentInfoToValue: ToValue[PaymentInfo]   = gen[PaymentInfo]
    implicit lazy val shippingInfoToValue: ToValue[ShippingInfo] = gen[ShippingInfo]
    implicit lazy val orderToValue: ToValue[Order]               = gen[Order]

    implicit lazy val someObjectToValue: ToValue[SomeObject.type] = gen[SomeObject.type]
    //implicit lazy val someIdToValue: ToValue[SomeId]              = gen[SomeId]
  }
  object SemiAutoFieldBuilder extends SemiAutoFieldBuilder

  //trait KeyValueOnly extends FieldBuilder with AutoDerivation with KeyValueCaseClassDerivation

  //trait ValueOnly extends FieldBuilder with SemiAutoDerivation with ValueCaseClassDerivation

  trait ShortEither extends FieldBuilder with AutoDerivation with EitherValueTypes

  trait ShortOption extends FieldBuilder with AutoDerivation with OptionValueTypes

  private val autoLogger = LoggerFactory.getLogger(getClass, AutoFieldBuilder)

  describe("automatic derivation") {

    it("should derive a case class") {
      val paymentInfo  = PaymentInfo("41111111", Instant.now())
      val shippingInfo = ShippingInfo("address 1", "address 2")
      val sku1         = Sku(232313, "some furniture")
      val lineItems    = Seq(LineItem(sku1, 1))
      val user         = User("user1", 2342331)
      val order        = Order(paymentInfo = paymentInfo, shippingInfo = shippingInfo, lineItems = lineItems, owner = user)
      autoLogger.info("{}", _.keyValue("order", order))
    }

    it("should derive a case object") {
      autoLogger.info("{}", _.keyValue("someObject", SomeObject))
    }

    // it("should derive an anyval") {
    //   autoLogger.info("{}", _.keyValue("someId", SomeId(1)))
    // }

    it("should derive a tuple") {
      autoLogger.info("{}", _.keyValue("tuple", (1, 2, 3, 4)))
    }
  }

  describe("semi automatic derivation") {
    val semiAutoLogger = LoggerFactory.getLogger(getClass, SemiAutoFieldBuilder)

    it("should derive a case class") {
      val paymentInfo  = PaymentInfo("41111111", Instant.now())
      val shippingInfo = ShippingInfo("address 1", "address 2")
      val sku1         = Sku(232313, "some furniture")
      val lineItems    = Seq(LineItem(sku1, 1))
      val user         = User("user1", 2342331)
      val order        = Order(paymentInfo = paymentInfo, shippingInfo = shippingInfo, lineItems = lineItems, owner = user)
      semiAutoLogger.info("{}", _.keyValue("order", order))
    }

    it("should derive a case object") {
      semiAutoLogger.info("{}", _.keyValue("someObject", SomeObject))
    }

    // it("should derive an anyval") {
    //   semiAutoLogger.info("{}", _.keyValue("someId", SomeId(1)))
    // }

    it("should derive a tuple") {
      pending // of limited value :-/
      // semiAutoLogger.info("{}", _.keyValue("tuple", (1,2,3,4)))
    }
  }

}
