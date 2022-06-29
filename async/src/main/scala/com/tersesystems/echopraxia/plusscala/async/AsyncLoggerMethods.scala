package com.tersesystems.echopraxia.plusscala.async

import com.tersesystems.echopraxia.api.FieldBuilderResult
import com.tersesystems.echopraxia.plusscala.api.Condition

/**
 * Async Logger Methods with source code implicits.
 */
trait AsyncLoggerMethods[FB] {

  // Use a dependent type here as IntelliJ gets very confused if a parametric type is used.
  trait Handle {
    def apply(message: String): Unit
    def apply(message: String, e: Throwable): Unit
    def apply(message: String, f: FB => FieldBuilderResult): Unit
  }

  // ------------------------------------------------------------------------
  // TRACE

  /**
   * Logs statement at TRACE level.
   *
   * @param message
   *   the given message.
   */
  def trace(message: String): Unit

  def ifTraceEnabled(consumer: Handle => Unit): Unit

  def ifTraceEnabled(condition: Condition)(consumer: Handle => Unit): Unit

  /**
   * Logs statement at TRACE level using a field builder function.
   *
   * @param message
   *   the message.
   * @param f
   *   the field builder function.
   */
  def trace(message: String, f: FB => FieldBuilderResult): Unit

  /**
   * Logs statement at TRACE level with exception.
   *
   * @param message
   *   the message.
   * @param e
   *   the given exception.
   */
  def trace(message: String, e: Throwable): Unit

  /**
   * Conditionally logs statement at TRACE level.
   *
   * @param condition
   *   the given condition.
   * @param message
   *   the message.
   */
  def trace(condition: Condition, message: String): Unit

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
  def trace(condition: Condition, message: String, f: FB => FieldBuilderResult): Unit

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
  def trace(condition: Condition, message: String, e: Throwable): Unit

  // ------------------------------------------------------------------------
  // DEBUG

  def ifDebugEnabled(consumer: Handle => Unit): Unit

  def ifDebugEnabled(condition: Condition)(consumer: Handle => Unit): Unit

  /**
   * Logs statement at DEBUG level.
   *
   * @param message
   *   the given message.
   */
  def debug(message: String): Unit

  /**
   * Logs statement at DEBUG level using a field builder function.
   *
   * @param message
   *   the message.
   * @param f
   *   the field builder function.
   */
  def debug(message: String, f: FB => FieldBuilderResult): Unit

  /**
   * Logs statement at DEBUG level with exception.
   *
   * @param message
   *   the message.
   * @param e
   *   the given exception.
   */
  def debug(message: String, e: Throwable): Unit

  /**
   * Conditionally logs statement at DEBUG level.
   *
   * @param condition
   *   the given condition.
   * @param message
   *   the message.
   */
  def debug(condition: Condition, message: String): Unit

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
  def debug(condition: Condition, message: String, f: FB => FieldBuilderResult): Unit

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
  def debug(condition: Condition, message: String, e: Throwable): Unit

  // ------------------------------------------------------------------------
  // INFO

  def ifInfoEnabled(consumer: Handle => Unit): Unit

  def ifInfoEnabled(condition: Condition)(consumer: Handle => Unit): Unit

  /**
   * Logs statement at INFO level.
   *
   * @param message
   *   the given message.
   */
  def info(message: String): Unit

  /**
   * Logs statement at INFO level using a field builder function.
   *
   * @param message
   *   the message.
   * @param f
   *   the field builder function.
   */
  def info(message: String, f: FB => FieldBuilderResult): Unit

  /**
   * Logs statement at INFO level with exception.
   *
   * @param message
   *   the message.
   * @param e
   *   the given exception.
   */
  def info(message: String, e: Throwable): Unit

  /**
   * Conditionally logs statement at INFO level.
   *
   * @param condition
   *   the given condition.
   * @param message
   *   the message.
   */
  def info(condition: Condition, message: String): Unit

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
  def info(condition: Condition, message: String, f: FB => FieldBuilderResult): Unit

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
  def info(condition: Condition, message: String, e: Throwable): Unit

  // ------------------------------------------------------------------------
  // WARN

  def ifWarnEnabled(consumer: Handle => Unit): Unit

  def ifWarnEnabled(condition: Condition)(consumer: Handle => Unit): Unit

  /**
   * Logs statement at WARN level.
   *
   * @param message
   *   the given message.
   */
  def warn(message: String): Unit

  /**
   * Logs statement at WARN level using a field builder function.
   *
   * @param message
   *   the message.
   * @param f
   *   the field builder function.
   */
  def warn(message: String, f: FB => FieldBuilderResult): Unit

  /**
   * Logs statement at WARN level with exception.
   *
   * @param message
   *   the message.
   * @param e
   *   the given exception.
   */
  def warn(message: String, e: Throwable): Unit

  def warn(condition: Condition, message: String): Unit

  def warn(condition: Condition, message: String, f: FB => FieldBuilderResult): Unit

  def warn(condition: Condition, message: String, e: Throwable): Unit

  // ------------------------------------------------------------------------
  // ERROR

  def ifErrorEnabled(consumer: Handle => Unit): Unit

  def ifErrorEnabled(condition: Condition)(consumer: Handle => Unit): Unit

  def error(message: String): Unit

  def error(message: String, f: FB => FieldBuilderResult): Unit

  def error(message: String, e: Throwable): Unit

  def error(condition: Condition, message: String): Unit

  def error(condition: Condition, message: String, f: FB => FieldBuilderResult): Unit

  def error(condition: Condition, message: String, e: Throwable): Unit
}
