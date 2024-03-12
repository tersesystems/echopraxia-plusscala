package com.tersesystems.echopraxia.plusscala.logger2

import com.tersesystems.echopraxia.api.FieldBuilderResult.list
import com.tersesystems.echopraxia.api.{Field, FieldBuilderResult}
import com.tersesystems.echopraxia.plusscala.api.Level._
import com.tersesystems.echopraxia.plusscala.api._
import com.tersesystems.echopraxia.spi.{CoreLogger, CoreLoggerFactory}

import scala.collection.JavaConverters.seqAsJavaListConverter

/**
 * This logger does not expose a field buildre and instead relies on a complimentary trait to implicitly convert arguments into Fields.
 *
 * {{{
 * class MyClass extends Logging {
 *   private val logger = LoggerFactory.getLogger(classOf[MyClass])
 *
 *   logger.info("foo" -> bar) // Logging will convert this to a field.
 * }
 * }}}
 *
 * @param core
 */
class Logger(core: CoreLogger) {

  // XXX this needs to be extended for withCondition etc

  def withFields(fields: => Seq[Field]): Logger = {
    new Logger(core.withFields((_: PresentationFieldBuilder) => FieldBuilderResult.list(fields.asJava), PresentationFieldBuilder))
  }

  def withCondition(condition: Condition): Logger = new Logger(core.withCondition(condition.asJava))

  abstract class LoggerMethod(level: Level) {
    def enabled: Boolean = core.isEnabled(level.asJava)

    def apply(message: String): Unit                                                         = core.log(level.asJava, message)
    def apply(message: String, f1: => Field): Unit                                           = handle(level, message, f1)
    def apply(message: String, f1: => Field, f2: => Field): Unit                             = handle(level, message, f1 ++ f2)
    def apply(message: String, f1: => Field, f2: => Field, f3: => Field): Unit               = handle(level, message, f1 ++ f2 ++ f3)
    def apply(message: String, f1: => Field, f2: => Field, f3: => Field, f4: => Field): Unit = handle(level, message, f1 ++ f2 ++ f3 ++ f4)

    def apply(): Unit                                                       = core.log(level.asJava, "")
    def apply(f1: => Field): Unit                                           = apply("{}", f1)
    def apply(f1: => Field, f2: => Field): Unit                             = apply("{} {}", f1, f2)
    def apply(f1: => Field, f2: => Field, f3: => Field): Unit               = apply("{} {} {}", f1, f2, f3)
    def apply(f1: => Field, f2: => Field, f3: => Field, f4: => Field): Unit = apply("{} {} {} {}", f1, f2, f3, f4)

    // variadic params don't take call by name  :-(
    def v(fields: Field*): Unit = handle(level, ("{} " * fields.size).trim, list(fields.toArray))

    private def handle(level: Level, message: String, f: => FieldBuilderResult): Unit = {
      import scala.compat.java8.FunctionConverters._

      val f1: PresentationFieldBuilder => FieldBuilderResult = _ => f
      core.log(level.asJava, message, f1.asJava, PresentationFieldBuilder)
    }
  }

  object info extends LoggerMethod(INFO)

  object debug extends LoggerMethod(DEBUG)

  object trace extends LoggerMethod(TRACE)

  object warn extends LoggerMethod(WARN)

  object error extends LoggerMethod(ERROR)
}

object LoggerFactory {
  private val FQCN = classOf[Logger].getName

  def getLogger(clazz: Class[_]): Logger = {
    val core: CoreLogger = CoreLoggerFactory.getLogger(FQCN, clazz)
    new Logger(core)
  }

  def getLogger(name: String): Logger = {
    val core: CoreLogger = CoreLoggerFactory.getLogger(FQCN, name)
    new Logger(core)
  }
}
