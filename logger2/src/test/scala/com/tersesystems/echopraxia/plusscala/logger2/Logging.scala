package com.tersesystems.echopraxia.plusscala.logger2

import com.tersesystems.echopraxia.api.Value

import java.util.{Currency, UUID}
import scala.concurrent.Future
import scala.reflect.{ClassTag, classTag}
import com.tersesystems.echopraxia.plusscala.api.ToName
import com.tersesystems.echopraxia.plusscala.api.ToValueAttribute
import com.tersesystems.echopraxia.plusscala.api.ToStringFormat

// Each package can add its own mappings
trait Logging extends LoggingBase {

  // use the class name as the name here
  // Elasticsearch doesn't like dots in field names so this doesn't go in the framework.
  implicit val uuidToLog: ToLog[UUID] = ToLog.create(classOf[UUID].getName, uuid => ToValue(uuid.toString))

  // Use class name for future
  implicit def futureToName[T: ClassTag]: ToName[Future[T]] = _ => s"future[${classTag[T].runtimeClass.getName}]"

  implicit val personToLog: ToLog[Person] = ToLog.create("person", p => ToObjectValue("firstName" -> p.firstName, "lastName" -> p.lastName))

  implicit val titleToLog: ToLog[Title] = ToLog.create("title", t => ToValue(t.raw))

  implicit val authorToLog: ToLog[Author] = ToLog.create("author", a => ToValue(a.raw))

  implicit val categoryToLog: ToLog[Category] = ToLog.create("category", c => ToValue(c.raw))

  implicit val currencyToLog: ToLog[Currency] = ToLog.create("currency", currency => ToValue(currency.getCurrencyCode))

  implicit val priceToLog: ToLog[Price] = ToLog.create("price", price => ToObjectValue(price.currency, "amount" -> price.amount))

  implicit val bookToLog: ToLog[Book] = ToLog.create("book", book => ToObjectValue(book.title, book.category, book.author, book.price))

  // Says we want a toString of $8.95 in a message template for a price
  implicit val priceToStringValue: ToStringFormat[Price] = (price: Price) => Value.string(price.toString)

  // everyone wants different things out of maps, so implementing that
  // is up to the individual application
  implicit def mapToValue[TV: ToValue](implicit va: ToValueAttribute[TV]): ToValue[Map[String, TV]] = { v =>
    val value: Seq[Value.ObjectValue] = v.map { case (k, v) =>
      ToObjectValue("key" -> k, "value" -> v)
    }.toSeq
    ToArrayValue(value)
  }

  // Echopraxia takes a bit more work the more heterogeneous the input gets.
  // For example, to pass through random tuples, you need to map it to an object
  implicit def tupleToValue[TVK: ToValue, TVV: ToValue](implicit va: ToValueAttribute[Tuple2[TVK, TVV]]): ToValue[Tuple2[TVK, TVV]] = { case (k, v) =>
    ToObjectValue("key" -> k, "value" -> v)
  }
}
