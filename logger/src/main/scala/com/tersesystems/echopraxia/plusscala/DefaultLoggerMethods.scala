package com.tersesystems.echopraxia.plusscala

import com.tersesystems.echopraxia.api.Level._
import com.tersesystems.echopraxia.api._
import com.tersesystems.echopraxia.api.{Condition => JCondition}
import com.tersesystems.echopraxia.plusscala.api.{Condition, DefaultMethodsSupport}
import sourcecode.{Enclosing, File, Line}

import scala.compat.java8.FunctionConverters._

/**
 * Default Logger methods with source code implicits.
 *
 * This implementation uses the protected `sourceInfoFields` method to add source code information as context fields, adding a `sourcecode` object
 * containing `line`, `file`, and `enclosing` fields.
 *
 * You can subclass this method and override `sourceInfoFields` to provide your own implementation.
 */
trait DefaultLoggerMethods[FB] extends LoggerMethods[FB] {
  this: DefaultMethodsSupport[FB] =>

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

  /**
   * Logs statement at TRACE level.
   *
   * @param message
   *   the given message.
   */
  def trace(
      message: String
  )(implicit line: sourcecode.Line, file: sourcecode.File, enc: sourcecode.Enclosing): Unit =
    handleMessage(TRACE, message)

  /**
   * Logs statement at TRACE level using a field builder function.
   *
   * @param message
   *   the message.
   * @param f
   *   the field builder function.
   */
  def trace(message: String, f: FB => FieldBuilderResult)(implicit
      line: sourcecode.Line,
      file: sourcecode.File,
      enc: sourcecode.Enclosing
  ): Unit = handleMessageArgs(TRACE, message, f)

  /**
   * Logs statement at TRACE level with exception.
   *
   * @param message
   *   the message.
   * @param e
   *   the given exception.
   */
  def trace(message: String, e: Throwable)(implicit
      line: sourcecode.Line,
      file: sourcecode.File,
      enc: sourcecode.Enclosing
  ): Unit = handleMessageThrowable(TRACE, message, e)

  /**
   * Conditionally logs statement at TRACE level.
   *
   * @param condition
   *   the given condition.
   * @param message
   *   the message.
   */
  def trace(condition: Condition, message: String)(implicit
      line: sourcecode.Line,
      file: sourcecode.File,
      enc: sourcecode.Enclosing
  ): Unit = handleConditionMessage(TRACE, condition, message)

  /**
   * Conditionally logs statement at TRACE level using a field builder function.
   *
   * @param condition
   *   the given condition.
   * @param message
   *   the message.
   * @param f
   *   the field builder function.
   */
  def trace(condition: Condition, message: String, f: FB => FieldBuilderResult)(implicit
      line: sourcecode.Line,
      file: sourcecode.File,
      enc: sourcecode.Enclosing
  ): Unit = handleConditionMessageArgs(TRACE, condition, message, f)

  /**
   * Conditionally logs statement at TRACE level with exception.
   *
   * @param condition
   *   the given condition.
   * @param message
   *   the message.
   * @param e
   *   the given exception.
   */
  def trace(condition: Condition, message: String, e: Throwable)(implicit
      line: sourcecode.Line,
      file: sourcecode.File,
      enc: sourcecode.Enclosing
  ): Unit = handleConditionMessageThrowable(TRACE, condition, message, e)

  // -----------------------------------------------------------
  // DEBUG

  /** @return true if the logger level is DEBUG or higher. */
  def isDebugEnabled: Boolean = core.isEnabled(DEBUG)

  /**
   * @param condition
   *   the given condition.
   * @return
   *   true if the logger level is DEBUG or higher and the condition is met.
   */
  def isDebugEnabled(condition: Condition): Boolean = core.isEnabled(DEBUG, condition.asJava)

  /**
   * Logs statement at DEBUG level.
   *
   * @param message
   *   the given message.
   */
  def debug(
      message: String
  )(implicit line: sourcecode.Line, file: sourcecode.File, enc: sourcecode.Enclosing): Unit =
    handleMessage(DEBUG, message)

  /**
   * Logs statement at DEBUG level using a field builder function.
   *
   * @param message
   *   the message.
   * @param f
   *   the field builder function.
   */
  def debug(message: String, f: FB => FieldBuilderResult)(implicit
      line: sourcecode.Line,
      file: sourcecode.File,
      enc: sourcecode.Enclosing
  ): Unit = handleMessageArgs(DEBUG, message, f)

  /**
   * Logs statement at DEBUG level with exception.
   *
   * @param message
   *   the message.
   * @param e
   *   the given exception.
   */
  def debug(message: String, e: Throwable)(implicit
      line: sourcecode.Line,
      file: sourcecode.File,
      enc: sourcecode.Enclosing
  ): Unit = handleMessageThrowable(DEBUG, message, e)

  /**
   * Conditionally logs statement at DEBUG level.
   *
   * @param condition
   *   the given condition.
   * @param message
   *   the message.
   */
  def debug(condition: Condition, message: String)(implicit
      line: sourcecode.Line,
      file: sourcecode.File,
      enc: sourcecode.Enclosing
  ): Unit = handleConditionMessage(DEBUG, condition, message)

