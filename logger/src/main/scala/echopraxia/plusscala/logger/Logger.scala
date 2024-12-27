package echopraxia.plusscala.logger

import echopraxia.api.Field
import echopraxia.api.FieldBuilderResult
import echopraxia.api.FieldBuilderResult.list
import echopraxia.logging.api.{Level => JLevel}
import echopraxia.logging.spi.CoreLogger
import echopraxia.logging.spi.Utilities
import echopraxia.plusscala.api.FieldBuilder
import echopraxia.plusscala.api.SourceCode
import echopraxia.plusscala.logging.api.Condition
import echopraxia.plusscala.logging.api.DefaultMethodsSupport
import echopraxia.plusscala.logging.api.LoggerSupport
import sourcecode.Enclosing
import sourcecode.File
import sourcecode.Line

import scala.jdk.FunctionConverters.enrichAsJavaFunction

trait Logger[FB <: Singleton] extends LoggerMethods[FB] with LoggerSupport[FB, Logger] with DefaultMethodsSupport[FB]

object Logger {
  import JLevel._

  def apply[FB <: Singleton](core: CoreLogger, fieldBuilder: FB): Logger[FB] = new Impl[FB](core, fieldBuilder)

  /**
   */
  class Impl[FB <: Singleton](val core: CoreLogger, val fieldBuilder: FB) extends Logger[FB] {

    private val traceMethod: LoggerMethod[FB] = new LoggerMethodWithLevel(JLevel.TRACE, core, fieldBuilder)
    private val debugMethod: LoggerMethod[FB] = new LoggerMethodWithLevel(JLevel.DEBUG, core, fieldBuilder)
    private val infoMethod: LoggerMethod[FB]  = new LoggerMethodWithLevel(JLevel.INFO, core, fieldBuilder)
    private val warnMethod: LoggerMethod[FB]  = new LoggerMethodWithLevel(JLevel.WARN, core, fieldBuilder)
    private val errorMethod: LoggerMethod[FB] = new LoggerMethodWithLevel(JLevel.ERROR, core, fieldBuilder)

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

    override def withFieldBuilder[NEWFB <: Singleton](newFieldBuilder: NEWFB): Logger[NEWFB] = {
      newLogger(newFieldBuilder = newFieldBuilder)
    }

    @inline
    private def newLogger[T <: Singleton](
        newCoreLogger: CoreLogger = core,
        newFieldBuilder: T = fieldBuilder
    ): Logger[T] =
      new Impl[T](newCoreLogger, newFieldBuilder)

    // -----------------------------------------------------------
    // TRACE

    /** @return true if the logger level is TRACE or higher. */
    def isTraceEnabled: Boolean = core.isEnabled(JLevel.TRACE)

    /**
     * @param condition
     *   the given condition.
     * @return
     *   true if the logger level is TRACE or higher and the condition is met.
     */
    def isTraceEnabled(condition: Condition): Boolean = core.isEnabled(JLevel.TRACE, condition.asJava)

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

  trait NoOp[FB <: Singleton] extends Logger[FB] {
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
    def apply[FB <: Singleton](c: CoreLogger, fb: FB): NoOp[FB] = new NoOp[FB] {
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

class LoggerMethodWithLevel[FB](level: JLevel, core: CoreLogger, fieldBuilder: FB) extends LoggerMethod[FB] {

  override def apply(fields: Field*)(implicit line: Line, file: File, enclosing: Enclosing): Unit =
    apply(("{} " * fields.size).trim, fields*)

  override def apply(message: String, fields: Field*)(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
    val f1: FB => FieldBuilderResult = _ => list(fields.toArray)
    core.log(level, () => sourceCodeField.fields(), message, f1.asJava, fieldBuilder)
  }

  override def apply(message: String, f: FB => FieldBuilderResult)(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
    core.log(level, () => sourceCodeField.fields(), message, f.asJava, fieldBuilder)
  }

  override def apply(message: String, exception: Throwable)(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
    val f: FB => FieldBuilderResult = _ => onlyException(exception)
    core.log(level, () => sourceCodeField.fields(), message, f.asJava, fieldBuilder)
  }

  override def apply(condition: Condition, message: String, exception: Throwable)(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
    val f: FB => FieldBuilderResult = _ => onlyException(exception)
    core.log(level, () => sourceCodeField.fields(), condition.asJava, message, f.asJava, fieldBuilder)
  }

  override def apply(condition: Condition, message: String, f: FB => FieldBuilderResult)(implicit
      line: Line,
      file: File,
      enclosing: Enclosing
  ): Unit = {
    core.log(level, () => sourceCodeField.fields(), condition.asJava, message, f.asJava, fieldBuilder)
  }

  override def apply(condition: Condition, fields: Field*)(implicit line: Line, file: File, enclosing: Enclosing): Unit =
    apply(condition, ("{} " * fields.size).trim, fields*)

  override def apply(condition: Condition, message: String, fields: Field*)(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
    val f1: FB => FieldBuilderResult = _ => list(fields.toArray)
    core.log(level, () => sourceCodeField.fields(), condition.asJava, message, f1.asJava, fieldBuilder)
  }

  // -----------------------------------------------------------
  // Internal methods

  private def sourceCodeField(implicit line: Line, file: File, enc: Enclosing): FieldBuilderResult = {
    val sc = SourceCode(line, file, enc)
    val fb = FieldBuilder
    fb.sourceCode(sc)
  }

  @inline
  protected def onlyException(e: Throwable): FieldBuilderResult = {
    val fb = FieldBuilder
    fb.exception(e)
  }
}
