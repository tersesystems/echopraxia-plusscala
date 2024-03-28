package com.tersesystems.echopraxia.plusscala.fieldlogger

import com.tersesystems.echopraxia.api.Field
import com.tersesystems.echopraxia.api.FieldBuilderResult
import com.tersesystems.echopraxia.api.FieldBuilderResult.list
import com.tersesystems.echopraxia.api.Value
import com.tersesystems.echopraxia.api.Level
import com.tersesystems.echopraxia.plusscala.api._
import com.tersesystems.echopraxia.spi.CoreLogger
import sourcecode._

import java.util.Collections
import scala.collection.JavaConverters.seqAsJavaListConverter

/**
 * This logger does not expose a field builder and instead relies on a complimentary trait to implicitly convert arguments into Fields.
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
  private val traceMethod = new LoggerMethodWithLevel(Level.TRACE, core)
  private val debugMethod = new LoggerMethodWithLevel(Level.DEBUG, core)
  private val infoMethod = new LoggerMethodWithLevel(Level.INFO, core)
  private val warnMethod = new LoggerMethodWithLevel(Level.WARN, core)
  private val errorMethod = new LoggerMethodWithLevel(Level.ERROR, core)

  def name: String = core.getName()

  def withFields(fields: => Seq[Field]): Logger = {
    new Logger(core.withFields((_: PresentationFieldBuilder) => FieldBuilderResult.list(fields.asJava), PresentationFieldBuilder))
  }

  def withCondition(condition: Condition): Logger = new Logger(core.withCondition(condition.asJava))

  def isTraceEnabled = core.isEnabled(Level.TRACE)
  def trace: LoggerMethod = if (core.isEnabled(Level.TRACE)) traceMethod else LoggerMethod.NoOp

  def isDebugEnabled = core.isEnabled(Level.DEBUG)
  def debug: LoggerMethod = if (core.isEnabled(Level.DEBUG)) debugMethod else LoggerMethod.NoOp

  def isInfoEnabled = core.isEnabled(Level.INFO)
  def info: LoggerMethod = if (core.isEnabled(Level.INFO)) infoMethod else LoggerMethod.NoOp

  def isWarnEnabled = core.isEnabled(Level.WARN)
  def warn: LoggerMethod = if (core.isEnabled(Level.WARN)) warnMethod else LoggerMethod.NoOp

  def isErrorEnabled = core.isEnabled(Level.ERROR)
  def error: LoggerMethod = if (core.isEnabled(Level.ERROR)) errorMethod else LoggerMethod.NoOp
}

trait LoggerMethod {
  // Don't use call by name here, as we've already determined we are logging if it makes it past
  // the log level check to access the method.  This also means we don't have to deal with type erasure if
  // we want to do anything fancy.
  //
  // There is also no point in having special (field1, field2) methods here as we would have to create
  // extra objects to concatenate them in any case.

  def apply(fields: Field*)(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(message: String, fields: Field*)(implicit line: Line, file: File, enclosing: Enclosing): Unit
}

object LoggerMethod {
  val NoOp = new LoggerMethod {
    override def apply(fields: Field*)(implicit line: Line, file: File, enclosing: Enclosing): Unit = ()
    override def apply(message: String, fields: Field*)(implicit line: Line, file: File, enclosing: Enclosing): Unit = ()
  }
}

class LoggerMethodWithLevel(level: Level, core: CoreLogger) extends LoggerMethod {
  override def apply(fields: Field*)(implicit line: Line, file: File, enclosing: Enclosing): Unit =
    apply(("{} " * fields.size).trim, fields: _*)

  override def apply(message: String, fields: Field*)(implicit line: Line, file: File, enclosing: Enclosing): Unit =
    handle(level, message, list(fields.toArray))

  private def handle(level: Level, message: String, f: => FieldBuilderResult)(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
    import scala.compat.java8.FunctionConverters._

    val f1: PresentationFieldBuilder => FieldBuilderResult = _ => f
    core.log(level, () => Collections.singletonList(sourceCodeField), message, f1.asJava, PresentationFieldBuilder)
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
