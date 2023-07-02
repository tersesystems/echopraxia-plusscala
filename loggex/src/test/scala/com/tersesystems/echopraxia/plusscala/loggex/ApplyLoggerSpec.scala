package com.tersesystems.echopraxia.plusscala.loggex

import com.tersesystems.echopraxia.api.{Field, FieldBuilderResult}
import com.tersesystems.echopraxia.plusscala.api.FieldBuilder
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers

import java.time.Instant
import java.time.Instant.now
import scala.compat.java8.FunctionConverters.enrichAsJavaFunction

class ApplyLoggerSpec extends AnyFunSpec with BeforeAndAfterEach with Matchers {

  trait MyFieldBuilder extends FieldBuilder {
    // Instant type
    implicit val instantToStringValue: ToValue[Instant] = (t: Instant) => ToValue(t.toString)

    def instant(name: String, i: Instant): Field = keyValue(name, ToValue(i))
    def apply[T: ToValue](value: T): Field = keyValue("derp", ToValue(value))

    def instant(i: Instant): Field = keyValue("instant", ToValue(i))

    def instant(tuple: (String, Instant)): Field = keyValue(tuple)
  }

  implicit class EnrichedLogMethod[FB <: MyFieldBuilder.type](lm: Loggex[FB]#LogMethod) {
    import lm.level
    import lm.support._
    def apply[T](message: String, v: T)(implicit ev: lm.support.fieldBuilder.ToValue[T]): Unit = {
      val f: FB => FieldBuilderResult = fb => fb.keyValue("derp", fieldBuilder.ToValue(v))
      core.log(level, message, f.asJava, fieldBuilder)
    }
  }

  object MyFieldBuilder extends MyFieldBuilder

  describe("basic logging") {

    it("should log") {
      val logger = LoggexFactory.getLogger(getClass.getName, MyFieldBuilder)
      logger.debug("foo {}", now)
    }
  }
}
