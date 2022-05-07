package com.tersesystems.echopraxia.plusscala

import com.tersesystems.echopraxia.api.Field
import com.tersesystems.echopraxia.plusscala.api.FieldBuilder

import java.time.Instant

object Main {

  private val logger = LoggerFactory.getLogger.withFieldBuilder(MyFieldBuilder)

  case class Book(category: String, author: String, title: String, price: Double)

  def main(args: Array[String]): Unit = {

    val refBook = Book("reference", "Nigel Rees", "Sayings of the Century", 8.95)
    logger.info(
      "{}",
      fb => {
        import fb._
        fb.obj("store", fb.array("book", Seq(refBook)))
      }
    )

    logger.info(
      "{}",
      fb => {
        fb.book("thisbook", refBook)
      }
    )

    logger.info("array of throwables {}", fb => fb.array("ex" -> Seq(new RuntimeException())))
  }

  trait MyFieldBuilder extends FieldBuilder {
    implicit val instantToStringValue: ToValue[Instant] = (t: Instant) => ToValue(t.toString)

    def instant(name: String, i: Instant): Field = keyValue(name, ToValue(i))

    implicit val bookToObjectValue: ToObjectValue[Book] = { book =>
      ToObjectValue(
        string("category", book.category),
        string("author", book.author),
        string("title", book.title),
        number("price", book.price)
      )
    }

    def book(name: String, i: Book): Field = keyValue(name, ToValue(i))

    implicit def mapToObjectValue[V: ToValue]: ToObjectValue[Map[String, V]] = m => ToObjectValue(m.map(t => keyValue(t)))

  }

  object MyFieldBuilder extends MyFieldBuilder

}
