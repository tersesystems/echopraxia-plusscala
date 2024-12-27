package com.tersesystems.echopraxia.plusscala.simple

import echopraxia.api.{Field, FieldBuilderResult}
import com.tersesystems.echopraxia.plusscala.api.{FieldBuilder, SourceCode}
import echopraxia.logging.api.Level
import echopraxia.logging.spi.CoreLogger
import echopraxia.plusscala.logging.api.Condition
import sourcecode.{Enclosing, File, Line}

import java.util
import scala.compat.java8.FunctionConverters.enrichAsJavaFunction

class Logger(val core: CoreLogger) {
  private val traceMethod: LoggerMethod = new LoggerMethod(Level.TRACE)
  private val debugMethod: LoggerMethod = new LoggerMethod(Level.DEBUG)
  private val infoMethod: LoggerMethod  = new LoggerMethod(Level.INFO)
  private val warnMethod: LoggerMethod  = new LoggerMethod(Level.WARN)
  private val errorMethod: LoggerMethod = new LoggerMethod(Level.ERROR)

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
