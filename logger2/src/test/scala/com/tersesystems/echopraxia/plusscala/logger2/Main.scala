package com.tersesystems.echopraxia.plusscala.logger2

import com.tersesystems.echopraxia.api.Level
import com.tersesystems.echopraxia.plusscala.api.{Condition, _}
import com.tersesystems.echopraxia.spi.FieldConstants

import java.time.Instant

object Main {

  private val logger = LoggerFactory.getLogger(BookFieldBuilder)

  val infoOrHigherCondition: Condition = Condition((level, _) => level >= Level.INFO)

  val fooCondition: Condition = Condition(_.fields.exists(_.name == "foo"))

  val infoAndFoo: Condition = infoOrHigherCondition and fooCondition

  def main(args: Array[String]): Unit = {
    val refBook = Book("reference", "Nigel Rees", "Sayings of the Century", 8.95)
    logger.info("{}", _("book" -> refBook))
    logger.info("{}", _("instant" -> Instant.now()), _("book" -> refBook))

    val e = new IllegalStateException()
    logger.error("{}", _(e))

    logger.debug(infoOrHigherCondition, "INFO message")
  }

}

case class Book(category: String, author: String, title: String, price: Double)

case class CreditCard(number: String, expirationDate: String)


trait BookFieldBuilder extends PresentationFieldBuilder {

  implicit val instantToStringValue: ToValue[Instant] = (t: Instant) => ToValue(t.toString)

  implicit val bookToObjectValue: ToObjectValue[Book] = { book =>
    ToObjectValue(
      keyValue("category", book.category),
      keyValue("author", book.author),
      keyValue("title", book.title),
      keyValue("price", book.price)
    )
  }

  def apply[V: ToValue](tuple: (Name, V)) = keyValue(tuple)
  def apply[V: ToValue](key: Name, value: V) = keyValue(key, value)
  def apply(e: Throwable) = keyValue(FieldConstants.EXCEPTION, e)
}

object BookFieldBuilder extends BookFieldBuilder

