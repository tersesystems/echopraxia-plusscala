package com.tersesystems.echopraxia.plusscala.dump

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

class DumpLoggerSpec extends AnyFunSpec with BeforeAndAfterEach with Matchers {

  private val logger = DumpLoggerFactory.getLogger(getClass).withFieldBuilder(MyFieldBuilder)

  describe("logging") {

    it("should log a person") {
      val person = Person("thisperson", 13)
      logger.info(person)

      matchThis("person={thisperson, 13}")
    }
  }

  private def matchThis(message: String) = {
    val listAppender: ListAppender[ILoggingEvent] = getListAppender
    val list: util.List[ILoggingEvent]            = listAppender.list
    val event: ILoggingEvent                      = list.get(0)
    event.getFormattedMessage must be(message)
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

case class Person(name: String, age: Int)
