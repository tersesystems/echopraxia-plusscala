package com.tersesystems.echopraxia.plusscala.simple

import com.tersesystems.echopraxia.api.Level._
import com.tersesystems.echopraxia.api.{Field, FieldBuilderResult, Level}
import com.tersesystems.echopraxia.plusscala.api.{Condition, FieldBuilder, SourceCode}
import com.tersesystems.echopraxia.spi.CoreLogger
import sourcecode.{Enclosing, File, Line}

import java.util
import scala.compat.java8.FunctionConverters.enrichAsJavaFunction

class Logger(val core: CoreLogger) {
  import com.tersesystems.echopraxia.plusscala.api.FieldBuilder

  private val traceMethod: LoggerMethod = new LoggerMethod(Level.TRACE)
  private val debugMethod: LoggerMethod = new LoggerMethod(Level.DEBUG)
  private val infoMethod: LoggerMethod  = new LoggerMethod(Level.INFO)
  private val warnMethod: LoggerMethod  = new LoggerMethod(Level.WARN)
  private val errorMethod: LoggerMethod = new LoggerMethod(Level.ERROR)

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

  def trace: LoggerMethod = traceMethod

  /** @return true if the logger level is DEBUG or higher. */
  def isDebugEnabled: Boolean = core.isEnabled(DEBUG)

  def debug: LoggerMethod = debugMethod

  // -----------------------------------------------------------
  // INFO

  /** @return true if the logger level is INFO or higher. */
  def isInfoEnabled: Boolean = core.isEnabled(INFO)

  def info: LoggerMethod = infoMethod

  // -----------------------------------------------------------
  // WARN

  /** @return true if the logger level is WARN or higher. */
  def isWarnEnabled: Boolean = core.isEnabled(WARN)

  def warn: LoggerMethod = warnMethod

  // -----------------------------------------------------------
  // ERROR

  /** @return true if the logger level is ERROR or higher. */
  def isErrorEnabled: Boolean = core.isEnabled(ERROR)

  def error: LoggerMethod = errorMethod

  class LoggerMethod(level: Level) {

    def apply(f: FieldBuilderResult*)(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      var totalFields = 0
      val f1: FieldBuilder => FieldBuilderResult = _ => {
        val buffer = new util.ArrayList[Field]()
        f.foreach { result =>
          val fields = result.fields()
          totalFields += fields.size()
          buffer.addAll(fields)
        }
        FieldBuilderResult.list(buffer)
      }
      val message = ("{} " * totalFields).trim
      core.log(level, () => sourceCodeField.fields(), message, f1.asJava, FieldBuilder)
    }

    def apply(message: String, f: FieldBuilderResult*)(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val f1: FieldBuilder => FieldBuilderResult = _ => {
        val buffer = new util.ArrayList[Field]()
        f.foreach(result => buffer.addAll(result.fields()))
        FieldBuilderResult.list(buffer)
      }
      core.log(level, () => sourceCodeField.fields(), message, f1.asJava, FieldBuilder)
    }

    def apply(message: String, exception: Throwable)(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val f: FieldBuilder => FieldBuilderResult = _ => onlyException(exception)
      core.log(level, () => sourceCodeField.fields(), message, f.asJava, FieldBuilder)
    }

    // -----------------------------------------------------------
    // Internal methods

    private def sourceCodeField(implicit line: Line, file: File, enc: Enclosing): FieldBuilderResult = {
      FieldBuilder.sourceCode(SourceCode(line, file, enc))
    }

    @inline
    protected def onlyException(e: Throwable): FieldBuilderResult = {
      FieldBuilder.exception(e)
    }
  }
}
