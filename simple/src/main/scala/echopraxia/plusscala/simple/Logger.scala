package echopraxia.plusscala.simple

import echopraxia.api.Field
import echopraxia.api.FieldBuilderResult
import echopraxia.logging.api.Level
import echopraxia.logging.spi.CoreLogger
import echopraxia.plusscala.api.FieldBuilder
import echopraxia.plusscala.api.SourceCode
import echopraxia.plusscala.logging.api.Condition
import sourcecode.Enclosing
import sourcecode.File
import sourcecode.Line

import scala.jdk.FunctionConverters.enrichAsJavaFunction

class Logger(val core: CoreLogger) {

  private val traceMethod: LoggerMethod = new LoggerMethod(Level.TRACE)
  private val debugMethod: LoggerMethod = new LoggerMethod(Level.DEBUG)
  private val infoMethod: LoggerMethod  = new LoggerMethod(Level.INFO)
  private val warnMethod: LoggerMethod  = new LoggerMethod(Level.WARN)
  private val errorMethod: LoggerMethod = new LoggerMethod(Level.ERROR)

  def withCondition(condition: Condition): Logger = {
    new Logger(core.withCondition(condition.asJava))
  }

  def withFields(result: FieldBuilderResult): Logger = {
    new Logger(core.withFields(_ => result, FieldBuilder))
  }

  // -----------------------------------------------------------
  // TRACE

  def isTraceEnabled: Boolean                       = traceMethod.isEnabled
  def isTraceEnabled(condition: Condition): Boolean = traceMethod.isEnabled(condition)
  def trace: LoggerMethod                           = traceMethod

  // -----------------------------------------------------------
  // DEBUG

  def isDebugEnabled: Boolean                       = debugMethod.isEnabled
  def isDebugEnabled(condition: Condition): Boolean = debugMethod.isEnabled(condition)
  def debug: LoggerMethod                           = debugMethod

  // -----------------------------------------------------------
  // INFO

  def isInfoEnabled: Boolean                       = infoMethod.isEnabled
  def isInfoEnabled(condition: Condition): Boolean = infoMethod.isEnabled(condition)
  def info: LoggerMethod                           = infoMethod

  // -----------------------------------------------------------
  // WARN

  def isWarnEnabled: Boolean                       = warnMethod.isEnabled
  def isWarnEnabled(condition: Condition): Boolean = warnMethod.isEnabled(condition)
  def warn: LoggerMethod                           = warnMethod

  // -----------------------------------------------------------
  // ERROR

  def isErrorEnabled: Boolean                       = errorMethod.isEnabled
  def isErrorEnabled(condition: Condition): Boolean = errorMethod.isEnabled(condition)
  def error: LoggerMethod                           = errorMethod

  class LoggerMethod(level: Level) {

    @inline
    def isEnabled: Boolean = core.isEnabled(level)

    @inline
    def isEnabled(condition: Condition): Boolean = core.isEnabled(level, condition.asJava)

    def apply(results: Field*)(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      // we can only do this with fields which have already been computed
      val message = ("{} " * results.size).trim
      val f1: FieldBuilder => FieldBuilderResult = _ => {
        FieldBuilderResult.list(results.toArray)
      }
      core.log(level, () => sourceCodeField.fields(), message, f1.asJava, FieldBuilder)
    }

    def apply(message: String, fields: Field*)(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val f1: FieldBuilder => FieldBuilderResult = _ => {
        FieldBuilderResult.list(fields.toArray)
      }
      core.log(level, () => sourceCodeField.fields(), message, f1.asJava, FieldBuilder)
    }

    def apply(message: String, exception: Throwable)(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val f: FieldBuilder => FieldBuilderResult = _ => onlyException(exception)
      core.log(level, () => sourceCodeField.fields(), message, f.asJava, FieldBuilder)
    }

    def apply(exception: Throwable)(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val f: FieldBuilder => FieldBuilderResult = _ => onlyException(exception)
      core.log(level, () => sourceCodeField.fields(), "{}", f.asJava, FieldBuilder)
    }

    // -----------------------------------------------------------
    // Internal methods

    @inline
    private def sourceCodeField(implicit line: Line, file: File, enc: Enclosing): FieldBuilderResult = {
      FieldBuilder.sourceCode(SourceCode(line, file, enc))
    }

    @inline
    protected def onlyException(e: Throwable): FieldBuilderResult = {
      FieldBuilder.exception(e)
    }
  }
}
