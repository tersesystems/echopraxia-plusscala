package com.tersesystems.echopraxia.plusscala

import com.tersesystems.echopraxia.api.{Field, Level}
import com.tersesystems.echopraxia.spi.{CoreLoggerFactory, DefaultField}
import com.tersesystems.echopraxia.plusscala.api._
import enumeratum.EnumEntry._
import enumeratum._

import java.time.Instant

object Main {

  sealed trait Names extends EnumEntry with Snakecase

  object Names extends Enum[Names] {

    /*
     `findValues` is a protected method that invokes a macro to find all `Greeting` object declarations inside an `Enum`

     You use it to implement the `val values` member
     */
    val values = findValues

    case object ExpirationDate extends Names

    case object Number     extends Names
    case object CreditCard extends Names

    case object Book extends Names

    case object Instant  extends Names
    case object Title    extends Names
    case object Author   extends Names
    case object Category extends Names
    case object Price    extends Names

    case object Store extends Names
  }

  trait CreditCardFieldBuilder extends ArgsFieldBuilder with HasName {
    override type FieldType = DefaultField
    protected val fieldClass: Class[DefaultField] = classOf[DefaultField]

    import Names._
    type Name = Names

    type Id[A] = A
    implicit def narrow[T <: Singleton](t: T): Id[T] = t

    def keyValue[V: ToValue](key: Name, value: V): DefaultField = Field.keyValue(key.entryName, ToValue(value), fieldClass)

    // ------------------------------------------------------------------
    // value

    def value[V: ToValue](key: Name, value: V): DefaultField = Field.value(key.entryName, ToValue(value), fieldClass)

    implicit def creditCardToValue(implicit cap: Sensitive = Censored): ToValue[CreditCard] = cc => {
      ToObjectValue(
        sensitiveKeyValue(Number, cc.number),
        keyValue(ExpirationDate, cc.expirationDate)
      )
    }

    def sensitiveKeyValue(name: Names, value: String)(implicit cap: Sensitive = Censored): Field = {
      cap match {
        case Censored =>
          keyValue(name, "[CENSORED]")
        case Explicit =>
          keyValue(name, value)
      }
    }
  }

  trait MyFieldBuilder extends CreditCardFieldBuilder {
    override protected val fieldClass: Class[DefaultField] = classOf[DefaultField]

    implicit val instantToStringValue: ToValue[Instant] = (t: Instant) => ToValue(t.toString)

    implicit val bookToObjectValue: ToObjectValue[Book] = { book =>
      ToObjectValue(
        keyValue(Names.Category, book.category),
        keyValue(Names.Author, book.author),
        keyValue(Names.Title, book.title),
        keyValue(Names.Price, book.price)
      )
    }

    implicit def mapToObjectValue[N <: Name, V: ToValue]: ToObjectValue[Map[N, V]] = m =>
      ToObjectValue(m.map { case (k, v) =>
        keyValue(k, v)
      })
  }

  object MyFieldBuilder extends MyFieldBuilder

  private val logger = Logger(CoreLoggerFactory.getLogger("", getClass), MyFieldBuilder)

  case class Book(category: String, author: String, title: String, price: Double)

  case class CreditCard(number: String, expirationDate: String)

  def main(args: Array[String]): Unit = {

    implicit val sensitive = Explicit
    val creditCard         = CreditCard("4111 1111 1111 1111", "04/23")
    logger.info("{}", _.keyValue(Names.CreditCard, creditCard))

    val refBook = Book("reference", "Nigel Rees", "Sayings of the Century", 8.95)
    logger.info(
      "{}",
      fb => {
        import fb._
        fb.obj(Names.Store, fb.array(Names.Book, Seq(refBook)))
      }
    )

    logger.info("{}", _.keyValue(Names.Book, refBook))

    logger.info("{}", _.keyValue(Names.Instant, Instant.now()))

    logger.debug(infoOrHigherCondition, "INFO message")
  }

  val infoOrHigherCondition: Condition = Condition((level, _) => level >= Level.INFO)

  val fooCondition: Condition = Condition(_.fields.exists(_.name == "foo"))

  val infoAndFoo: Condition = infoOrHigherCondition and fooCondition

  sealed trait Sensitive

  case object Censored extends Sensitive

  case object Explicit extends Sensitive

}
