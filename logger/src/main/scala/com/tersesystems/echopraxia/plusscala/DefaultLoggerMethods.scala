package com.tersesystems.echopraxia.plusscala

import com.tersesystems.echopraxia.api.Level._
import com.tersesystems.echopraxia.api._
import com.tersesystems.echopraxia.plusscala.api.Condition
import com.tersesystems.echopraxia.plusscala.spi.DefaultMethodsSupport

/**
 * Default Logger methods with source code implicits.
 */
trait DefaultLoggerMethods[FB] extends LoggerMethods[FB] with LoggerMethodSupport[FB] {
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
  def trace(message: String): Unit = handleMessage(TRACE, message)

  /**
   * Logs statement at TRACE level using a field builder function.
   *
   * @param message
   *   the message.
   * @param f
   *   the field builder function.
   */
  def trace(message: String, f: FB => FieldBuilderResult): Unit = handleMessageArgs(TRACE, message, f)

  /**
   * Logs statement at TRACE level with exception.
   *
   * @param message
   *   the message.
   * @param e
   *   the given exception.
   */
  def trace(message: String, e: Throwable): Unit = handleMessageThrowable(TRACE, message, e)

  /**
   * Conditionally logs statement at TRACE level.
   *
   * @param condition
   *   the given condition.
   * @param message
   *   the message.
   */
  def trace(condition: Condition, message: String): Unit = handleConditionMessage(TRACE, condition, message)

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
  def trace(condition: Condition, message: String, f: FB => FieldBuilderResult): Unit = handleConditionMessageArgs(TRACE, condition, message, f)

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
  def trace(condition: Condition, message: String, e: Throwable): Unit = handleConditionMessageThrowable(TRACE, condition, message, e)

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
  ): Unit =
    handleMessage(DEBUG, message)

  /**
   * Logs statement at DEBUG level using a field builder function.
   *
   * @param message
   *   the message.
   * @param f
   *   the field builder function.
   */
  def debug(message: String, f: FB => FieldBuilderResult): Unit = handleMessageArgs(DEBUG, message, f)

  /**
   * Logs statement at DEBUG level with exception.
   *
   * @param message
   *   the message.
   * @param e
   *   the given exception.
   */
  def debug(message: String, e: Throwable): Unit = handleMessageThrowable(DEBUG, message, e)

  /**
   * Conditionally logs statement at DEBUG level.
   *
   * @param condition
   *   the given condition.
   * @param message
   *   the message.
   */
  def debug(condition: Condition, message: String): Unit = handleConditionMessage(DEBUG, condition, message)

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
  def debug(condition: Condition, message: String, e: Throwable): Unit = handleConditionMessageThrowable(DEBUG, condition, message, e)

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
  def debug(condition: Condition, message: String, f: FB => FieldBuilderResult): Unit = handleConditionMessageArgs(DEBUG, condition, message, f)

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
  ): Unit =
    handleMessage(INFO, message)

  /**
   * Logs statement at INFO level using a field builder function.
   *
   * @param message
   *   the message.
   * @param f
   *   the field builder function.
   */
  def info(message: String, f: FB => FieldBuilderResult): Unit = handleMessageArgs(INFO, message, f)

  /**
   * Logs statement at INFO level with exception.
   *
   * @param message
   *   the message.
   * @param e
   *   the given exception.
   */
  def info(message: String, e: Throwable): Unit = handleMessageThrowable(INFO, message, e)

  /**
   * Conditionally logs statement at INFO level.
   *
   * @param condition
   *   the given condition.
   * @param message
   *   the message.
   */
  def info(condition: Condition, message: String): Unit = handleConditionMessage(INFO, condition, message)

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
  def info(condition: Condition, message: String, f: FB => FieldBuilderResult): Unit = handleConditionMessageArgs(INFO, condition, message, f)

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
  def info(condition: Condition, message: String, e: Throwable): Unit = handleConditionMessageThrowable(INFO, condition, message, e)

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
  ): Unit =
    handleMessage(WARN, message)

  /**
   * Logs statement at WARN level using a field builder function.
   *
   * @param message
   *   the message.
   * @param f
   *   the field builder function.
   */
  def warn(message: String, f: FB => FieldBuilderResult): Unit = handleMessageArgs(WARN, message, f)

  /**
   * Logs statement at WARN level with exception.
   *
   * @param message
   *   the message.
   * @param e
   *   the given exception.
   */
  def warn(message: String, e: Throwable): Unit = handleMessageThrowable(WARN, message, e)

  def warn(condition: Condition, message: String): Unit = handleConditionMessage(WARN, condition, message)

  def warn(condition: Condition, message: String, e: Throwable): Unit = handleConditionMessageThrowable(WARN, condition, message, e)

  def warn(condition: Condition, message: String, f: FB => FieldBuilderResult): Unit = handleConditionMessageArgs(WARN, condition, message, f)

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

  def error(message: String): Unit = {
    handleMessage(ERROR, message)
  }

  def error(message: String, f: FB => FieldBuilderResult): Unit = {
    handleMessageArgs(ERROR, message, f)
  }

  def error(message: String, e: Throwable): Unit = {
    handleMessageThrowable(ERROR, message, e)
  }

  def error(condition: Condition, message: String): Unit = {
    handleConditionMessage(ERROR, condition, message)
  }

  def error(condition: Condition, message: String, f: FB => FieldBuilderResult): Unit = {
    handleConditionMessageArgs(ERROR, condition, message, f)
  }

  def error(condition: Condition, message: String, e: Throwable): Unit = {
    handleConditionMessageThrowable(ERROR, condition, message, e)
  }

}
