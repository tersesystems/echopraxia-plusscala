package com.tersesystems.echopraxia.logger2

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.tersesystems.echopraxia.api.Field
import com.tersesystems.echopraxia.plusscala.api._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers

import java.time.Instant
import java.util

class ScalaLoggerSpec extends AnyFunSpec with BeforeAndAfterEach with Matchers {

  private val logger = LoggerFactory.getLogger(getClass, MyFieldBuilder)

  describe("withCondition") {

    it("should use a scala withCondition") {
      val condition: Condition = (level: Level, context: LoggingContext) => true
      logger.withCondition(condition)
    }
  }

  describe("withFields") {
    it("should use withFields") {
      logger.withFields(fb => fb.string("derp", "herp"))
    }
  }

  describe("results") {

    it("should log a seq automatically") {
      val seq = Array("one", "two", "three")
      logger.debug(
        "single tuple {}",
        fb => fb.list(seq.zipWithIndex.map { case (value, i) => fb.string(i.toString, value) })
      )
      matchThis("single tuple {}")
    }

  }

  describe("tuple") {

    it("should log using a single tuple") {
      logger.debug("single tuple {}", _.string("foo" -> "bar"))
      matchThis("single tuple {}")
    }

    it("should log using multiple tuples using an import") {
      logger.debug(
        "multiple tuples {}",
        fb => {
          import fb._
          fb.list(string("foo" -> "bar"), string("k2" -> "v2"))
        }
      )

      matchThis("multiple tuples {}")
    }
  }

  describe("seq") {

    it("should log using a Seq of String") {
      val seq = Seq("one", "two", "three")
      logger.debug("seq {}", _.array("someSeq", seq))

      matchThis("seq {}")
    }

    it("should log using a Seq of Instant") {
      logger.debug(
        "seq {}",
        { fb =>
          import fb._
          val seq: List[Instant] = List(Instant.now(), Instant.now(), Instant.now())
          (fb.array("someSeq", seq))
        }
      )

      matchThis("seq {}")
    }

    it("should log using a Seq of Instant in tuple style") {
      logger.debug(
        "seq {}",
        { fb =>
          import fb._
          val seq: List[Instant] = List(Instant.now(), Instant.now(), Instant.now())
          (fb.array("someSeq" -> seq))
        }
      )

      matchThis("seq {}")
    }

    it("should log using a Seq of boolean") {
      logger.debug(
        "seq {}",
        { fb =>
          import fb._
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
        fb => {
          import fb._
          (instant("iso_timestamp" -> Instant.now()))
        }
      )
      matchThis("mapping time = {}")
    }

    it("should log a person as a value or object value") {
      logger.debug(
        "person1 {} person2 {}",
        fb => {
          import fb._
          fb.list(
            fb.person("person1" -> Person("Eloise", 1)),
            fb.obj("person2"    -> Person("Eloise", 1))
          )
        }
      )
      matchThis("person1 {} person2 {}")
    }

    it("should work with a map with different values") {
      logger.info(
        "testing {}",
        fb => {
          import fb._
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
      logger.debug(
        "person = {}",
        fb => {
          import fb._
          obj("owner", person("person" -> Person("Eloise", 1)))
        }
      )

      matchThis("person = {}")
    }

    it("should custom with tuples") {
      logger.debug(
        "list of tuples = {}",
        fb => {
          import fb._
          list(
            person("owner"          -> Person("Eloise", 1)),
            instant("iso_timestamp" -> Instant.now()),
            string("foo"            -> "bar"),
            bool("something"        -> true)
          )
        }
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
