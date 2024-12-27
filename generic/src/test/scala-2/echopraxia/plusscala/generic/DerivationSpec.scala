package echopraxia.plusscala.generic

import echopraxia.plusscala.api._
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

  trait AutoLogging extends AutoDerivation {
    implicit val instantToValue: ToValue[Instant] = instant => ToValue(instant.toString)
  }

  trait AutoFieldBuilder  extends FieldBuilder with AutoLogging
  object AutoFieldBuilder extends AutoFieldBuilder

  trait SemiAutoLogging extends SemiAutoDerivation {
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
    implicit lazy val someIdToValue: ToValue[SomeId]              = gen[SomeId]
  }

  trait SemiAutoFieldBuilder  extends FieldBuilder with SemiAutoLogging
  object SemiAutoFieldBuilder extends SemiAutoFieldBuilder

  // trait KeyValueOnly extends FieldBuilder with AutoDerivation with KeyValueCaseClassDerivation

  // trait ValueOnly extends FieldBuilder with SemiAutoDerivation with ValueCaseClassDerivation

  // trait ShortEither extends FieldBuilder with AutoDerivation with EitherValueTypes

  // trait ShortOption extends AutoDerivation with OptionValueTypes

  describe("automatic derivation") {

    it("should derive a case class") {
      val paymentInfo  = PaymentInfo("41111111", Instant.EPOCH)
      val shippingInfo = ShippingInfo("address 1", "address 2")
      val sku1         = Sku(232313, "some furniture")
      val lineItems    = Seq(LineItem(sku1, 1))
      val user         = User("user1", 2342331)
      val order        = Order(paymentInfo = paymentInfo, shippingInfo = shippingInfo, lineItems = lineItems, owner = user)

      val field = AutoFieldBuilder.keyValue("order" -> order)
      field.toString must be(
        "order={@type=echopraxia.plusscala.generic.Order, paymentInfo={@type=echopraxia.plusscala.generic.PaymentInfo, creditCardNumber=41111111, expirationDate=1970-01-01T00:00:00Z}, shippingInfo={@type=echopraxia.plusscala.generic.ShippingInfo, address1=address 1, address2=address 2}, lineItems=[{@type=echopraxia.plusscala.generic.LineItem, sku={@type=echopraxia.plusscala.generic.Sku, id=232313, description=some furniture}, quantity=1}], owner={@type=echopraxia.plusscala.generic.User, name=user1, id=2342331}}"
      )
    }

    it("should derive a case object") {
      val field = AutoFieldBuilder.keyValue("someObject", SomeObject)
      field.toString must be("someObject=SomeObject")
    }

    it("should derive an anyval") {
      val field = AutoFieldBuilder.keyValue("someId", SomeId(1))
      field.toString must be("someId=1")
    }

    it("should derive a tuple") {
      val field = AutoFieldBuilder.keyValue("tuple", (1, 2, 3, 4))
      field.toString must be("tuple={@type=scala.Tuple4, _1=1, _2=2, _3=3, _4=4}")
    }
  }

  describe("semi automatic derivation") {

    it("should derive a case class") {
      val paymentInfo  = PaymentInfo("41111111", Instant.EPOCH)
      val shippingInfo = ShippingInfo("address 1", "address 2")
      val sku1         = Sku(232313, "some furniture")
      val lineItems    = Seq(LineItem(sku1, 1))
      val user         = User("user1", 2342331)
      val order        = Order(paymentInfo = paymentInfo, shippingInfo = shippingInfo, lineItems = lineItems, owner = user)
      val field        = SemiAutoFieldBuilder.keyValue("order", order)
      field.toString must be(
        "order={@type=echopraxia.plusscala.generic.Order, paymentInfo={@type=echopraxia.plusscala.generic.PaymentInfo, creditCardNumber=41111111, expirationDate=1970-01-01T00:00:00Z}, shippingInfo={@type=echopraxia.plusscala.generic.ShippingInfo, address1=address 1, address2=address 2}, lineItems=[{@type=echopraxia.plusscala.generic.LineItem, sku={@type=echopraxia.plusscala.generic.Sku, id=232313, description=some furniture}, quantity=1}], owner={@type=echopraxia.plusscala.generic.User, name=user1, id=2342331}}"
      )
    }

    it("should derive a case object") {
      val field = SemiAutoFieldBuilder.keyValue("someObject", SomeObject)
      field.toString must be("someObject=SomeObject")
    }

    it("should derive an anyval") {
      val field = SemiAutoFieldBuilder.keyValue("someId", SomeId(1))
      field.toString must be("someId=1")
    }

    it("should derive a tuple") {
      pendingUntilFixed {
        fail()
      }
      // val field = SemiAutoFieldBuilder.keyValue("tuple", (1,2,3,4))
      // field.toString must be("tuple={@type=scala.Tuple4, _1=1, _2=2, _3=3, _4=4}")
    }
  }

}