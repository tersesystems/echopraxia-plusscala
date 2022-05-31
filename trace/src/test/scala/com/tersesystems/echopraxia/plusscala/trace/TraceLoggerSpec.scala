package com.tersesystems.echopraxia.plusscala.trace

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.tersesystems.echopraxia.plusscala.api.{Condition, Level, LoggingContext}
import org.scalatest.{Assertion, BeforeAndAfterEach}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers

import java.util
import scala.jdk.CollectionConverters._

class TraceLoggerSpec  extends AnyFunSpec with BeforeAndAfterEach with Matchers {
  trait SimpleTraceFieldBuilder extends DefaultTracingFieldBuilder
  object SimpleTraceFieldBuilder extends SimpleTraceFieldBuilder

  private val logger = TraceLoggerFactory.getLogger(getClass)

  private def doSomething: String = logger.trace {
    "I return string"
  }

  describe("simple") {
    it("should enter and exit") {
      doSomething
      logsContain("doSomething")
      logsContain("tag=entry")
      logsContain("tag=exit")
      logsContain("I return string")
    }
  }

  // XXX test conditions
  // XXX test never logger
  // XXX test customization

  private def logsContain(message: String): Assertion = {
    val listAppender: ListAppender[ILoggingEvent] = getListAppender
    val list: util.List[ILoggingEvent]            = listAppender.list
    list.asScala.exists { event => event.getFormattedMessage.contains(message) } must be(true)
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
