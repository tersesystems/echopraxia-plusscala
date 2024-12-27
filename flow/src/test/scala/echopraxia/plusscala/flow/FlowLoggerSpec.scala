package echopraxia.plusscala.flow

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import echopraxia.plusscala.logging.api.Condition
import org.scalatest.Assertion
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers

import java.util
import scala.jdk.CollectionConverters._
import scala.util.Try

class FlowLoggerSpec extends AnyFunSpec with BeforeAndAfterEach with Matchers {

  private val logger = FlowLoggerFactory.getLogger(getClass)

  describe("simple") {
    it("should enter and exit") {
      def doSomething: String = logger.trace {
        "I return string"
      }
      doSomething

      logsContain("entry")
      logsContain("exit")
      logsContain("I return string")
    }

    it("should enter and throw") {
      def divideByZero: Int = logger.trace {
        1 / 0
      }

      Try(divideByZero)
      logsContain("entry")
      logsContain("throwing")
    }

    it("should not log if disabled") {
      def noLogging: String = logger.trace(Condition.never) {
        "I do not log"
      }

      noLogging
      logsNotContain("noLogging")
    }

    it("should log unit") {
      def logging: Unit = logger.trace {
        println("hello")
      }

      logging

      logsContain("entry")
      logsContain("exit")
    }
  }

  private def logsContain(message: String): Assertion = {
    val listAppender: ListAppender[ILoggingEvent] = getListAppender
    val list: util.List[ILoggingEvent]            = listAppender.list
    list.asScala.exists { event => event.getFormattedMessage.contains(message) } must be(true)
  }

  private def logsNotContain(message: String): Assertion = {
    val listAppender: ListAppender[ILoggingEvent] = getListAppender
    val list: util.List[ILoggingEvent]            = listAppender.list
    list.asScala.exists { event => event.getFormattedMessage.contains(message) } must be(false)
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
}
