package com.tersesystems.echopraxia.plusscala

import com.tersesystems.echopraxia.api.FieldBuilderResult.list
import com.tersesystems.echopraxia.api.{Field, FieldBuilderResult, Level, Value}
import com.tersesystems.echopraxia.api.Level._
import com.tersesystems.echopraxia.plusscala.api.Condition
import com.tersesystems.echopraxia.plusscala.spi.DefaultMethodsSupport
import com.tersesystems.echopraxia.plusscala.spi.LoggerSupport
import com.tersesystems.echopraxia.spi.{CoreLogger, FieldConstants, Utilities}
import sourcecode.{Enclosing, File, Line}

import scala.compat.java8.FunctionConverters.enrichAsJavaFunction

trait Logger[FB] extends LoggerMethods[FB] with LoggerSupport[FB, Logger] with DefaultMethodsSupport[FB]

object Logger {

  def apply[FB](core: CoreLogger, fieldBuilder: FB): Logger[FB] = new Impl[FB](core, fieldBuilder)

  /**
   */
  class Impl[FB](val core: CoreLogger, val fieldBuilder: FB) extends Logger[FB] {

    private val traceMethod: LoggerMethod[FB] = new LoggerMethodWithLevel(Level.TRACE, core, fieldBuilder)
    private val debugMethod: LoggerMethod[FB] = new LoggerMethodWithLevel(Level.DEBUG, core, fieldBuilder)
    private val infoMethod: LoggerMethod[FB]  = new LoggerMethodWithLevel(Level.INFO, core, fieldBuilder)
    private val warnMethod: LoggerMethod[FB]  = new LoggerMethodWithLevel(Level.WARN, core, fieldBuilder)
    private val errorMethod: LoggerMethod[FB] = new LoggerMethodWithLevel(Level.ERROR, core, fieldBuilder)

    override def name: String = core.getName

    override def withCondition(condition: Condition): Logger[FB] = {
      condition match {
        case Condition.always =>
          this
        case Condition.never =>
          NoOp(core, fieldBuilder)
        case other =>
          newLogger(newCoreLogger = core.withCondition(other.asJava))
      }
    }

    override def withFields(f: FB => FieldBuilderResult): Logger[FB] = {
      newLogger(newCoreLogger = core.withFields(f.asJava, fieldBuilder))
    }

    override def withThreadContext: Logger[FB] = {
      newLogger(
        newCoreLogger = core.withThreadContext(Utilities.threadContext())
      )
    }

    override def withFieldBuilder[NEWFB](newFieldBuilder: NEWFB): Logger[NEWFB] = {
      newLogger(newFieldBuilder = newFieldBuilder)
    }

    @inline
    private def newLogger[T](
        newCoreLogger: CoreLogger = core,
        newFieldBuilder: T = fieldBuilder
    ): Logger[T] =
      new Impl[T](newCoreLogger, newFieldBuilder)

    // -----------------------------------------------------------
    // TRACE

    /** @return true if the logger level is TRACE or higher. */
    def isTraceEnabled: Boolean = core.isEnabled(TRACE)

    /**
     * @param condition
     *   the given condition.
     * @return
     *   true if the logger level is TRACE or higher and the condition is met.
     */
    def isTraceEnabled(condition: Condition): Boolean = core.isEnabled(TRACE, condition.asJava)

    def trace: LoggerMethod[FB] = traceMethod

    /** @return true if the logger level is DEBUG or higher. */
    def isDebugEnabled: Boolean = core.isEnabled(DEBUG)

    /**
     * @param condition
     *   the given condition.
     * @return
     *   true if the logger level is DEBUG or higher and the condition is met.
     */
    def isDebugEnabled(condition: Condition): Boolean = core.isEnabled(DEBUG, condition.asJava)

    def debug: LoggerMethod[FB] = debugMethod

    // -----------------------------------------------------------
    // INFO

    /** @return true if the logger level is INFO or higher. */
    def isInfoEnabled: Boolean = core.isEnabled(INFO)

    /**
     * @param condition
     *   the given condition.
     * @return
     *   true if the logger level is INFO or higher and the condition is met.
     */
    def isInfoEnabled(condition: Condition): Boolean = core.isEnabled(INFO, condition.asJava)

    def info: LoggerMethod[FB] = infoMethod

    // -----------------------------------------------------------
    // WARN

    /** @return true if the logger level is WARN or higher. */
    def isWarnEnabled: Boolean = core.isEnabled(WARN)

