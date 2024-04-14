package com.tersesystems.echopraxia.plusscala

import com.tersesystems.echopraxia.api.Field
import com.tersesystems.echopraxia.api.FieldBuilderResult
import com.tersesystems.echopraxia.plusscala.api.Condition
import sourcecode._

trait LoggerMethod[FB] {

  def apply(fields: Field*)(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(message: String, fields: Field*)(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(message: String, exception: Throwable)(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(message: String, f: FB => FieldBuilderResult)(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(condition: Condition, fields: Field*)(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(condition: Condition, message: String, fields: Field*)(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(condition: Condition, message: String, exception: Throwable)(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(condition: Condition, message: String, f: FB => FieldBuilderResult)(implicit line: Line, file: File, enclosing: Enclosing): Unit
}

object LoggerMethod {
  def noopMethod[FB] = new LoggerMethod[FB] {
    override def apply(message: String, fields: Field*)(implicit line: Line, file: File, enclosing: Enclosing): Unit = ()

    override def apply(message: String, exception: Throwable)(implicit line: Line, file: File, enclosing: Enclosing): Unit = ()

    override def apply(message: String, f: FB => FieldBuilderResult)(implicit line: Line, file: File, enclosing: Enclosing): Unit = ()

    override def apply(condition: Condition, message: String, fields: Field*)(implicit line: Line, file: File, enclosing: Enclosing): Unit = ()

    override def apply(condition: Condition, message: String, exception: Throwable)(implicit line: Line, file: File, enclosing: Enclosing): Unit = ()

    override def apply(condition: Condition, message: String, f: FB => FieldBuilderResult)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = ()

    override def apply(fields: Field*)(implicit line: Line, file: File, enclosing: Enclosing): Unit = ()

    override def apply(condition: Condition, fields: Field*)(implicit line: Line, file: File, enclosing: Enclosing): Unit = ()
  }
}

trait LoggerMethods[FB] {

  def isTraceEnabled: Boolean

  def isTraceEnabled(condition: Condition): Boolean

  def trace: LoggerMethod[FB]

  /** @return true if the logger level is DEBUG or higher. */
  def isDebugEnabled: Boolean

  /**
   * @param condition
   *   the given condition.
   * @return
   *   true if the logger level is DEBUG or higher and the condition is met.
   */
  def isDebugEnabled(condition: Condition): Boolean

  def debug: LoggerMethod[FB]

  /** @return true if the logger level is INFO or higher. */
  def isInfoEnabled: Boolean

  /**
   * @param condition
   *   the given condition.
   * @return
   *   true if the logger level is INFO or higher and the condition is met.
   */
  def isInfoEnabled(condition: Condition): Boolean

  def info: LoggerMethod[FB]

  /** @return true if the logger level is WARN or higher. */
  def isWarnEnabled: Boolean

  /**
   * @param condition
   *   the given condition.
   * @return
   *   true if the logger level is WARN or higher and the condition is met.
   */
  def isWarnEnabled(condition: Condition): Boolean

  def warn: LoggerMethod[FB]

  /** @return true if the logger level is ERROR or higher. */
  def isErrorEnabled: Boolean

  /**
   * @param condition
   *   the given condition.
   * @return
   *   true if the logger level is ERROR or higher and the condition is met.
   */
  def isErrorEnabled(condition: Condition): Boolean

  def error: LoggerMethod[FB]
}
