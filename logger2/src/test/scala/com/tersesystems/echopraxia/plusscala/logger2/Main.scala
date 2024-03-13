package com.tersesystems.echopraxia.plusscala.logger2

import com.tersesystems.echopraxia.api.Field

import java.util.{Currency, UUID}
import scala.concurrent.Future

object Main {
  def main(args: Array[String]): Unit = {
    val printer = new Printer()
    printer.print()
  }
}

class Printer extends Logging {
  private val logger: Logger = LoggerFactory.getLogger(getClass)

  def print(): Unit = {
    val person1 = Person("Person1", "Last Name")
    val person2 = Person("Person2", "Last Name")

    // This uses the "personToField" mapping
    logger.info("template shows {}", person1)
    logger.info(person1)

    // Can define custom mapping with tuples
    logger.info("person1" -> person1)
    logger.info("person1" -> person1, "person2" -> person2)

    // Options work out of the box
    val optPerson: Option[Person] = Option(person1)
    logger.info("optPerson" -> optPerson)

    // XXX this doesn't work in 2.12 but works in 2.13 and 3
    //logger.info("optPerson" -> None)

    // As does either
    logger.info("eitherPerson" -> Left(person1))

    // And so do lists
    logger.info("people" -> Seq(person1, person2))

    // and maps
    logger.info("people" -> Map("person1" -> person1, "person2" -> person2))

    // Echopraxia takes a bit more work the more heterogeneous the input gets.
    // For example, to pass through random tuples, you need to map it to an object
    implicit def tupleToValue[TVK: ToValue, TVV: ToValue](implicit va: ToValueAttribute[Tuple2[TVK, TVV]]): ToValue[Tuple2[TVK, TVV]] = {
      case (k, v) => ToObjectValue("_1" -> k, "_2" -> v)
    }
    // XXX This doesn't work in 2.12, works in 2.13 and 3
    //logger.info("tuple" -> (1, person1))

    // support for exceptions
    logger.error(new IllegalStateException())
    if (logger.info.enabled) {
      // this will take any number of fields but is less efficient
      // as it is not call-by-name
      logger.info.v("p1" -> person1, "p2" -> person2, "p3" -> person1)
    }

    // Complex objects are no problem
    val book1 = Book(
      Category("reference"),
      Author("Nigel Rees"),
      Title("Sayings of the Century"),
      Price(amount = 8.95, currency = Currency.getInstance("USD"))
    )
    logger.info(book1)

    // If you want to render fields as an object, you can use ToObjectValue
    logger.info("object" -> ToObjectValue(book1, person1)) // object={book={}, person={}}

    // For heterogeneous fields you'll need to use `Seq[Field]` explicitly, or use info.v as seen below
    logger.info("object" -> Seq[Field](book1, person1)) // object=[book={}, person={}]

    // For heterogeneous values you'll want to specify Seq[Value[_]] to give implicit conversion some clues
    logger.info("oneTrueString" -> Seq(ToValue(1), ToValue(true), ToValue("string")))

    // You can also use "withFields" to render JSON on every message (this will not show in line format)
    logger.withFields(Seq(book1, person1)).info("testing")

    // You can also use variadic method but best to wrap it in conditional
    if (logger.info.enabled) {
      // not call by name so it gets evaluated eagerly :-(
      logger.info.v(
        Category("reference"),
        Author("Nigel Rees"),
        Title("Sayings of the Century"),
        Price(amount = 8.95, currency = Currency.getInstance("USD")),
        person1 // add more than 4
      )
    }

    // Can also log using class name
    logger.info(UUID.randomUUID)

    // Logging futures is also possible, and can include names
    logger.info(Future.successful(true))
    logger.info(Future.successful("String"))
  }
}

import java.text.NumberFormat
import java.util.Currency

case class Book(category: Category, author: Author, title: Title, price: Price)

case class Price(amount: BigDecimal, currency: Currency) {
  override def toString: String = {
    val numberFormat = NumberFormat.getCurrencyInstance
    numberFormat.setCurrency(currency)
    numberFormat.format(amount)
  }
}

case class Category(raw: String) extends AnyVal
case class Author(raw: String)   extends AnyVal
case class Title(raw: String)    extends AnyVal

case class Person(firstName: String, lastName: String)
