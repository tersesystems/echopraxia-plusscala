package com.tersesystems.echopraxia.logger2

import com.tersesystems.echopraxia.api.FieldBuilderResult
import com.tersesystems.echopraxia.plusscala.api._


trait LoggerMethod[FB] {
  /** @return true if the logger level is TRACE or higher. */
  def enabled: Boolean

  /**
   * @param condition
   * the given condition.
   * @return
   * true if the logger level is TRACE or higher and the condition is met.
   */
  def enabled(condition: Condition): Boolean

  /**
   * Logs statement at TRACE level.
   *
   * @param message
   * the given message.
   */
  def apply(message: String): Unit

  /**
   * Logs statement at TRACE level using a field builder function.
   *
   * @param message
   * the message.
   * @param f
   * the field builder function.
   */
  def apply(message: String, f1: FB => FieldBuilderResult): Unit

  def apply(message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult): Unit

  def apply(message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult, f3: FB => FieldBuilderResult): Unit

  def apply(message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult, f3: FB => FieldBuilderResult, f4: FB => FieldBuilderResult): Unit

  /**
   * Conditionally logs statement at TRACE level.
   *
   * @param condition
   * the given condition.
   * @param message
   * the message.
   */
  def apply(condition: Condition, message: String): Unit


  /**
   * Conditionally logs statement at TRACE level using a field builder function.
   *
   * @param condition
   * the given condition.
   * @param message
   * the message.
   * @param f
   * the field builder function.
   */
  def apply(condition: Condition, message: String, f1: FB => FieldBuilderResult): Unit

  def apply(condition: Condition, message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult): Unit
  def apply(condition: Condition, message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult, f3: FB => FieldBuilderResult): Unit
  def apply(condition: Condition, message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult, f3: FB => FieldBuilderResult, f4: FB => FieldBuilderResult): Unit
}