  /**
   * Conditionally logs statement at DEBUG level with exception.
   *
   * @param condition
   *   the given condition.
   * @param message
   *   the message.
   * @param e
   *   the given exception.
   */
  def debug(condition: Condition, message: String, e: Throwable)(implicit
      line: sourcecode.Line,
      file: sourcecode.File,
      enc: sourcecode.Enclosing
  ): Unit = handleConditionMessageThrowable(DEBUG, condition, message, e)

  /**
   * Conditionally logs statement at DEBUG level using a field builder function.
   *
   * @param condition
   *   the given condition.
   * @param message
   *   the message.
   * @param f
   *   the field builder function.
   */
  def debug(condition: Condition, message: String, f: FB => FieldBuilderResult)(implicit
      line: sourcecode.Line,
      file: sourcecode.File,
      enc: sourcecode.Enclosing
  ): Unit = handleConditionMessageArgs(DEBUG, condition, message, f)

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

  /**
   * Logs statement at INFO level.
   *
   * @param message
   *   the given message.
   */
  def info(
      message: String
  )(implicit line: sourcecode.Line, file: sourcecode.File, enc: sourcecode.Enclosing): Unit =
    handleMessage(INFO, message)

  /**
   * Logs statement at INFO level using a field builder function.
   *
   * @param message
   *   the message.
   * @param f
   *   the field builder function.
   */
  def info(message: String, f: FB => FieldBuilderResult)(implicit
      line: sourcecode.Line,
      file: sourcecode.File,
      enc: sourcecode.Enclosing
  ): Unit = handleMessageArgs(INFO, message, f)

  /**
   * Logs statement at INFO level with exception.
   *
   * @param message
   *   the message.
   * @param e
   *   the given exception.
   */
  def info(message: String, e: Throwable)(implicit
      line: sourcecode.Line,
      file: sourcecode.File,
      enc: sourcecode.Enclosing
  ): Unit = handleMessageThrowable(INFO, message, e)

  /**
   * Conditionally logs statement at INFO level.
   *
   * @param condition
   *   the given condition.
   * @param message
   *   the message.
   */
  def info(condition: Condition, message: String)(implicit
      line: sourcecode.Line,
      file: sourcecode.File,
      enc: sourcecode.Enclosing
  ): Unit = handleConditionMessage(INFO, condition, message)

  /**
   * Conditionally logs statement at INFO level using a field builder function.
   *
   * @param condition
   *   the given condition.
   * @param message
   *   the message.
   * @param f
   *   the field builder function.
   */
  def info(condition: Condition, message: String, f: FB => FieldBuilderResult)(implicit
      line: sourcecode.Line,
      file: sourcecode.File,
      enc: sourcecode.Enclosing
  ): Unit = handleConditionMessageArgs(INFO, condition, message, f)

  /**
   * Conditionally logs statement at INFO level with exception.
   *
   * @param condition
   *   the given condition.
   * @param message
   *   the message.
   * @param e
   *   the given exception.
   */
  def info(condition: Condition, message: String, e: Throwable)(implicit
      line: sourcecode.Line,
      file: sourcecode.File,
      enc: sourcecode.Enclosing
  ): Unit = handleConditionMessageThrowable(INFO, condition, message, e)

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

  /**
   * Logs statement at WARN level.
   *
   * @param message
   *   the given message.
   */
  def warn(
      message: String
  )(implicit line: sourcecode.Line, file: sourcecode.File, enc: sourcecode.Enclosing): Unit =
    handleMessage(WARN, message)

  /**
   * Logs statement at WARN level using a field builder function.
   *
   * @param message
   *   the message.
   * @param f
   *   the field builder function.
   */
  def warn(message: String, f: FB => FieldBuilderResult)(implicit
      line: sourcecode.Line,
      file: sourcecode.File,
      enc: sourcecode.Enclosing
  ): Unit = handleMessageArgs(WARN, message, f)

  /**
   * Logs statement at WARN level with exception.
   *
   * @param message
   *   the message.
   * @param e
   *   the given exception.
   */
  def warn(message: String, e: Throwable)(implicit
      line: sourcecode.Line,
      file: sourcecode.File,
      enc: sourcecode.Enclosing
  ): Unit = handleMessageThrowable(WARN, message, e)

  def warn(condition: Condition, message: String)(implicit
      line: sourcecode.Line,
      file: sourcecode.File,
      enc: sourcecode.Enclosing
  ): Unit = handleConditionMessage(WARN, condition, message)

  def warn(condition: Condition, message: String, e: Throwable)(implicit
      line: sourcecode.Line,
      file: sourcecode.File,
      enc: sourcecode.Enclosing
  ): Unit = handleConditionMessageThrowable(WARN, condition, message, e)

  def warn(condition: Condition, message: String, f: FB => FieldBuilderResult)(implicit
      line: sourcecode.Line,
      file: sourcecode.File,
      enc: sourcecode.Enclosing
  ): Unit = handleConditionMessageArgs(WARN, condition, message, f)

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

