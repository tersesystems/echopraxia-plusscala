package com.tersesystems.echopraxia.plusscala.flow

import echopraxia.plusscala.logging.api.{Condition, DefaultMethodsSupport}

trait FlowLoggerMethods[FB <: FlowFieldBuilder] { self: DefaultMethodsSupport[FB] =>

  // Need a solid value to use dependent types here
  private[flow] val fb: FB = this.fieldBuilder

  type ToValue[B] = this.fb.ToValue[B]

  // -----------------------------------------
  // Trace

  def trace[B: ToValue](attempt: => B): B

  def trace[B: ToValue](condition: Condition)(attempt: => B): B

  // -----------------------------------------
  // Debug

  def debug[B: ToValue](attempt: => B): B

  def debug[B: ToValue](condition: Condition)(attempt: => B): B

  // -----------------------------------------
  // Info

  def info[B: ToValue](attempt: => B): B

  def info[B: ToValue](condition: Condition)(attempt: => B): B

  // -----------------------------------------
  // Warn

  def warn[B: ToValue](attempt: => B): B

  def warn[B: ToValue](condition: Condition)(attempt: => B): B

  // -----------------------------------------
  // Error

  def error[B: ToValue](attempt: => B): B

  def error[B: ToValue](condition: Condition)(attempt: => B): B

}
