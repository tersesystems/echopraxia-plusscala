package com.tersesystems.echopraxia.plusscala.async

import com.tersesystems.echopraxia.api.Level._
import com.tersesystems.echopraxia.api._
import com.tersesystems.echopraxia.plusscala.api.{Condition, DefaultMethodsSupport}

import java.util.function
import scala.compat.java8.FunctionConverters._

/**
 * Default Async Logger Methods with source code implicits.
 */
trait DefaultAsyncLoggerMethods[FB] extends AsyncLoggerMethods[FB] {
  self: DefaultMethodsSupport[FB] =>

  // ------------------------------------------------------------------------
  // TRACE

  override def ifTraceEnabled(consumer: Handle => Unit): Unit =
    handleConsumer(TRACE, consumer)

  override def ifTraceEnabled(condition: Condition)(consumer: Handle => Unit): Unit =
    handleConsumer(TRACE, condition, consumer)

  /**
   * Logs statement at TRACE level.
   *
   * @param message
   *   the given message.
   */
  def trace(message: String): Unit =
    handleMessage(TRACE, message)

  /**
   * Logs statement at TRACE level using a field builder function.
   *
   * @param message
   *   the message.
   * @param f
   *   the field builder function.
   */
  def trace(message: String, f: FB => FieldBuilderResult): Unit =
    handleMessageArgs(TRACE, message, f)

  /**
   * Logs statement at TRACE level with exception.
   *
   * @param message
   *   the message.
   * @param e
   *   the given exception.
   */
  def trace(message: String, e: Throwable): Unit =
    handleMessageThrowable(TRACE, message, e)

  /**
   * Conditionally logs statement at TRACE level.
   *
   * @param condition
   *   the given condition.
   * @param message
   *   the message.
   */
  def trace(condition: Condition, message: String): Unit =
    handleConditionMessage(TRACE, condition, message)

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
  def trace(condition: Condition, message: String, f: FB => FieldBuilderResult): Unit =
    handleConditionMessageArgs(TRACE, condition, message, f)

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
  def trace(condition: Condition, message: String, e: Throwable): Unit =
    handleConditionMessageThrowable(TRACE, condition, message, e)

  // -----------------------------------------------------------
  // DEBUG

  override def ifDebugEnabled(consumer: Handle => Unit): Unit =
    handleConsumer(DEBUG, consumer)

  override def ifDebugEnabled(condition: Condition)(consumer: Handle => Unit): Unit =
    handleConsumer(DEBUG, condition, consumer)

  /**
   * Logs statement at DEBUG level.
   *
   * @param message
   *   the given message.
   */
  def debug(message: String): Unit =
    handleMessage(DEBUG, message)

  /**
   * Logs statement at DEBUG level using a field builder function.
   *
   * @param message
   *   the message.
   * @param f
   *   the field builder function.
   */
  def debug(message: String, f: FB => FieldBuilderResult): Unit =
    handleMessageArgs(DEBUG, message, f)

  /**
   * Logs statement at DEBUG level with exception.
   *
   * @param message
   *   the message.
   * @param e
   *   the given exception.
   */
  def debug(message: String, e: Throwable): Unit =
    handleMessageThrowable(DEBUG, message, e)

  /**
   * Conditionally logs statement at DEBUG level.
   *
   * @param condition
   *   the given condition.
   * @param message
   *   the message.
   */
  def debug(condition: Condition, message: String): Unit =
    handleConditionMessage(DEBUG, condition, message)

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
  def debug(condition: Condition, message: String, e: Throwable): Unit =
    handleConditionMessageThrowable(DEBUG, condition, message, e)

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
  def debug(condition: Condition, message: String, f: FB => FieldBuilderResult): Unit =
    handleConditionMessageArgs(DEBUG, condition, message, f)

  // -----------------------------------------------------------
  // INFO

  override def ifInfoEnabled(consumer: Handle => Unit): Unit =
    handleConsumer(INFO, consumer)

  override def ifInfoEnabled(condition: Condition)(consumer: Handle => Unit): Unit =
    handleConsumer(INFO, condition, consumer)

  /**
   * Logs statement at INFO level.
   *
   * @param message
   *   the given message.
   */
  def info(message: String): Unit =
    handleMessage(INFO, message)

  /**
   * Logs statement at INFO level using a field builder function.
   *
   * @param message
   *   the message.
   * @param f
   *   the field builder function.
   */
  def info(message: String, f: FB => FieldBuilderResult): Unit =
    handleMessageArgs(INFO, message, f)

  /**
   * Logs statement at INFO level with exception.
   *
   * @param message
   *   the message.
   * @param e
   *   the given exception.
   */
  def info(message: String, e: Throwable): Unit =
    handleMessageThrowable(INFO, message, e)

