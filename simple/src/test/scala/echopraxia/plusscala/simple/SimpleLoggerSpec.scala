package echopraxia.plusscala.simple

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import echopraxia.api.{Field, Value}
import echopraxia.plusscala.api.FieldBuilder
import echopraxia.plusscala.logging.api.Condition
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers

import java.time.Instant
import java.util

class SimpleLoggerSpec extends AnyFunSpec with BeforeAndAfterEach with Matchers {

  private val logger = LoggerFactory.getLogger(getClass)
  private val fb = MyFieldBuilder

  describe("withCondition") {

    it("should use a scala withCondition") {
      val condition: Condition = Condition.always
      logger.withCondition(condition)
    }
  }

  describe("withFields") {
    it("should use withFields in a function") {
      logger.withFields(() => fb.string("herp", "derp"))
    }

    it("should use withFields directly") {
      logger.withFields(fb.string("herp", "derp"))
    }
  }

  describe("results") {

    it("should log a seq automatically") {
      val seq = Array("one", "two", "three")
      logger.debug(
        "single tuple {}",
        seq.zipWithIndex.map { case (value, i) =>
          Field.keyValue(i.toString, Value.string(value))
        }*
      )
      matchThis("single tuple {}")
    }

  }

  describe("tuple") {

    it("should log using a single tuple") {
      logger.debug("single tuple {}", fb.string("foo" -> "bar"))
      matchThis("single tuple {}")
    }

    it("should log using multiple tuples using an import") {
      import fb.*

      logger.debug(
        "multiple tuples {}",
        string("foo" -> "bar"), string("k2" -> "v2")
      )

      matchThis("multiple tuples {}")
    }
  }

  describe("seq") {

    it("should log using a Seq of String") {
      val seq = Seq("one", "two", "three")
      logger.debug("seq {}", fb.array("someSeq", seq))

      matchThis("seq {}")
    }

    it("should log using a Seq of Instant") {
      logger.debug(
        "seq {}",
        { import fb.*
          val seq: List[Instant] = List(Instant.now(), Instant.now(), Instant.now())
          fb.array("someSeq", seq)
        }
      )

      matchThis("seq {}")
    }

    it("should log using a Seq of Instant in tuple style") {
      logger.debug(
        "seq {}",
        { import fb.*
          val seq: List[Instant] = List(Instant.now(), Instant.now(), Instant.now())
          fb.array("someSeq" -> seq)
        }
      )

      matchThis("seq {}")
    }

    it("should log using a Seq of boolean") {
      logger.debug(
        "seq {}",
        {
          import fb.*
          array("someSeq", Seq(true, false, true))
        }
      )

      matchThis("seq {}")
    }
  }

  describe("instant and person") {

    it("should log an instant as a string") {
      logger.debug(
        "mapping time = {}",
        {
          import fb.*
          (instant("iso_timestamp" -> Instant.now()))
        }
      )
      matchThis("mapping time = {}")
    }

    it("should log a person as a value or object value") {
      import fb.*
      logger.debug(
        "person1 {} person2 {}",
        fb.person("person1" -> Person("Eloise", 1)),
        fb.obj("person2"    -> Person("Eloise", 1))
      )
      matchThis("person1 {} person2 {}")
    }

    it("should work with a map with different values") {
      import fb.*

      logger.info(
        "testing {}",
        {
          val any = Map("int" -> 1, "str" -> "foo", "instant" -> Instant.now())
          val fields = any.map {
            case (k: String, v: String) =>
              string(k, v)
            case (k: String, v: Int) =>
              number(k, v)
            case (k: String, v: Instant) =>
              instant(k, v)
          }
          obj("foo", fields)
        }
      )
    }

    it("should log a person as an object") {
      import fb.*
      logger.debug(
        "person = {}",
        obj("owner", person("person" -> Person("Eloise", 1)))
      )

      matchThis("person = {}")
    }

    it("should custom with tuples") {
      import fb.*

      logger.debug(
        "list of tuples = {}",
        person("owner"          -> Person("Eloise", 1)),
        instant("iso_timestamp" -> Instant.now()),
        string("foo"            -> "bar"),
        bool("something"        -> true)
      )

      matchThis("list of tuples = {}")
    }

  }

  private def matchThis(message: String) = {
    val listAppender: ListAppender[ILoggingEvent] = getListAppender
    val list: util.List[ILoggingEvent]            = listAppender.list
    val event: ILoggingEvent                      = list.get(0)
    event.getMessage must be(message)
  }

  override def beforeEach(): Unit = {
    getListAppender.list.clear()
  }

  private def loggerContext: LoggerContext = {
    org.slf4j.LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
  }

  private def getListAppender: ListAppender[ILoggingEvent] = {
    loggerContext
      .getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
      .getAppender("LIST")
      .asInstanceOf[ListAppender[ILoggingEvent]]
  }

  trait MyFieldBuilder extends FieldBuilder {
    // Instant type
    implicit val instantToStringValue: ToValue[Instant] = (t: Instant) => ToValue(t.toString)
    def instant(name: String, i: Instant): Field        = keyValue(name, ToValue(i))
    def instant(tuple: (String, Instant)): Field        = keyValue(tuple)

    // Person type
    implicit val personToObjectValue: ToObjectValue[Person] = (p: Person) =>
      ToObjectValue(
        string("name", p.name),
        number("age", p.age)
      )
    def person(name: String, person: Person): Field = keyValue(name, person)
    def person(tuple: (String, Person)): Field      = keyValue(tuple)
  }

  object MyFieldBuilder extends MyFieldBuilder
}
