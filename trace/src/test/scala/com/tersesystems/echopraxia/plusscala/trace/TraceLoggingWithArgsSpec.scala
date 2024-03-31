package com.tersesystems.echopraxia.plusscala.trace

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.tersesystems.echopraxia.api.{FieldBuilderResult, Value}
import com.tersesystems.echopraxia.plusscala.api.{Condition, FieldBuilder, SourceCode}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{Assertion, BeforeAndAfterEach}
import sourcecode.{Args, Enclosing, File, Line}

import java.util
import scala.jdk.CollectionConverters._
import scala.util.Try

class TraceLoggingWithArgsSpec extends AnyFunSpec with BeforeAndAfterEach with Matchers {

  private val logger = TraceLoggerFactory.getLogger(getClass)

  describe("simple") {
    it("should enter and exit") {
      def doSomething: String = logger.trace {
        "I return string"
      }
      doSomething

      logsContain("doSomething")
      logsContain("entry:")
      logsContain("exit:")
      logsContain("I return string")
    }

    it("should enter and throw") {
      def divideByZero: Int = logger.trace {
        1 / 0
      }

      Try(divideByZero)
      logsContain("divideByZero")
      logsContain("entry:")
      logsContain("throwing:")
    }

    it("should not log if disabled") {
      def noLogging: String = logger.trace(Condition.never) {
        "I do not log"
      }

      noLogging
      logsNotContain("noLogging")
    }
  }

  describe("custom") {
    it("should enter and exit") {
      val customLogger = logger.withFieldBuilder(SimpleTraceFieldBuilder)
      def doSomething: String = customLogger.trace {
        "I return string"
      }

      doSomething

      logsContain("doSomething")
      logsContain("entering: method=")
      logsContain("exiting: method=")
      logsContain("I return string")
    }

    it("should enter and throw") {
      val customLogger = logger.withFieldBuilder(SimpleTraceFieldBuilder)
      def divideByZero: Int = customLogger.trace {
        1 / 0
      }

      Try(divideByZero)
      logsContain("divideByZero")
      logsContain("entering: method=")
      logsContain("throwing: method=")
    }

    it("should not log if disabled") {
      // if we define a logger inline here we can crash Scala 2.12 :-D
      def noCustomLogging: String = customLogger.trace(Condition.never) {
        "I do not log"
      }

      noCustomLogging
      logsNotContain("noLogging")
    }
  }

  private val customLogger = logger.withFieldBuilder(SimpleTraceFieldBuilder)

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

  trait SimpleTraceFieldBuilder extends FieldBuilder with TraceFieldBuilder {
    override def sourceFields(implicit line: Line, file: File, enc: Enclosing, args: Args): SourceFields =
      new DefaultSourceFields(SourceCode(line, file, enc), args)

    override def entering(sourceFields: SourceFields): FieldBuilderResult = {
      list(sourceFields.argumentFields)
    }

    override def exiting(sourceFields: SourceFields, retValue: Value[_]): FieldBuilderResult = {
      list(sourceFields.argumentFields :+ value("traceResult", retValue))
    }

    override def throwing(sourceFields: SourceFields, ex: Throwable): FieldBuilderResult = {
      list(sourceFields.argumentFields :+ exception(ex))
    }
  }

  object SimpleTraceFieldBuilder extends SimpleTraceFieldBuilder {
    override def enteringTemplate: String = "entering: method={} args={}"

    override def exitingTemplate: String = "exiting: method={} args={} result={}"

    override def throwingTemplate: String = "throwing: method={} args={} exception={}"
  }

}
