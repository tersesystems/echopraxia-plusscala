package com.tersesystems.echopraxia.plusscala

import com.tersesystems.echopraxia.spi.{CoreLogger, CoreLoggerFactory}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers

import com.tersesystems.echopraxia.api.{Level => JLevel}

class ApplyLoggerSpec extends AnyFunSpec with BeforeAndAfterEach with Matchers {

  private val coreLogger = CoreLoggerFactory.getLogger("", classOf[ApplyLoggerSpec])

  describe("basic logging") {

    it("should log") {
      val logger = new SimpleLogger(getClass.getName, coreLogger, MyFieldBuilder)
      logger.debug("foo")
      logger.debug("foo", fb => fb.list())
    }

    it("should take extra parameters") {
      val logger = new ExtraLogger(getClass.getName, coreLogger, MyFieldBuilder)
      logger.debug(new Exception())
    }
  }
}

trait ExtraApplyMethods[FB] extends ApplyMethods[FB] {
  def apply(t: Throwable): Unit
}

class ExtraLogger[FB](name: String, core: CoreLogger, fieldBuilder: FB) extends SimpleLogger(name, core, fieldBuilder) {
  override type MethodType = ExtraApplyMethods[FB]

  override protected def createMethod(level: JLevel): MethodType = new DefaultApplyMethods(level) with ExtraApplyMethods[FB] {
    override def apply(t: Throwable): Unit = handleMessage(level, "derp")
  }
}