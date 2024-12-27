package echopraxia.plusscala

import echopraxia.api.Field
import api._

import java.util.Currency
import java.util.UUID
import scala.concurrent.Future
import scala.reflect.ClassTag
import scala.reflect.classTag

// Each package can add its own mappings
trait Logging extends LoggingBase {

  // Echopraxia takes a bit more work the more heterogeneous the input gets.
  // For example, to pass through random tuples, you need to map it to an object
  implicit def tupleToValue[TVK: ToValue, TVV: ToValue]: ToValue[(TVK, TVV)] = { case (k, v) =>
    ToObjectValue("key" -> k, "value" -> v)
  }

  // use the class name as the name here
  // Elasticsearch doesn't like dots in field names so this doesn't go in the framework.
  implicit val uuidToField: ToField[UUID] = ToField(_ => classOf[UUID].getName, uuid => ToValue(uuid.toString))

  // Use class name for future
  implicit def futureToName[T: ClassTag]: ToName[Future[T]] = _ => s"future[${classTag[T].runtimeClass.getName}]"

  implicit val personToField: ToField[Person] = ToField(_ => "person", p => ToObjectValue("name" -> p.name, "age" -> p.age))

  implicit val govtToValue: ToValue[Government] = govt => ToObjectValue("name" -> govt.name, "debt" -> govt.debt)

  implicit val titleToField: ToField[Title] = ToField(_ => "title", t => ToValue(t.raw))

  implicit val authorToField: ToField[Author] = ToField(_ => "author", a => ToValue(a.raw))

  implicit val categoryToField: ToField[Category] = ToField(_ => "category", c => ToValue(c.raw))

  implicit val currencyToField: ToField[Currency] = ToField(_ => "currency", currency => ToValue(currency.getCurrencyCode))

  implicit val priceToField: ToField[Price] = ToField(
    _ => "price",
    price => {
      ToObjectValue(price.currency, "amount" -> price.amount).withToStringValue(price.toString)
    }
  )

  implicit val bookToField: ToField[Book] = ToField(_ => "book", book => ToObjectValue(book.title, book.category, book.author, book.price))

  implicit val creditCardToName: ToName[CreditCard] = _ => "credit_card"
  implicit def creditCardToValue(implicit cap: Sensitive = Censored): ToValue[CreditCard] = cc => {
    ToObjectValue(
      sensitiveKeyValue("cc_number", cc.number),
      "expiration_date" -> cc.expirationDate
    )
  }

  def sensitiveKeyValue(name: String, value: String)(implicit cap: Sensitive = Censored): Field = {
    cap match {
      case Censored =>
        name -> "[CENSORED]"
      case Explicit =>
        name -> value
    }
  }

  sealed trait Sensitive

  case object Censored extends Sensitive

  case object Explicit extends Sensitive
}
