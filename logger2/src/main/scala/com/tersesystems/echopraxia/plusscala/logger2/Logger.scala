package com.tersesystems.echopraxia.plusscala.logger2

import com.tersesystems.echopraxia.api.FieldBuilderResult.list
import com.tersesystems.echopraxia.api.{Field, FieldBuilderResult}
import com.tersesystems.echopraxia.plusscala.api.Level._
import com.tersesystems.echopraxia.plusscala.api._
import com.tersesystems.echopraxia.spi.{CoreLogger, CoreLoggerFactory}

import scala.collection.JavaConverters.seqAsJavaListConverter

import sourcecode._
import com.tersesystems.echopraxia.api.Value
import java.util.Collections

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
class Logger(val core: CoreLogger) {

  def name: String = core.getName()

  def withFields(fields: => Seq[Field]): Logger = {
    new Logger(core.withFields((_: PresentationFieldBuilder) => FieldBuilderResult.list(fields.asJava), PresentationFieldBuilder))
  }

  def withCondition(condition: Condition): Logger = new Logger(core.withCondition(condition.asJava))

  abstract class LoggerMethod(level: Level) {
    def enabled: Boolean = core.isEnabled(level.asJava)

    def apply(message: String)(implicit line: Line, file: File, enclosing: Enclosing): Unit               = handle(level, message)
    def apply(message: String, f1: => Field)(implicit line: Line, file: File, enclosing: Enclosing): Unit = handle(level, message, f1)
    def apply(message: String, f1: => Field, f2: => Field)(implicit line: Line, file: File, enclosing: Enclosing): Unit =
      handle(level, message, f1 ++ f2)
    def apply(message: String, f1: => Field, f2: => Field, f3: => Field)(implicit line: Line, file: File, enclosing: Enclosing): Unit =
      handle(level, message, f1 ++ f2 ++ f3)
    def apply(message: String, f1: => Field, f2: => Field, f3: => Field, f4: => Field)(implicit line: Line, file: File, enclosing: Enclosing): Unit =
      handle(level, message, f1 ++ f2 ++ f3 ++ f4)

    def apply()(implicit line: Line, file: File, enclosing: Enclosing): Unit                                         = apply("")
    def apply(f1: => Field)(implicit line: Line, file: File, enclosing: Enclosing): Unit                             = apply("{}", f1)
    def apply(f1: => Field, f2: => Field)(implicit line: Line, file: File, enclosing: Enclosing): Unit               = apply("{} {}", f1, f2)
    def apply(f1: => Field, f2: => Field, f3: => Field)(implicit line: Line, file: File, enclosing: Enclosing): Unit = apply("{} {} {}", f1, f2, f3)
    def apply(f1: => Field, f2: => Field, f3: => Field, f4: => Field)(implicit line: Line, file: File, enclosing: Enclosing): Unit =
      apply("{} {} {} {}", f1, f2, f3, f4)

    // variadic params don't take call by name  :-(
    def v(fields: Field*)(implicit line: Line, file: File, enclosing: Enclosing): Unit =
      handle(level, ("{} " * fields.size).trim, list(fields.toArray))

    private def handle(level: Level, message: String)(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      handle(level, message, FieldBuilderResult.empty())
    }

    private def handle(level: Level, message: String, f: => FieldBuilderResult)(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      import scala.compat.java8.FunctionConverters._

      val f1: PresentationFieldBuilder => FieldBuilderResult = _ => f
      core.log(level.asJava, () => Collections.singletonList(sourceCodeField), message, f1.asJava, PresentationFieldBuilder)
    }

    private def sourceCodeField(implicit line: Line, file: File, enc: Enclosing): Field = {
      Field
        .keyValue(
          "sourcecode",
          Value.`object`(
            Field.keyValue("file", Value.string(file.value)),
            Field.keyValue("line", Value.number(line.value: java.lang.Integer)),
            Field.keyValue("enclosing", Value.string(enc.value))
          )
        )
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
