package com.tersesystems.echopraxia.plusscala

import com.tersesystems.echopraxia.api.Value
import com.tersesystems.echopraxia.api.Field
import com.tersesystems.echopraxia.plusscala.api._

import java.util.{Currency, UUID}
import scala.concurrent.Future
import scala.reflect.{ClassTag, classTag}

// Each package can add its own mappings
trait Logging extends LoggingBase {

  // Echopraxia takes a bit more work the more heterogeneous the input gets.
  // For example, to pass through random tuples, you need to map it to an object
  implicit def tupleToValue[TVK: ToValue: ToValueAttributes, TVV: ToValue: ToValueAttributes]: ToValue[Tuple2[TVK, TVV]] = { case (k, v) =>
    ToObjectValue("key" -> k, "value" -> v)
  }

  // use the class name as the name here
  // Elasticsearch doesn't like dots in field names so this doesn't go in the framework.
  implicit val uuidToLog: ToLog[UUID] = ToLog.create(classOf[UUID].getName, uuid => ToValue(uuid.toString))

  // Use class name for future
  implicit def futureToName[T: ClassTag]: ToName[Future[T]] = _ => s"future[${classTag[T].runtimeClass.getName}]"

  implicit val personToLog: ToLog[Person] = ToLog.create("person", p => ToObjectValue("name" -> p.name, "age" -> p.age))

  implicit val titleToLog: ToLog[Title] = ToLog.create("title", t => ToValue(t.raw))

  implicit val authorToLog: ToLog[Author] = ToLog.create("author", a => ToValue(a.raw))

  implicit val categoryToLog: ToLog[Category] = ToLog.create("category", c => ToValue(c.raw))

  implicit val currencyToLog: ToLog[Currency] = ToLog.create("currency", currency => ToValue(currency.getCurrencyCode))

  implicit val priceToLog: ToLog[Price] = ToLog.create("price", price => ToObjectValue(price.currency, "amount" -> price.amount))

  implicit val bookToLog: ToLog[Book] = ToLog.create("book", book => ToObjectValue(book.title, book.category, book.author, book.price))

  // Says we want a toString of $8.95 in a message template for a price
  implicit val priceToStringValue: ToStringFormat[Price] = (price: Price) => Value.string(price.toString)

  implicit val creditCardToName: ToName[CreditCard] = ToName.create("credit_card")
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

trait MyFieldBuilder extends PresentationFieldBuilder with Logging {
  implicit val personToValue: ToObjectValue[Person] = { (person: Person) =>
    ToObjectValue(
      keyValue("name", ToValue(person.name)),
      keyValue("age", ToValue(person.age))
    )
  }

  implicit val govtToValue: ToObjectValue[Government] = { (govt: Government) =>
    ToObjectValue(
      keyValue("name", ToValue(govt.name)),
      keyValue("debt", ToValue(govt.debt))
    )
  }
}

object MyFieldBuilder extends MyFieldBuilder
