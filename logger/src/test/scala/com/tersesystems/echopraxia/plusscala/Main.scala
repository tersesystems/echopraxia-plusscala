package com.tersesystems.echopraxia.plusscala

import com.tersesystems.echopraxia.api.{Field, Level}
import com.tersesystems.echopraxia.plusscala.api._

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

    logger.info("{}", _.keyValue("thisbook" -> refBook))

    logger.info("testing {} {}", fb => fb.list(
      //fb.keyValue("foo", fb.book(refBook)),
      fb.keyValue("foo", refBook),
      // https://stackoverflow.com/questions/5598085/where-does-scala-look-for-implicits/5598107#5598107
      //fb.keyValue("foo" -> refBook),
      //fb.keyValue("foo", refBook),
    ))
    logger.info("{}", _.keyValue("instant", Instant.now()))

    logger.info("array of throwables {}", fb => fb.array("ex" -> Seq(new RuntimeException())))

    logger.debug(infoOrHigherCondition, "INFO message")
  }

  val infoOrHigherCondition: Condition = Condition((level, _) => level >= Level.INFO)

  val fooCondition: Condition = Condition(_.fields.exists(_.name == "foo"))

  val infoAndFoo: Condition = infoOrHigherCondition xor fooCondition

  trait MyFieldBuilder extends FieldBuilder {
    implicit val instantToStringValue: ToValue[Instant] = (t: Instant) => ToValue(t.toString)

    def instant(name: String, i: Instant): Field = keyValue(name, ToValue(i))

    implicit val bookToObjectValue: ToObjectValue[Book] = { book =>
      ToObjectValue(
        keyValue("category", book.category),
        keyValue("author", book.author),
        keyValue("title", book.title),
        keyValue("price", book.price)
      )
    }

    implicit def mapToObjectValue[V: ToValue]: ToObjectValue[Map[String, V]] = m => ToObjectValue(m.map(keyValue(_)))
  }

  object MyFieldBuilder extends MyFieldBuilder

}