    /**
     * @param condition
     *   the given condition.
     * @return
     *   true if the logger level is WARN or higher and the condition is met.
     */
    def isWarnEnabled(condition: Condition): Boolean = core.isEnabled(WARN, condition.asJava)

    def warn: LoggerMethod[FB] = warnMethod

    // -----------------------------------------------------------
    // ERROR

    /** @return true if the logger level is ERROR or higher. */
    def isErrorEnabled: Boolean = core.isEnabled(ERROR)

    /**
     * @param condition
     *   the given condition.
     * @return
     *   true if the logger level is ERROR or higher and the condition is met.
     */
    def isErrorEnabled(condition: Condition): Boolean = core.isEnabled(ERROR, condition.asJava)

    def error: LoggerMethod[FB] = errorMethod
  }

  trait NoOp[FB] extends Logger[FB] {
    override def isTraceEnabled: Boolean = false

    override def isTraceEnabled(condition: Condition): Boolean = false

    def trace: LoggerMethod[FB] = LoggerMethod.noopMethod

    override def isDebugEnabled: Boolean = false

    override def isDebugEnabled(condition: Condition): Boolean = false

    def debug: LoggerMethod[FB] = LoggerMethod.noopMethod

    override def isInfoEnabled: Boolean = false

    override def isInfoEnabled(condition: Condition): Boolean = false

    def info: LoggerMethod[FB] = LoggerMethod.noopMethod

    override def isWarnEnabled: Boolean = false

    override def isWarnEnabled(condition: Condition): Boolean = false

    def warn: LoggerMethod[FB] = LoggerMethod.noopMethod

    override def isErrorEnabled: Boolean = false

    override def isErrorEnabled(condition: Condition): Boolean = false

    def error: LoggerMethod[FB] = LoggerMethod.noopMethod

  }

  object NoOp {
    def apply[FB](c: CoreLogger, fb: FB): NoOp[FB] = new NoOp[FB] {
      override def name: String = c.getName

      override def core: CoreLogger = c

      override def fieldBuilder: FB = fb

      override def withCondition(scalaCondition: Condition): Logger[FB] = this

      override def withFields(f: FB => FieldBuilderResult): Logger[FB] = this

      override def withThreadContext: Logger[FB] = this

      override def withFieldBuilder[T <: FB](newBuilder: T): Logger[T] = NoOp(core, newBuilder)
    }
  }
}

class LoggerMethodWithLevel[FB](level: Level, core: CoreLogger, fieldBuilder: FB) extends LoggerMethod[FB] {

  override def apply(message: String, fields: Field*)(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
    import scala.compat.java8.FunctionConverters._
    val f1: FB => FieldBuilderResult = _ => list(fields.toArray)
    core.log(level, () => java.util.Collections.singletonList(sourceCodeField), message, f1.asJava, fieldBuilder)
  }

  override def apply(message: String, f: FB => FieldBuilderResult)(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
    core.log(level, () => java.util.Collections.singletonList(sourceCodeField), message, f.asJava, fieldBuilder)
  }

  override def apply(message: String, exception: Throwable)(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
    val f: FB => FieldBuilderResult = _ => onlyException(exception)
    core.log(level, () => java.util.Collections.singletonList(sourceCodeField), message, f.asJava, fieldBuilder)
  }

  override def apply(condition: Condition, message: String, exception: Throwable)(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
    val f: FB => FieldBuilderResult = _ => onlyException(exception)
    core.log(level, () => java.util.Collections.singletonList(sourceCodeField), condition.asJava, message, f.asJava, fieldBuilder)
  }

  override def apply(condition: Condition, message: String, f: FB => FieldBuilderResult)(implicit
      line: Line,
      file: File,
      enclosing: Enclosing
  ): Unit = {
    core.log(level, () => java.util.Collections.singletonList(sourceCodeField), condition.asJava, message, f.asJava, fieldBuilder)
  }

  override def apply(condition: Condition, message: String, fields: Field*)(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
    import scala.compat.java8.FunctionConverters._
    val f1: FB => FieldBuilderResult = _ => list(fields.toArray)
    core.log(level, () => java.util.Collections.singletonList(sourceCodeField), condition.asJava, message, f1.asJava, fieldBuilder)
  }

  // -----------------------------------------------------------
  // Internal methods

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

  @inline
  protected def onlyException(e: Throwable): FieldBuilderResult = {
    Field.keyValue(FieldConstants.EXCEPTION, Value.exception(e))
  }

}