  /**
   * Conditionally logs statement at INFO level.
   *
   * @param condition
   *   the given condition.
   * @param message
   *   the message.
   */
  def info(condition: Condition, message: String): Unit =
    handleConditionMessage(INFO, condition, message)

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
  def info(condition: Condition, message: String, f: FB => FieldBuilderResult): Unit =
    handleConditionMessageArgs(INFO, condition, message, f)

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
  def info(condition: Condition, message: String, e: Throwable): Unit =
    handleConditionMessageThrowable(INFO, condition, message, e)

  // -----------------------------------------------------------
  // WARN

  override def ifWarnEnabled(consumer: Handle => Unit): Unit =
    handleConsumer(WARN, consumer)

  override def ifWarnEnabled(condition: Condition)(consumer: Handle => Unit): Unit =
    handleConsumer(WARN, condition, consumer)

  /**
   * Logs statement at WARN level.
   *
   * @param message
   *   the given message.
   */
  def warn(message: String): Unit =
    handleMessage(WARN, message)

  /**
   * Logs statement at WARN level using a field builder function.
   *
   * @param message
   *   the message.
   * @param f
   *   the field builder function.
   */
  def warn(message: String, f: FB => FieldBuilderResult): Unit =
    handleMessageArgs(WARN, message, f)

  /**
   * Logs statement at WARN level with exception.
   *
   * @param message
   *   the message.
   * @param e
   *   the given exception.
   */
  def warn(message: String, e: Throwable): Unit =
    handleMessageThrowable(WARN, message, e)

  def warn(condition: Condition, message: String): Unit =
    handleConditionMessage(WARN, condition, message)

  def warn(condition: Condition, message: String, e: Throwable): Unit =
    handleConditionMessageThrowable(WARN, condition, message, e)

  def warn(condition: Condition, message: String, f: FB => FieldBuilderResult): Unit =
    handleConditionMessageArgs(WARN, condition, message, f)

  // -----------------------------------------------------------
  // ERROR

  override def ifErrorEnabled(consumer: Handle => Unit): Unit =
    handleConsumer(ERROR, consumer)

  override def ifErrorEnabled(condition: Condition)(consumer: Handle => Unit): Unit =
    handleConsumer(ERROR, condition, consumer)

  def error(message: String): Unit = { handleMessage(ERROR, message) }

  def error(message: String, f: FB => FieldBuilderResult): Unit =
    handleMessageArgs(ERROR, message, f)

  def error(message: String, e: Throwable): Unit =
    handleMessageThrowable(ERROR, message, e)

  def error(condition: Condition, message: String): Unit =
    handleConditionMessage(ERROR, condition, message)

  def error(condition: Condition, message: String, f: FB => FieldBuilderResult): Unit =
    handleConditionMessageArgs(ERROR, condition, message, f)

  def error(condition: Condition, message: String, e: Throwable): Unit =
    handleConditionMessageThrowable(ERROR, condition, message, e)

  // -----------------------------------------------------------
  // Internal methods

  @inline
  private def onlyException(e: Throwable): FieldBuilderResult = {
    Field.keyValue(FieldConstants.EXCEPTION, Value.exception(e))
  }

  @inline
  private def handleConsumer(level: Level, consumer: Handle => Unit): Unit = {
    core.asyncLog(level, (h: LoggerHandle[FB]) => consumer(h), fieldBuilder)
  }

  @inline
  private def handleConsumer(level: Level, condition: Condition, consumer: Handle => Unit): Unit = {
    core.asyncLog(level, condition.asJava, (h: LoggerHandle[FB]) => consumer(h), fieldBuilder)
  }

  @inline
  private implicit def toHandle(h: LoggerHandle[FB]): Handle = new Handle {
    @inline
    override def apply(message: String): Unit = h.log(message)
    @inline
    override def apply(message: String, e: Throwable): Unit = {
      val f: function.Function[FB, FieldBuilderResult] = _ => onlyException(e)
      h.log(message, f)
    }
    @inline
    override def apply(message: String, f: FB => FieldBuilderResult): Unit =
      h.log(message, f.asJava)
  }

  @inline
  private def handleMessage(level: Level, message: String): Unit =
    handleConsumer(level, handle => handle(message))

  @inline
  private def handleMessageArgs(level: Level, message: String, f: FB => FieldBuilderResult): Unit =
    handleConsumer(level, handle => handle(message, f))

  @inline
  private def handleMessageThrowable(level: Level, message: String, e: Throwable): Unit =
    handleConsumer(level, handle => handle(message, e))

  @inline
  private def handleConditionMessage(level: Level, condition: Condition, message: String): Unit =
    handleConsumer(level, condition, handle => handle(message))

  @inline
  private def handleConditionMessageArgs(level: Level, condition: Condition, message: String, f: FB => FieldBuilderResult): Unit = {
    handleConsumer(level, condition, handle => handle(message, f))
  }

  @inline
  private def handleConditionMessageThrowable(level: Level, condition: Condition, message: String, e: Throwable): Unit = {
    handleConsumer(level, condition, handle => handle(message, e))
  }

}