  def error(
      message: String
  )(implicit line: sourcecode.Line, file: sourcecode.File, enc: sourcecode.Enclosing): Unit = {
    handleMessage(ERROR, message)
  }

  def error(message: String, f: FB => FieldBuilderResult)(implicit
      line: sourcecode.Line,
      file: sourcecode.File,
      enc: sourcecode.Enclosing
  ): Unit = {
    handleMessageArgs(ERROR, message, f)
  }

  def error(message: String, e: Throwable)(implicit
      line: sourcecode.Line,
      file: sourcecode.File,
      enc: sourcecode.Enclosing
  ): Unit = {
    handleMessageThrowable(ERROR, message, e)
  }

  def error(condition: Condition, message: String)(implicit
      line: sourcecode.Line,
      file: sourcecode.File,
      enc: sourcecode.Enclosing
  ): Unit = {
    handleConditionMessage(ERROR, condition, message)
  }

  def error(condition: Condition, message: String, f: FB => FieldBuilderResult)(implicit
      line: sourcecode.Line,
      file: sourcecode.File,
      enc: sourcecode.Enclosing
  ): Unit = {
    handleConditionMessageArgs(ERROR, condition, message, f)
  }

  def error(condition: Condition, message: String, e: Throwable)(implicit
      line: sourcecode.Line,
      file: sourcecode.File,
      enc: sourcecode.Enclosing
  ): Unit = {
    handleConditionMessageThrowable(ERROR, condition, message, e)
  }

  // -----------------------------------------------------------
  // Internal methods

  protected def sourceInfoFields(
      line: Line,
      file: File,
      enc: Enclosing
  ): java.util.function.Function[FB, FieldBuilderResult] = { fb: FB =>
    Field
      .keyValue(
        SourceFieldConstants.sourcecode,
        Value.`object`(
          Field.keyValue(SourceFieldConstants.file, Value.string(file.value)),
          Field.keyValue(SourceFieldConstants.line, Value.number(line.value: java.lang.Integer)),
          Field.keyValue(SourceFieldConstants.enclosing, Value.string(enc.value))
        )
      )
      .asInstanceOf[FieldBuilderResult]
  }.asJava

  @inline
  private def handleMessage(level: Level, message: String)(implicit
      line: sourcecode.Line,
      file: sourcecode.File,
      enc: sourcecode.Enclosing
  ): Unit = {
    core.withFields(sourceInfoFields(line, file, enc), fieldBuilder).log(level, message)
  }

  @inline
  private def handleMessageArgs(level: Level, message: String, f: FB => FieldBuilderResult)(implicit
      line: sourcecode.Line,
      file: sourcecode.File,
      enc: sourcecode.Enclosing
  ): Unit = {
    core
      .withFields(sourceInfoFields(line, file, enc), fieldBuilder)
      .log(level, message, f.asJava, fieldBuilder)
  }

  @inline
  private def handleMessageThrowable(level: Level, message: String, e: Throwable)(implicit
      line: sourcecode.Line,
      file: sourcecode.File,
      enc: sourcecode.Enclosing
  ): Unit = {
    core
      .withFields(sourceInfoFields(line, file, enc), fieldBuilder)
      .log(
        level,
        message,
        (_: FB) => Field.keyValue(FieldConstants.EXCEPTION, Value.exception(e)),
        fieldBuilder
      )
  }

  private def handleConditionMessage(level: Level, condition: Condition, message: String)(implicit
      line: sourcecode.Line,
      file: sourcecode.File,
      enc: sourcecode.Enclosing
  ): Unit = {
    core
      .withFields(sourceInfoFields(line, file, enc), fieldBuilder)
      .log(level, handleCondition(condition), message)
  }

  @inline
  private def handleConditionMessageArgs(
      level: Level,
      condition: Condition,
      message: String,
      f: FB => FieldBuilderResult
  )(implicit
      line: sourcecode.Line,
      file: sourcecode.File,
      enc: sourcecode.Enclosing
  ): Unit = {
    core
      .withFields(sourceInfoFields(line, file, enc), fieldBuilder)
      .log(level, handleCondition(condition), message, f.asJava, fieldBuilder)
  }

  @inline
  private def handleConditionMessageThrowable(
      level: Level,
      condition: Condition,
      message: String,
      e: Throwable
  )(implicit
      line: sourcecode.Line,
      file: sourcecode.File,
      enc: sourcecode.Enclosing
  ): Unit = {
    core
      .withFields(sourceInfoFields(line, file, enc), fieldBuilder)
      .log(
        level,
        handleCondition(condition),
        message,
        (_: FB) => Field.keyValue(FieldConstants.EXCEPTION, Value.exception(e)),
        fieldBuilder
      )
  }

  @inline
  protected def handleCondition(condition: Condition): JCondition = {
    condition match {
      case always if always == Condition.always =>
        JCondition.always()
      case never if never == Condition.never =>
        JCondition.never()
      case other =>
        other.asJava
    }
  }

}
